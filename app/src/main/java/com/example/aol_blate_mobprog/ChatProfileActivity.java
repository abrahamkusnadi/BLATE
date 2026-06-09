package com.example.aol_blate_mobprog;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.aol_blate_mobprog.models.Person;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ChatProfileActivity extends AppCompatActivity {

    private ImageView ivProfile, btnBack;
    private TextView tvName, tvGender, tvAbout, tvHobbies, tvAge, tvReligion, tvDomicile, tvJob;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_profile);

        // inisialisasi
        btnBack = findViewById(R.id.btnBackFromProfile);
        ivProfile = findViewById(R.id.ivProfilePic);
        tvName = findViewById(R.id.tvProfileName);
        tvGender = findViewById(R.id.tvProfileGender);
        tvAbout = findViewById(R.id.tvProfileAbout);
        tvHobbies = findViewById(R.id.tvProfileHobbies);
        tvAge = findViewById(R.id.tvProfileAge);
        tvReligion = findViewById(R.id.tvProfileReligion);
        tvDomicile = findViewById(R.id.tvProfileDomicile);
        tvJob = findViewById(R.id.tvProfileJob);

        db = FirebaseFirestore.getInstance();

        //dapet nama dari chat detail activity
        String nameToFind = getIntent().getStringExtra("userName");

        if (nameToFind != null) {
            tvName.setText(nameToFind);
            loadPersonData(nameToFind);
        } else {
            Toast.makeText(this, "No user data received", Toast.LENGTH_SHORT).show();
        }

        btnBack.setOnClickListener(v -> finish());
    }

    private void loadPersonData(String name) {
        // cari orangnya di database collection person
        db.collection("person")
                .whereEqualTo("name", name)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        // convert ke object person
                        Person p = querySnapshot.getDocuments().get(0).toObject(Person.class);
                        if (p != null) {
                            updateUI(p);
                        }
                    } else {
                        Toast.makeText(this, "User details not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Log.e("CHAT_PROFILE", "Error loading profile: " + e.getMessage()));
    }

    private void updateUI(Person p) {
        // logic gender
        String genderStr = p.isGender() ? "Male" : "Female";
        tvGender.setText("Gender: " + genderStr);
        tvAbout.setText(p.getAbout());
        tvReligion.setText("Religion: " + p.getReligion());
        tvDomicile.setText("Domicile: " + p.getDomicile());
        tvJob.setText("Current Job: " + p.getCurrent_job());

        //hitung umurnya
        String age = calculateAge(p.getDob());
        tvAge.setText("Age: " + age);

        // logic hobby
        if (p.getHobbies() != null) {
            String hobbiesStr = p.getHobbies().toString().replace("[", "").replace("]", "");
            tvHobbies.setText(hobbiesStr);
        }

        // logic image
        String profileName = p.getProfile();
        int resId = 0;
        if (profileName != null && !profileName.isEmpty()) {
            resId = getResources().getIdentifier(profileName.toLowerCase(), "drawable", getPackageName());
        }

        if (resId != 0) {
            ivProfile.setImageResource(resId);
        } else {
            ivProfile.setImageResource(R.drawable.ic_launcher_background);
        }
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
}