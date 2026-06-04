package com.example.aol_blate_mobprog;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.aol_blate_mobprog.models.User; // Import Model User
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddProfileDetailActivity extends AppCompatActivity {

    private EditText etUsername, etDob, etAddress;
    private Spinner spHobbies, spJob, spReligion;
    private RadioGroup rgGender;
    private Button btnConfirm;

    // Sinyal edit mode & Data Registration
    private boolean isEditMode = false;
    private String regEmail, regPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_addprofiledetail);

        etUsername = findViewById(R.id.usernameInput);
        etDob = findViewById(R.id.dobInput);
        etAddress = findViewById(R.id.addressInput);
        spHobbies = findViewById(R.id.hobbiesDropdown);
        spJob = findViewById(R.id.jobDropdown);
        spReligion = findViewById(R.id.religionDropdown);
        rgGender = findViewById(R.id.genderGroup);
        btnConfirm = findViewById(R.id.confirmButton);

        // 1. Tangkap Sinyal & Data Register
        Intent intent = getIntent();
        isEditMode = intent.getBooleanExtra("IS_EDIT_MODE", false);
        regEmail = intent.getStringExtra("email");
        regPassword = intent.getStringExtra("password");

        setupSpinners();

        if(validateInput()) {
            fetchOldDataFromFirebase();
        } else {
            loadSavedData();
        }

        btnConfirm.setOnClickListener(v -> {
            if (validateInput()) {
                if (isEditMode) {
                    updateUserInFirebase();
                } else {
                    registerUserToFirebase();
                }
            }

        });

        showHelpDialog();
    }

    private void registerUserToFirebase() {
        // 1. Gather Data
        String name = etUsername.getText().toString().trim();
        String dob = etDob.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String hobby = spHobbies.getSelectedItem().toString();
        String job = spJob.getSelectedItem().toString();
        String religion = spReligion.getSelectedItem().toString();

        // Convert Gender (Male=True, Female=False)
        int selectedId = rgGender.getCheckedRadioButtonId();
        boolean isMale = true;
        if (selectedId != -1) {
            RadioButton selectedRadioButton = findViewById(selectedId);
            String genderStr = selectedRadioButton.getText().toString();
            if(genderStr.equalsIgnoreCase("Female")) isMale = false;
        }

        // 2. Create ID (Random Integer time)
        int userId = (int) (System.currentTimeMillis() / 1000);

        // 3. Create List of Hobbies (Model List<String>)
        List<String> hobbiesList = Collections.singletonList(hobby);

        // 4. Create User Object
        // Constructor: User(int id, String profile, String name, String dob, String domicile,
        //                   String current_job, String religion, String about, boolean gender,
        //                   List<String> hobbies, String email, String password)
        User newUser = new User(
                userId,
                "", // No profile pic yet
                name,
                dob,
                address,
                job,
                religion,
                "New User", // Default About
                isMale,
                hobbiesList,
                regEmail,
                regPassword
        );

        // 5. Call FirestoreManager
        FirestoreManager.getInstance().registerNewUser(newUser, new FirestoreManager.FirestoreCallback() {
            @Override
            public void onSuccess(Object result) {
                Toast.makeText(AddProfileDetailActivity.this, "Registration Successful!", Toast.LENGTH_SHORT).show();
                saveDataToMemory();
                SharedPreferences prefs = getSharedPreferences("UserProfile", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("saved_id", String.valueOf(userId));
                editor.apply();

                // Go to Discover
                Intent intentDis = new Intent(AddProfileDetailActivity.this, DiscoverActivity.class);
                intentDis.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intentDis);
                finish();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(AddProfileDetailActivity.this, "Registration Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void updateUserInFirebase() {
        String name = etUsername.getText().toString().trim();
        String dob = etDob.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String hobby = spHobbies.getSelectedItem().toString();
        String job = spJob.getSelectedItem().toString();
        String religion = spReligion.getSelectedItem().toString();

        int selectedId = rgGender.getCheckedRadioButtonId();
        boolean isMale = true;
        if (selectedId != -1) {
            RadioButton selectedRadioButton = findViewById(selectedId);
            String genderStr = selectedRadioButton.getText().toString();
            if(genderStr.equalsIgnoreCase("Female")) isMale = false;
        }

        SharedPreferences prefs = getSharedPreferences("UserProfile", MODE_PRIVATE);
        String currentUserId = prefs.getString("saved_id", "");

        if (currentUserId.isEmpty()) {
            Toast.makeText(this, "User ID not found. Please log in again.", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updatedData = new HashMap<>();
        if (!name.isEmpty()) updatedData.put("name", name);
        if (!dob.isEmpty()) updatedData.put("dob", dob);
        if (!address.isEmpty()) updatedData.put("domicile", address);

        updatedData.put("current_job", job);
        updatedData.put("religion", religion);
        updatedData.put("gender", isMale);
        updatedData.put("hobbies", Collections.singletonList(hobby));


        FirestoreManager.getInstance().updateUserData(String.valueOf(currentUserId), updatedData, new FirestoreManager.FirestoreCallback() {

            @Override
            public void onSuccess(Object result) {
                saveDataToMemory();
                Toast.makeText(AddProfileDetailActivity.this, "Profile Updated!", Toast.LENGTH_SHORT).show();
                Intent pindah = new Intent(AddProfileDetailActivity.this, ProfileActivity.class);
                pindah.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(pindah);
                finish();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(AddProfileDetailActivity.this, "Update Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

    }

    private void setupSpinners() {
        String[] hobbies = new String[]{"Traveling", "Culinary", "Gaming", "Reading", "Sports"};
        ArrayAdapter<String> hobbiesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, hobbies);
        hobbiesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spHobbies.setAdapter(hobbiesAdapter);

        ArrayAdapter<CharSequence> jobAdapter = ArrayAdapter.createFromResource(this, R.array.job_list, android.R.layout.simple_spinner_item);
        jobAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spJob.setAdapter(jobAdapter);

        ArrayAdapter<CharSequence> religionAdapter = ArrayAdapter.createFromResource(this, R.array.religion_list, android.R.layout.simple_spinner_item);
        religionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spReligion.setAdapter(religionAdapter);
    }

    private void fetchOldDataFromFirebase() {
        SharedPreferences prefs = getSharedPreferences("UserProfile", MODE_PRIVATE);
        String currentUserId = prefs.getString("saved_id", "");

        if (currentUserId.isEmpty()) return;

        FirebaseFirestore.getInstance().collection("user").document(currentUserId).get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        User oldUser = document.toObject(User.class);
                        if (oldUser != null) {
                            etUsername.setText(oldUser.getName());
                            etDob.setText(oldUser.getDob());
                            etAddress.setText(oldUser.getDomicile());

                            if (oldUser.isGender()) {
                                rgGender.check(R.id.maleOption);
                            } else {
                                rgGender.check(R.id.femaleOption);
                            }

                            if (oldUser.getHobbies() != null && !oldUser.getHobbies().isEmpty()) {
                                String hobby = oldUser.getHobbies().get(0);
                                ArrayAdapter<String> adapter = (ArrayAdapter<String>) spHobbies.getAdapter();
                                int position = adapter.getPosition(hobby);
                                if (position >= 0) spHobbies.setSelection(position);
                            }

                            ArrayAdapter<CharSequence> jobAdapter = (ArrayAdapter<CharSequence>) spJob.getAdapter();
                            int jobPosition = jobAdapter.getPosition(oldUser.getCurrent_job());
                            if (jobPosition >= 0) spJob.setSelection(jobPosition);

                            ArrayAdapter<CharSequence> religionAdapter = (ArrayAdapter<CharSequence>) spReligion.getAdapter();
                            int religionPosition = religionAdapter.getPosition(oldUser.getReligion());
                            if (religionPosition >= 0) spReligion.setSelection(religionPosition);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load old data", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadSavedData() {
        SharedPreferences prefs = getSharedPreferences("UserProfile", MODE_PRIVATE);

        String savedName = prefs.getString("saved_name", "");
        String savedDob = prefs.getString("saved_dob", "");
        String savedAddress = prefs.getString("saved_address", "");
        String savedGender = prefs.getString("saved_gender", "Male");
        String savedHobby = prefs.getString("saved_hobby", "");
        String savedReligion = prefs.getString("saved_religion", "");

        if (!savedName.equals("No Name") && !savedName.isEmpty()) {
            etUsername.setText(savedName);
            etDob.setText(savedDob);
            etAddress.setText(savedAddress);

            if (savedGender.equalsIgnoreCase("Female")) {
                rgGender.check(R.id.femaleOption);
            } else {
                rgGender.check(R.id.maleOption);
            }

            if (!savedHobby.isEmpty()) {
                ArrayAdapter adapter = (ArrayAdapter) spHobbies.getAdapter();
                int position = adapter.getPosition(savedHobby);
                if (position >= 0) spHobbies.setSelection(position);
            }

            if (!savedReligion.isEmpty()) {
                ArrayAdapter adapter = (ArrayAdapter) spReligion.getAdapter();
                int position = adapter.getPosition(savedReligion);
                if (position >= 0) spReligion.setSelection(position);
            }
        }
    }

    private boolean validateInput() {
        if (isEditMode) {
            return true;
        }
        if (etUsername.getText().toString().isEmpty()) {
            etUsername.setError("Username required");
            return false;
        }
        if (etDob.getText().toString().isEmpty()) {
            etDob.setError("Date of Birth required");
            return false;
        }
        return true;
    }

    private void saveDataToMemory() {
        String name = etUsername.getText().toString();
        String dob = etDob.getText().toString();
        String address = etAddress.getText().toString();
        String hobby = spHobbies.getSelectedItem().toString();
        String religion = spReligion.getSelectedItem().toString();

        int selectedId = rgGender.getCheckedRadioButtonId();
        String gender = "Male";
        if (selectedId != -1) {
            RadioButton selectedRadioButton = findViewById(selectedId);
            gender = selectedRadioButton.getText().toString();
        }

        SharedPreferences prefs = getSharedPreferences("UserProfile", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        if (!name.isEmpty()) editor.putString("saved_name", name);
        if (!dob.isEmpty()) editor.putString("saved_dob", dob);
        if (!address.isEmpty()) editor.putString("saved_address", address);
        editor.putString("saved_hobby", hobby);
        editor.putString("saved_gender", gender);
        editor.putString("saved_religion", religion);

        editor.apply();
    }

    private void showHelpDialog(){
        ImageView btnHelp = findViewById(R.id.helpIcon);
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

                if (tvTitle != null) tvTitle.setText("Profile Details");
                if (tvMessage != null) tvMessage.setText("Please fill in your personal information accurately.");
                if (btnClose != null) btnClose.setOnClickListener(view -> dialog.dismiss());
                dialog.show();
            });
        }
    }
}
