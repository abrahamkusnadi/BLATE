package com.example.aol_blate_mobprog;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvName, tvGender, tvAbout, tvHobby1, tvAge;
    private Button btnEdit;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Inisialisasi UI
        tvName = findViewById(R.id.NameProfileTV);
        tvGender = findViewById(R.id.GenderProfileTV);
        tvAbout = findViewById(R.id.AboutProfileTV);
        tvHobby1 = findViewById(R.id.Hobby1ProfileTV);
        tvAge = findViewById(R.id.AgeProfileTV);
        btnEdit = findViewById(R.id.EditProfileBtn);

        db = FirebaseFirestore.getInstance();

        // Ambil data langsung dari Firebase, bukan SharedPreferences lagi
        fetchProfileFromFirebase();

        // Tombol Edit
        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, AddProfileDetailActivity.class);
            intent.putExtra("IS_EDIT_MODE", true);
            startActivity(intent);
        });

        setupNavbar();
        showHelpDialog();
    }

    private void fetchProfileFromFirebase() {
        Log.d("PROFILE_DEBUG", "Fetching user profile from Firestore...");

        // 1. Ambil Custom ID dari SharedPreferences
        SharedPreferences prefs = getSharedPreferences("UserProfile", MODE_PRIVATE);
        String currentUserId = prefs.getString("saved_id", null);

        if (currentUserId == null || currentUserId.isEmpty()) {
            Log.e("PROFILE_DEBUG", "No user ID found in SharedPreferences! User needs to login.");
            return;
        }

        Log.d("PROFILE_DEBUG", "Looking for document with ID: " + currentUserId);

        db.collection("user").document(currentUserId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot doc = task.getResult();
                        if (doc.exists()) {
                            Log.d("PROFILE_DEBUG", "Profile data found!");

                            String savedName = doc.getString("name");

                            Boolean isMale = doc.getBoolean("gender");
                            String savedGender = (isMale != null && isMale) ? "Male" : "Female";

                            String savedDob = doc.getString("dob");
                            String savedAddress = doc.getString("domicile");
                            String savedReligion = doc.getString("religion");

                            List<String> hobbies = (List<String>) doc.get("hobbies");
                            String savedHobby = (hobbies != null && !hobbies.isEmpty()) ? hobbies.get(0) : "";

                            if (savedName == null) savedName = "No Name";
                            if (savedDob == null) savedDob = "";
                            if (savedAddress == null) savedAddress = "-";
                            if (savedReligion == null) savedReligion = "-";

                            tvName.setText(savedName);
                            tvGender.setText("Gender : " + savedGender);

                            if (!savedHobby.isEmpty()) {
                                tvHobby1.setText(savedHobby);
                                tvHobby1.setVisibility(View.VISIBLE);
                            } else {
                                tvHobby1.setVisibility(View.GONE);
                            }

                            String aboutText = "Hi! I am " + savedName + ".\n" +
                                    "Currently living in " + savedAddress + ".\n" +
                                    "Born on " + (savedDob.isEmpty() ? "-" : savedDob) + ".\n" +
                                    "Religion: " + savedReligion + ".";
                            tvAbout.setText(aboutText);

                            if (!savedDob.isEmpty()) {
                                String ageString = calculateAge(savedDob);
                                tvAge.setText("Age : " + ageString);
                                tvAge.setVisibility(View.VISIBLE);
                            } else {
                                tvAge.setVisibility(View.GONE);
                            }
                        } else {
                            Log.d("PROFILE_DEBUG", "No such document exists in Firestore!");
                        }
                    } else {
                        Log.e("PROFILE_DEBUG", "Failed to fetch profile", task.getException());
                    }
                });
    }

    private String calculateAge(String dobString) {
        try {
            Date birthDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(dobString);

            if (birthDate == null) {
                return "-";
            }

            Calendar dob = Calendar.getInstance();
            dob.setTime(birthDate);

            Calendar today = Calendar.getInstance();

            int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);

            if (today.get(Calendar.MONTH) < dob.get(Calendar.MONTH)) {
                age--;
            } else if (today.get(Calendar.MONTH) == dob.get(Calendar.MONTH) && today.get(Calendar.DAY_OF_MONTH) < dob.get(Calendar.DAY_OF_MONTH)) {
                age--;
            }

            if (age < 0) {
                age = 0;
            }

            return String.valueOf(age);
        } catch (Exception e) {
            return "-";
        }

    }

    private void setupNavbar(){
        ImageView navChat = findViewById(R.id.ChatNav);
        ImageView navHistory = findViewById(R.id.HistoryNav);
        ImageView navDiscover = findViewById(R.id.DiscoverNav);

        if (navChat != null) {
            navChat.setOnClickListener(v-> {
                Intent intent = new Intent(this, ChatActivity.class);
                startActivity(intent);
                finishAffinity();
            });
        }
        if (navHistory != null) {
            navHistory.setOnClickListener(v->{
                Intent intent = new Intent(this, HistoryActivity.class);
                startActivity(intent);
                finishAffinity();
            });
        }
        if (navDiscover != null) {
            navDiscover.setOnClickListener(v -> {
                Intent intent = new Intent(this, DiscoverActivity.class);
                startActivity(intent);
                finishAffinity();
            });
        }
    }

    private void showHelpDialog(){
        ImageView btnHelp = findViewById(R.id.btnHelp);
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
                if (tvTitle != null) tvTitle.setText("Your Profile");
                if (tvMessage != null) tvMessage.setText("This is how your profile card appears to others.");
                if (btnClose != null) {
                    btnClose.setOnClickListener(view -> dialog.dismiss());
                }
                dialog.show();
            });
        }
    }
}