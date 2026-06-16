package com.example.aol_blate_mobprog;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 101;

    // Menggunakan Material Components
    TextInputLayout tilEmail, tilPassword, tilConfirmPassword;
    TextInputEditText etEmail, etPassword, etConfirmPassword;
    CheckBox chkTerms;
    Button btnSignUp;
    ImageView btnHelp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Clear memory on startup
        SharedPreferences prefs = getSharedPreferences("UserProfile", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        checkAndRequestPermissions();

        // Initialize Views dengan ID baru yang lebih bersih
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        chkTerms = findViewById(R.id.chkTerms);
        btnSignUp = findViewById(R.id.btnSignUp);
        TextView tvLoginLink = findViewById(R.id.tvlinktoPage);

        // Styling Link to Login agar lebih menyatu dengan tema
        String text = "Already Have an Account? Login";
        SpannableString ss = new SpannableString(text);

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(true); // Garis bawah tetap ada
                ds.setColor(Color.parseColor("#E91E63")); // Warna Pink sesuai tema
            }
        };

        ss.setSpan(clickableSpan, 0, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvLoginLink.setText(ss);
        tvLoginLink.setMovementMethod(LinkMovementMethod.getInstance());
        tvLoginLink.setHighlightColor(Color.TRANSPARENT);

        // Logic Sign Up
        btnSignUp.setOnClickListener(v -> {
            Animation shake = AnimationUtils.loadAnimation(MainActivity.this, R.anim.shake);

            String emailInput = etEmail.getText().toString().trim();
            String passwordInput = etPassword.getText().toString().trim();
            String confirmPasswordInput = etConfirmPassword.getText().toString().trim();
            boolean isTermsChecked = chkTerms.isChecked();

            boolean isValid = true;

            // Bersihkan error state sebelumnya
            tilEmail.setError(null);
            tilPassword.setError(null);
            tilConfirmPassword.setError(null);

            // Validasi Email
            if (!emailInput.endsWith("@gmail.com")) {
                tilEmail.setError("Email must contain @gmail.com");
                tilEmail.startAnimation(shake);
                isValid = false;
            }

            // Validasi Password
            if (passwordInput.length() < 8) {
                tilPassword.setError("Password must be at least 8 characters");
                tilPassword.startAnimation(shake);
                isValid = false;
            }

            // Validasi Confirm Password
            if (!passwordInput.equals(confirmPasswordInput)) {
                tilConfirmPassword.setError("Passwords do not match");
                tilConfirmPassword.startAnimation(shake);
                isValid = false;
            }

            // Validasi Terms & Conditions
            if (!isTermsChecked) {
                chkTerms.startAnimation(shake);
                Toast.makeText(MainActivity.this, "You must agree to the terms and conditions", Toast.LENGTH_SHORT).show();
                isValid = false;
            }

            // Jika validasi lokal gagal, hentikan proses di sini
            if (!isValid) return;

            // --- MULAI PENGECEKAN EMAIL KE FIRESTORE ---

            // Nonaktifkan tombol sementara agar user tidak spam klik saat loading
            btnSignUp.setEnabled(false);
            btnSignUp.setText("Checking...");

            com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore.getInstance();

            db.collection("user")
                    .whereEqualTo("email", emailInput) // Cari dokumen yang emailnya sama
                    .get()
                    .addOnCompleteListener(task -> {
                        // Kembalikan tombol ke kondisi semula
                        btnSignUp.setEnabled(true);
                        btnSignUp.setText("Sign Up");

                        if (task.isSuccessful()) {
                            if (!task.getResult().isEmpty()) {
                                // Jika hasilnya TIDAK kosong, berarti email sudah terdaftar!
                                tilEmail.setError("This email is already registered!");
                                tilEmail.startAnimation(shake);
                            } else {
                                // Jika hasilnya KOSONG, email aman digunakan. Lanjut ke halaman berikutnya!
                                Intent intent = new Intent(MainActivity.this, AddProfileDetailActivity.class);
                                intent.putExtra("email", emailInput);
                                intent.putExtra("password", passwordInput);
                                startActivity(intent);

                                Toast.makeText(MainActivity.this, "Step 1 Complete! Please fill details.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // Jika query gagal (misal tidak ada internet / rules diblokir)
                            Toast.makeText(MainActivity.this, "Failed to check email. Try again.", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        showHelpDialog();
    }


    private void checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                        PERMISSION_REQUEST_CODE);
            }
        }
        else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        PERMISSION_REQUEST_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Denied. You won't be able to upload a profile picture.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void showHelpDialog() {
        btnHelp = findViewById(R.id.btnHelp);
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

                if(tvTitle != null) tvTitle.setText("Registration Info");
                if(tvMessage != null) tvMessage.setText("Create your new account here. Please use a valid email address and create a strong password (at least 8 characters).");

                if(btnClose != null) btnClose.setOnClickListener(view -> dialog.dismiss());
                dialog.show();
            });
        }
    }
}