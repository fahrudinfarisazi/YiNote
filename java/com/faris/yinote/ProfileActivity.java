package com.faris.yinote;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private ImageView ivProfile;
    private EditText etName, etPhone;
    private Button btnSaveProfile, btnLogout;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private Uri imageUri; // hanya lokal

    // Picker gambar (LOCAL ONLY)
    private final ActivityResultLauncher<String> imagePicker =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    imageUri = uri;
                    ivProfile.setImageURI(uri); // preview saja
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        ivProfile = findViewById(R.id.ivProfile);
        etName = findViewById(R.id.etName);
        etPhone = findViewById(R.id.etPhone);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);
        btnLogout = findViewById(R.id.btnLogout);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (mAuth.getCurrentUser() == null) {
            finish();
            return;
        }

        String uid = mAuth.getCurrentUser().getUid();

        // LOAD PROFILE TEXT
        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        etName.setText(doc.getString("name"));
                        etPhone.setText(doc.getString("phone"));
                    }
                });

        // PILIH FOTO (LOCAL)
        ivProfile.setOnClickListener(v ->
                imagePicker.launch("image/*")
        );

        // SAVE TEXT PROFILE
        btnSaveProfile.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();

            if (name.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "Nama & nomor wajib diisi", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, Object> data = new HashMap<>();
            data.put("name", name);
            data.put("phone", phone);

            db.collection("users")
                    .document(uid)
                    .set(data)
                    .addOnSuccessListener(unused ->
                            Toast.makeText(this, "Profile tersimpan", Toast.LENGTH_SHORT).show()
                    )
                    .addOnFailureListener(e ->
                            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show()
                    );
        });

        // LOGOUT
        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }
}
