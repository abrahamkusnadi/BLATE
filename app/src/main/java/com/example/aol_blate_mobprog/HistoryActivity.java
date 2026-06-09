package com.example.aol_blate_mobprog;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aol_blate_mobprog.models.History;
import com.example.aol_blate_mobprog.models.User;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private HistoryAdapter adapter;
    private FirestoreManager firestoreManager;
    private FirebaseFirestore db;
    private ArrayList<History> masterList = new ArrayList<>();
    private ArrayList<History> displayList = new ArrayList<>();

    private TextView tvStatsLikeCount, tvStatsDislikeCount;
    private TextView btnAll, btnLike, btnDislike;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        firestoreManager = FirestoreManager.getInstance();
        db = FirebaseFirestore.getInstance();

        // setup recyclerview
        recyclerView = findViewById(R.id.rvHistory);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        // setup adapter
        adapter = new HistoryAdapter(displayList, this);
        recyclerView.setAdapter(adapter);

        // setup tombol filter
        btnAll = findViewById(R.id.btnFilterAll);
        btnLike = findViewById(R.id.btnFilterLike);
        btnDislike = findViewById(R.id.btnFilterDislike);

        tvStatsLikeCount = findViewById(R.id.tvStatsLikeCount);
        tvStatsDislikeCount = findViewById(R.id.tvStatsDislikeCount);

        // setup buat logic klik tombol
        btnAll.setOnClickListener(v -> filterList("ALL"));
        btnLike.setOnClickListener(v -> filterList("Like"));
        btnDislike.setOnClickListener(v -> filterList("Dislike"));

        // setup navbar
        setupNavbar();

        // setup tombol help
        showHelpDialog();

        fetchUserHistoryFromFirebase();
    }


    private void fetchUserHistoryFromFirebase() {
        Log.d("HISTORY_DEBUG", "Starting to fetch user history...");

        firestoreManager.getCurrentUser(new FirestoreManager.FirestoreCallback() {
            @Override
            public void onSuccess(Object result) {
                User currentUser = (User) result;

                List<String> acceptedIds = currentUser.getAccepted() != null ? currentUser.getAccepted() : new ArrayList<>();
                List<String> rejectedIds = currentUser.getRejected() != null ? currentUser.getRejected() : new ArrayList<>();

                List<String> cleanAccepted = new ArrayList<>();
                for (String id : acceptedIds) {
                    cleanAccepted.add(id.trim());
                }

                List<String> cleanRejected = new ArrayList<>();
                for (String id : rejectedIds) {
                    cleanRejected.add(id.trim());
                }

                Log.d("HISTORY_DEBUG", "Accepted IDs: " + cleanAccepted.toString());
                Log.d("HISTORY_DEBUG", "Rejected IDs: " + cleanRejected.toString());

                fetchPersonDetails(cleanAccepted, cleanRejected);
            }
            @Override
            public void onFailure(Exception e) {
                Log.e("HISTORY_DEBUG", "Failed to fetch user history. ", e);
            }
        });
    }

    private void fetchPersonDetails(List<String> acceptedIds, List<String> rejectedIds) {
        db.collection("person")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        masterList.clear();

                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            String personId = doc.getId().trim();

                            boolean isLiked = acceptedIds.contains(personId);
                            boolean isDisliked = rejectedIds.contains(personId);

                            if (isLiked || isDisliked) {
                                String name = doc.getString("name");
                                String profile = doc.getString("profile");

                                if (name == null) name = "Unknown User";
                                if (profile == null || profile.trim().isEmpty()) profile = "";

                                String date = "Recently";
                                String status = isLiked ? "Like" : "Dislike";

                                masterList.add(new History(name, status, date, profile));
                                Log.d("HISTORY_DEBUG", "Added to history: " + name + " | Status: " + status);
                            }
                        }

                        adapter.notifyDataSetChanged();

                        int likedCount = 0;
                        int dislikedCount = 0;

                        for (History item : masterList) {
                            if ("Like".equalsIgnoreCase(item.getStatus())) {
                                likedCount++;
                            } else if ("Dislike".equalsIgnoreCase(item.getStatus())) {
                                dislikedCount++;
                            }
                        }

                        if (btnAll != null) btnAll.setText("ALL (" + masterList.size() + ")");
                        if (btnLike != null) btnLike.setText("Likes (" + likedCount + ")");
                        if (btnDislike != null) btnDislike.setText("Dislikes (" + dislikedCount + ")");

                        if (tvStatsLikeCount != null) tvStatsLikeCount.setText(String.valueOf(likedCount));
                        if (tvStatsDislikeCount != null) tvStatsDislikeCount.setText(String.valueOf(dislikedCount));

                        filterList("ALL");
                    } else {
                        Log.e("HISTORY_DEBUG", "Failed to fetch person details. ", task.getException());
                    }
                });
    }

    private void updateButtonColor(TextView activeButton) {
        // warnanya
        int inactiveColor = Color.parseColor("#E0E0E0"); // Abu-abu
        int activeColor = Color.parseColor("#FDD835");   // Kuning
        int textActive = Color.BLACK;
        int textInactive = Color.GRAY;

        // reset tombol inactive
        setRoundedBackground(btnAll, inactiveColor);
        btnAll.setTextColor(textInactive);

        setRoundedBackground(btnLike, inactiveColor);
        btnLike.setTextColor(textInactive);

        setRoundedBackground(btnDislike, inactiveColor);
        btnDislike.setTextColor(textInactive);

        // set tombol active
        setRoundedBackground(activeButton, activeColor);
        activeButton.setTextColor(textActive);
    }

    private void filterList(String type) {
        displayList.clear(); //clear screen dulu

        if (type.equals("ALL")) {
            // masukin data semua
            displayList.addAll(masterList);
            updateButtonColor(btnAll);
        } else {
            // filtering
            for (History item : masterList) {
                if (item.getStatus().equalsIgnoreCase(type)) {
                    displayList.add(item);
                }
            }
            // ubah warna
            if (type.equals("Like")) updateButtonColor(btnLike);
            else updateButtonColor(btnDislike);
        }

        // mengabari adapter kalau data berubah
        adapter.notifyDataSetChanged();
    }
    private void setRoundedBackground(TextView view, int color) {
        android.graphics.drawable.GradientDrawable shape = new android.graphics.drawable.GradientDrawable();
        shape.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
        shape.setCornerRadius(50);
        shape.setColor(color);
        view.setBackground(shape);
    }

    private void setupNavbar() {
        ImageView navChat = findViewById(R.id.ChatNav);
        ImageView navProfile = findViewById(R.id.ProfileNav);
        ImageView navDiscover = findViewById(R.id.DiscoverNav);

        if (navChat != null) navChat.setOnClickListener(v -> {
            startActivity(new Intent(this, ChatActivity.class));
            finishAffinity();
        });

        if (navProfile != null) navProfile.setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
            finishAffinity();
        });

        navDiscover.setOnClickListener(v -> {
            Intent intent = new Intent(HistoryActivity.this, DiscoverActivity.class);
            startActivity(intent);
            finishAffinity();
        });
    }
    private void showHelpDialog(){
        ImageView btnHelp = findViewById(R.id.btnHelp);
        btnHelp.setOnClickListener(v->{
            Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.dialog_help);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            TextView tvTitle = dialog.findViewById(R.id.tvDialogTitle);
            TextView tvMessage = dialog.findViewById(R.id.tvDialogMessage);
            Button btnClose = dialog.findViewById(R.id.btnCloseDialog);

            tvTitle.setText("Activity Log");
            tvMessage.setText("This page tracks your activity history. Here you can see who you have 'Liked' or 'Disliked' previously.");

            btnClose.setOnClickListener(view -> dialog.dismiss());
            dialog.show();
        });

    }
}
