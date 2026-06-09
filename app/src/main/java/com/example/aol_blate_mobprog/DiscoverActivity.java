package com.example.aol_blate_mobprog;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.aol_blate_mobprog.models.Person;
import com.example.aol_blate_mobprog.models.User;
import java.util.ArrayList;
import java.util.List;

public class DiscoverActivity extends AppCompatActivity {

    private FirestoreManager firestoreManager;
    private List<Person> peopleList;
    private int currentIndex = 0;

    // UI Components
    private ImageView profileImage;
    private TextView tvName, tvJob, tvLocation;
    private View btnLike, btnDislike;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discover);

        firestoreManager = FirestoreManager.getInstance();
        peopleList = new ArrayList<>();

        // 1. Initialize Views (Make sure these IDs match your XML!)
        profileImage = findViewById(R.id.ProfileCardImg);
        tvName = findViewById(R.id.NameageCardTV);
        tvJob = findViewById(R.id.JobCardTV);
        tvLocation = findViewById(R.id.LocationCardTV);

        // Note: We REMOVED tvAge because we will add age to tvName

        btnLike = findViewById(R.id.LikeDiscoverBtn);
        btnDislike = findViewById(R.id.DislikeDiscoverBtn);

        View cardContainer = findViewById(R.id.layoutCardPerson);
        View btnContainer = findViewById(R.id.DiscoverBtnContainer);
        View emptyStateLayout = findViewById(R.id.layoutEmptyState);

        if (cardContainer != null) cardContainer.setVisibility(View.INVISIBLE);
        if (btnContainer != null) btnContainer.setVisibility(View.INVISIBLE);
        if (emptyStateLayout != null) emptyStateLayout.setVisibility(View.INVISIBLE);

        // 2. Setup Buttons
        if (btnLike != null) btnLike.setOnClickListener(v -> handleSwipe(true));
        if (btnDislike != null) btnDislike.setOnClickListener(v -> handleSwipe(false));

        setupNavbar();
        showHelpDialog();
        loadInitialData();
    }

    private void loadInitialData() {
        firestoreManager.getCurrentUser(new FirestoreManager.FirestoreCallback() {
            @Override
            public void onSuccess(Object result) {
                User currentUser = (User) result;
                List<String> ignoredIds = new ArrayList<>();
                if (currentUser.getAccepted() != null) ignoredIds.addAll(currentUser.getAccepted());
                if (currentUser.getRejected() != null) ignoredIds.addAll(currentUser.getRejected());

                // Pass gender to filter
                fetchAndFilterPeople(ignoredIds, currentUser.isGender());
            }

            @Override
            public void onFailure(Exception e) {
                // Fallback: Show everyone if user load fails
                fetchAndFilterPeople(new ArrayList<>(), true);
            }
        });
    }

    private void fetchAndFilterPeople(List<String> ignoredIds, boolean currentUserGender) {
        firestoreManager.getAllCandidates(new FirestoreManager.FirestoreCallback() {
            @Override
            public void onSuccess(Object result) {
                List<Person> allCandidates = (List<Person>) result;
                peopleList.clear();

                for (Person p : allCandidates) {
                    boolean isNotSwiped = !ignoredIds.contains(String.valueOf(p.getId()));
                    boolean isOppositeGender = (p.isGender() != currentUserGender);

                    if (isNotSwiped && isOppositeGender) {
                        peopleList.add(p);
                    }
                }

                if (peopleList.isEmpty()) {
                    // LIST IS EMPTY -> HIDE EVERYTHING
                    toggleEmptyState(true);
                } else {
                    // LIST HAS PEOPLE -> SHOW EVERYTHING
                    toggleEmptyState(false);
                    currentIndex = 0;
                    showPerson(currentIndex);
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(DiscoverActivity.this, "Error fetching people", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showPerson(int index) {
        // Double check if we ran out of people while swiping
        if (peopleList.isEmpty() || index >= peopleList.size()) {
            toggleEmptyState(true);
            return;
        }

        Person person = peopleList.get(index);

        // Calculate Age
        String age = "20";
        if (person.getDob() != null && person.getDob().length() >= 4) age = "21";

        if (tvName != null) tvName.setText(person.getName() + ", " + age);
        if (tvJob != null) tvJob.setText(person.getCurrent_job());
        if (tvLocation != null && person.getDomicile() != null) tvLocation.setText(person.getDomicile());

        String profileName = person.getProfile();
        int resId = 0;
        if (profileName != null && !profileName.isEmpty()) {
            resId = getResources().getIdentifier(profileName.toLowerCase(), "drawable", getPackageName());
        }
        if (resId != 0) profileImage.setImageResource(resId);
        else profileImage.setImageResource(R.drawable.ic_launcher_background);
    }

    private void handleSwipe(boolean isLike) {
        if (peopleList.isEmpty() || currentIndex >= peopleList.size()) return;

        Person currentPerson = peopleList.get(currentIndex);
        // Di dalam handleSwipe
        // Simpan ID sebagai String yang diformat menjadi 3 digit (001)
        // agar SAMA PERSIS dengan Document ID di Firestore
        String targetId = String.valueOf(currentPerson.getId()).trim();
        Log.d("DEBUG_SWIPE", "Saving swipe action for ID: " + targetId + " | isLike: " + isLike);
        firestoreManager.saveSwipeAction(targetId, isLike);

        currentIndex++; //  Move next
        showPerson(currentIndex); // Update screen
    }

    // --- NEW HELPER METHOD TO HIDE/SHOW UI ---
    private void toggleEmptyState(boolean isEmpty) {
        View cardContainer = findViewById(R.id.layoutCardPerson);
        View btnContainer = findViewById(R.id.DiscoverBtnContainer);
        View emptyStateLayout = findViewById(R.id.layoutEmptyState);

        if (isEmpty) {
            if (cardContainer != null) cardContainer.setVisibility(View.GONE);
            if (btnContainer != null) btnContainer.setVisibility(View.GONE);
            if (emptyStateLayout != null) emptyStateLayout.setVisibility(View.VISIBLE);
        } else {
            if (cardContainer != null) cardContainer.setVisibility(View.VISIBLE);
            if (btnContainer != null) btnContainer.setVisibility(View.VISIBLE);
            if (emptyStateLayout != null) emptyStateLayout.setVisibility(View.GONE);
        }
    }

    private void setupNavbar() {
        ImageView navProfile = findViewById(R.id.ProfileNav);
        ImageView navHistory = findViewById(R.id.HistoryNav);
        ImageView navChat = findViewById(R.id.ChatNav);

        if (navProfile != null) navProfile.setOnClickListener(v -> navigateTo(ProfileActivity.class));
        if (navHistory != null) navHistory.setOnClickListener(v -> navigateTo(HistoryActivity.class));
        if (navChat != null) navChat.setOnClickListener(v -> navigateTo(ChatActivity.class));
    }

    private void navigateTo(Class<?> targetActivity) {
        Intent intent = new Intent(this, targetActivity);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    // --- FUNGSI POPUP HELP (BARU) ---
    private void showHelpDialog(){
        ImageView btnHelp = findViewById(R.id.btnHelp); // ID harus sama dengan di XML

        if(btnHelp != null) {
            btnHelp.setOnClickListener(v -> {
                Dialog dialog = new Dialog(this);
                dialog.setContentView(R.layout.dialog_help);

                if (dialog.getWindow() != null) {
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                }

                TextView tvTitle = dialog.findViewById(R.id.tvDialogTitle);
                TextView tvMessage = dialog.findViewById(R.id.tvDialogMessage);
                Button btnClose = dialog.findViewById(R.id.btnCloseDialog);

                // Pesan khusus untuk halaman Discover
                if (tvTitle != null) tvTitle.setText("Discover People");
                if (tvMessage != null) tvMessage.setText("Tap 'Like' if you're interested or 'Dislike' to pass. Swipe through profiles to find your match!");

                if (btnClose != null) {
                    btnClose.setOnClickListener(view -> dialog.dismiss());
                }
                dialog.show();
            });
        }
    }
}