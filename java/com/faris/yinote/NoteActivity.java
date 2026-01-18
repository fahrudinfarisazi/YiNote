package com.faris.yinote;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class NoteActivity extends AppCompatActivity {

    private EditText etTitle, etContent;
    private ImageView btnSave, btnBack;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private String noteId = null; // null = create, not null = edit

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        etTitle = findViewById(R.id.etTitle);
        etContent = findViewById(R.id.etContent);
        btnSave = findViewById(R.id.btnSave);
        btnBack = findViewById(R.id.btnBack);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        if (getIntent() != null) {
            noteId = getIntent().getStringExtra("noteId");
            etTitle.setText(getIntent().getStringExtra("title"));
            etContent.setText(getIntent().getStringExtra("content"));
        }

        btnBack.setOnClickListener(v -> finish());

        btnSave.setOnClickListener(v -> saveNote());
    }

    private void saveNote() {
        String title = etTitle.getText().toString().trim();
        String content = etContent.getText().toString().trim();

        if (title.isEmpty()) {
            Toast.makeText(this, "Judul wajib diisi", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = mAuth.getCurrentUser().getUid();

        // 🔥 WAKTU DEVICE (HP / EMULATOR)
        long now = System.currentTimeMillis();

        Map<String, Object> note = new HashMap<>();
        note.put("title", title);
        note.put("content", content);
        note.put("userId", uid);
        note.put("updatedAt", now);

        if (noteId == null) {
            // CREATE
            note.put("createdAt", now);
            db.collection("notes")
                    .add(note)
                    .addOnSuccessListener(doc -> finish())
                    .addOnFailureListener(e ->
                            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show()
                    );
        } else {
            // UPDATE
            db.collection("notes")
                    .document(noteId)
                    .update(note)
                    .addOnSuccessListener(unused -> finish())
                    .addOnFailureListener(e ->
                            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show()
                    );
        }
    }
}
