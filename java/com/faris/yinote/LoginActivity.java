package com.faris.yinote;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin, btnGoogleLogin;
    private TextView tvSignUp, tvPrivacy;

    private FirebaseAuth mAuth; // 🔥 Firebase Auth

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Init Firebase
        mAuth = FirebaseAuth.getInstance();

        // Init views
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoogleLogin = findViewById(R.id.btnGoogleLogin);
        tvSignUp = findViewById(R.id.tvSignUp);
        tvPrivacy = findViewById(R.id.tvPrivacy);

        // LOGIN EMAIL
        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email dan password wajib diisi", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult -> {
                        Toast.makeText(this, "Login berhasil", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, HomeActivity.class));
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Login gagal: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        });

        // GOOGLE LOGIN (sementara NONAKTIF)
        btnGoogleLogin.setOnClickListener(v ->
                Toast.makeText(this, "Google Login belum diaktifkan", Toast.LENGTH_SHORT).show()
        );

        // KE REGISTER
        tvSignUp.setOnClickListener(v ->
                startActivity(new Intent(this, SignUpActivity.class))
        );

        // PRIVACY
        tvPrivacy.setOnClickListener(v ->
                Toast.makeText(this, "Privacy Policy", Toast.LENGTH_SHORT).show()
        );
    }
}
