package com.faris.yinote;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    // View
    private EditText etName, etPhone, etEmail, etPassword, etConfirmPassword;
    private Button btnSignUp;
    private TextView tvPrivacy;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 🔥 WAJIB ADA
        setContentView(R.layout.activity_signup);

        // Init Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Init View
        etName = findViewById(R.id.etName);
        etPhone = findViewById(R.id.etPhone);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnSignUp = findViewById(R.id.btnSignUp);
        tvPrivacy = findViewById(R.id.tvPrivacy);

        // Button Sign Up
        btnSignUp.setOnClickListener(v -> {

            String name = etName.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            // Validasi
            if (name.isEmpty() || phone.isEmpty() || email.isEmpty()
                    || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Semua data wajib diisi", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "Password tidak sama", Toast.LENGTH_SHORT).show();
                return;
            }

            // Register Firebase Auth
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult -> {

                        String uid = mAuth.getCurrentUser().getUid();

                        // Data user ke Firestore
                        Map<String, Object> user = new HashMap<>();
                        user.put("name", name);
                        user.put("phone", phone);
                        user.put("email", email);

                        db.collection("users")
                                .document(uid)
                                .set(user)
                                .addOnSuccessListener(unused -> {
                                    Toast.makeText(this, "Register berhasil", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(this, LoginActivity.class));
                                    finish();
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(this, "Gagal simpan data: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                                );

                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Register gagal: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        });

        // Privacy
        tvPrivacy.setOnClickListener(v ->
                Toast.makeText(this, "Terms & Privacy Policy", Toast.LENGTH_SHORT).show()
        );
    }
}
