package com.example.aol_blate_mobprog;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout tilEmail, tilPassword;
    private TextInputEditText etEmail, etPassword;
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 1. Inisialisasi Views
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        TextView tvRegisterLink = findViewById(R.id.tvlinktoRegister);

        // 2. Styling Link ke Halaman Register
        String text = "Don't Have an Account? Sign Up";
        SpannableString ss = new SpannableString(text);

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(true);
                ds.setColor(Color.parseColor("#FF0080"));
            }
        };

        ss.setSpan(clickableSpan, 0, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvRegisterLink.setText(ss);
        tvRegisterLink.setMovementMethod(LinkMovementMethod.getInstance());
        tvRegisterLink.setHighlightColor(Color.TRANSPARENT);

        // 3. Set Login Button Listener
        btnLogin.setOnClickListener(v -> handleLogin());

        // 4. Setup Help Dialog
        showHelpDialog();
    }

    private void handleLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Bersihkan error sebelumnya
        tilEmail.setError(null);
        tilPassword.setError(null);

        // Basic Validation
        if (email.isEmpty()) {
            tilEmail.setError("Email is required");
            return;
        }
        if (password.isEmpty()) {
            tilPassword.setError("Password is required");
            return;
        }

        // Kunci tombol saat menunggu balasan dari Firestore
        btnLogin.setEnabled(false);
        btnLogin.setText("Logging in...");

        // Call FirestoreManager untuk memeriksa kredensial
        FirestoreManager.getInstance().loginUser(email, password, new FirestoreManager.FirestoreCallback() {
            @Override
            public void onSuccess(Object result) {
                btnLogin.setEnabled(true);
                btnLogin.setText("Login");

                Toast.makeText(LoginActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();

                // Navigasi ke DiscoveryActivity
                Intent intent = new Intent(LoginActivity.this, DiscoverActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onFailure(Exception e) {
                btnLogin.setEnabled(true);
                btnLogin.setText("Login");

                Toast.makeText(LoginActivity.this, "User is not registered or incorrect password", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showHelpDialog() {
        ImageView btnHelp = findViewById(R.id.btnHelp);
        if (btnHelp != null) {
            btnHelp.setOnClickListener(v -> {
                Dialog dialog = new Dialog(this);
                dialog.setContentView(R.layout.dialog_help);
                if (dialog.getWindow() != null) {
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                }

                TextView tvTitle = dialog.findViewById(R.id.tvDialogTitle);
                TextView tvMessage = dialog.findViewById(R.id.tvDialogMessage);
                Button btnClose = dialog.findViewById(R.id.btnCloseDialog);

                if(tvTitle != null) tvTitle.setText("Login Info");
                if(tvMessage != null) tvMessage.setText("Enter your registered email and password to log in.");

                if(btnClose != null) btnClose.setOnClickListener(view -> dialog.dismiss());
                dialog.show();
            });
        }
    }
}