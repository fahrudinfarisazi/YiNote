package com.faris.yinote;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class NoteListActivity extends AppCompatActivity {

    Button btnAddNote;
    LinearLayout containerNotes;

    FirebaseFirestore db;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_list);

        btnAddNote = findViewById(R.id.btnAddNote);
        containerNotes = findViewById(R.id.containerNotes);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // ADD NOTE
        btnAddNote.setOnClickListener(v ->
                startActivity(new Intent(this, NoteActivity.class))
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadNotes();
    }

    private void loadNotes() {
        containerNotes.removeAllViews();

        String uid = mAuth.getCurrentUser().getUid();

        db.collection("notes")
                .whereEqualTo("userId", uid)
                .get()
                .addOnSuccessListener(query -> {
                    if (query.isEmpty()) {
                        Toast.makeText(this, "Belum ada note", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    for (QueryDocumentSnapshot doc : query) {
                        String noteId = doc.getId();
                        String title = doc.getString("title");
                        String content = doc.getString("content");

                        TextView tv = new TextView(this);
                        tv.setText(title + "\n" + content);
                        tv.setPadding(16, 16, 16, 16);
                        tv.setBackgroundColor(0xFFEFEFEF);
                        tv.setTextSize(16f);

                        // EDIT NOTE
                        tv.setOnClickListener(v -> {
                            Intent i = new Intent(this, NoteActivity.class);
                            i.putExtra("noteId", noteId);
                            i.putExtra("title", title);
                            i.putExtra("content", content);
                            startActivity(i);
                        });

                        // DELETE NOTE
                        tv.setOnLongClickListener(v -> {
                            db.collection("notes")
                                    .document(noteId)
                                    .delete()
                                    .addOnSuccessListener(unused ->
                                            Toast.makeText(this, "Note dihapus", Toast.LENGTH_SHORT).show()
                                    );
                            containerNotes.removeView(tv);
                            return true;
                        });

                        containerNotes.addView(tv);
                    }
                });
    }
}
