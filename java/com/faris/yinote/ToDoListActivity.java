package com.faris.yinote;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

public class ToDoListActivity extends AppCompatActivity {

    // VIEW
    private EditText etTodo;
    private Button btnAddTodo;
    private LinearLayout containerTodo;

    // FIREBASE
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo_list);

        // INIT VIEW
        etTodo = findViewById(R.id.etTodo);
        btnAddTodo = findViewById(R.id.btnAddTodo);
        containerTodo = findViewById(R.id.containerTodo);

        // INIT FIREBASE
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // TAMBAH TODO
        btnAddTodo.setOnClickListener(v -> addTodo());

        // LOAD TODO SAAT MASUK
        loadTodos();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTodos();
    }

    // ===== TAMBAH TODO =====
    private void addTodo() {
        String text = etTodo.getText().toString().trim();

        if (text.isEmpty()) {
            Toast.makeText(this, "Todo tidak boleh kosong", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = mAuth.getCurrentUser().getUid();

        Map<String, Object> todo = new HashMap<>();
        todo.put("title", text);
        todo.put("isDone", false);
        todo.put("userId", uid);
        todo.put("createdAt", System.currentTimeMillis());

        db.collection("todos")
                .add(todo)
                .addOnSuccessListener(doc -> {
                    etTodo.setText("");
                    loadTodos(); // refresh list
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    // ===== LOAD TODO =====
    private void loadTodos() {
        containerTodo.removeAllViews();

        String uid = mAuth.getCurrentUser().getUid();

        db.collection("todos")
                .whereEqualTo("userId", uid)
                .get()
                .addOnSuccessListener(query -> {

                    for (QueryDocumentSnapshot doc : query) {

                        String title = doc.getString("title");
                        boolean isDone = Boolean.TRUE.equals(doc.getBoolean("isDone"));

                        android.widget.CheckBox cb = new android.widget.CheckBox(this);
                        cb.setText(title);
                        cb.setChecked(isDone);
                        cb.setTextSize(16f);
                        cb.setPadding(16, 16, 16, 16);

                        // UPDATE STATUS
                        cb.setOnCheckedChangeListener((buttonView, checked) -> {
                            doc.getReference().update("isDone", checked);
                        });

                        // 🔥 HAPUS TODO (TEKAN LAMA)
                        cb.setOnLongClickListener(v -> {
                            doc.getReference().delete()
                                    .addOnSuccessListener(unused -> {
                                        Toast.makeText(this, "Todo dihapus", Toast.LENGTH_SHORT).show();
                                        loadTodos();
                                    });
                            return true;
                        });

                        containerTodo.addView(cb);
                    }
                });
    }
}
