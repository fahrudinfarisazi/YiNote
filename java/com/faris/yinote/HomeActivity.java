package com.faris.yinote;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity {

    // ===== VIEW =====
    private TextView tvTime, tvLocationValue, tvTodoProgress;
    private EditText etSearch;
    private RecyclerView rvNotes, rvTodo;
    private FloatingActionButton fabAddNote;

    // ===== FIREBASE =====
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    // ===== LOCATION =====
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;

    // ===== DATA =====
    private final List<Note> noteList = new ArrayList<>();
    private final List<Todo> todoList = new ArrayList<>();
    private NoteAdapter noteAdapter;
    private TodoAdapter todoAdapter;

    // ===== REALTIME CLOCK =====
    private final Handler timeHandler = new Handler();
    private Runnable timeRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // INIT VIEW
        tvTime = findViewById(R.id.tvTime);
        tvLocationValue = findViewById(R.id.tvLocationValue);
        tvTodoProgress = findViewById(R.id.tvTodoProgress);
        etSearch = findViewById(R.id.etSearch);
        rvNotes = findViewById(R.id.rvNotes);
        rvTodo = findViewById(R.id.rvTodo);
        fabAddNote = findViewById(R.id.fabAddNote);

        // INIT FIREBASE
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // INIT LOCATION
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // START REALTIME CLOCK
        startRealtimeClock();

        // LOCATION
        checkLocationPermission();

        // NOTE LIST
        noteAdapter = new NoteAdapter(noteList);
        rvNotes.setLayoutManager(new LinearLayoutManager(this));
        rvNotes.setAdapter(noteAdapter);

        // TODO LIST
        todoAdapter = new TodoAdapter(todoList);
        rvTodo.setLayoutManager(new LinearLayoutManager(this));
        rvTodo.setAdapter(todoAdapter);

        // FAB ADD NOTE
        fabAddNote.setOnClickListener(v ->
                startActivity(new Intent(this, NoteActivity.class))
        );

        // SEARCH NOTE
        etSearch.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            public void afterTextChanged(Editable s) {}
            public void onTextChanged(CharSequence s, int st, int b, int c) {
                filterNotes(s.toString());
            }
        });

        // SWIPE DELETE NOTE
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            public boolean onMove(@NonNull RecyclerView r, @NonNull RecyclerView.ViewHolder v, @NonNull RecyclerView.ViewHolder t) { return false; }
            public void onSwiped(@NonNull RecyclerView.ViewHolder vh, int dir) {
                int pos = vh.getAdapterPosition();
                db.collection("notes").document(noteList.get(pos).id).delete();
                noteList.remove(pos);
                noteAdapter.notifyItemRemoved(pos);
            }
        }).attachToRecyclerView(rvNotes);

        // SWIPE DELETE TODO
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            public boolean onMove(@NonNull RecyclerView r, @NonNull RecyclerView.ViewHolder v, @NonNull RecyclerView.ViewHolder t) { return false; }
            public void onSwiped(@NonNull RecyclerView.ViewHolder vh, int dir) {
                db.collection("todos").document(todoList.get(vh.getAdapterPosition()).id).delete();
                loadTodos();
            }
        }).attachToRecyclerView(rvTodo);

        // BOTTOM NAV
        BottomNavigationView nav = findViewById(R.id.bottomNavigationView);
        nav.setOnItemSelectedListener(i -> {
            if (i.getItemId() == R.id.nav_todo)
                startActivity(new Intent(this, ToDoListActivity.class));
            else if (i.getItemId() == R.id.nav_profile)
                startActivity(new Intent(this, ProfileActivity.class));
            return true;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadNotes();
        loadTodos();
        checkLocationPermission();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timeHandler.removeCallbacks(timeRunnable);
    }

    // ===== REALTIME CLOCK =====
    private void startRealtimeClock() {
        timeRunnable = () -> {
            tvTime.setText(
                    new SimpleDateFormat("HH:mm", Locale.getDefault())
                            .format(new Date())
            );
            timeHandler.postDelayed(timeRunnable, 1000);
        };
        timeHandler.post(timeRunnable);
    }

    // ===== LOAD NOTES =====
    private void loadNotes() {
        noteList.clear();
        db.collection("notes")
                .whereEqualTo("userId", mAuth.getCurrentUser().getUid())
                .get()
                .addOnSuccessListener(q -> {
                    for (QueryDocumentSnapshot d : q) {
                        noteList.add(new Note(
                                d.getId(),
                                d.getString("title"),
                                d.getString("content"),
                                d.getLong("createdAt")
                        ));
                    }
                    noteAdapter.notifyDataSetChanged();
                });
    }

    // ===== LOAD TODOS =====
    private void loadTodos() {
        todoList.clear();
        final int[] done = {0};

        db.collection("todos")
                .whereEqualTo("userId", mAuth.getCurrentUser().getUid())
                .get()
                .addOnSuccessListener(q -> {
                    for (QueryDocumentSnapshot d : q) {
                        boolean isDone = Boolean.TRUE.equals(d.getBoolean("isDone"));
                        todoList.add(new Todo(d.getId(), d.getString("title"), isDone));
                        if (isDone) done[0]++;
                    }
                    int total = todoList.size();
                    tvTodoProgress.setText(
                            "Todo Progress: " +
                                    (total == 0 ? 0 : done[0] * 100 / total) + "%"
                    );
                    todoAdapter.notifyDataSetChanged();
                });
    }

    // ===== SEARCH =====
    private void filterNotes(String q) {
        List<Note> f = new ArrayList<>();
        for (Note n : noteList)
            if (n.title.toLowerCase().contains(q.toLowerCase())) f.add(n);
        noteAdapter.update(f);
    }

    // ===== LOCATION =====
    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        else getLocation();
    }

    private void getLocation() {
        fusedLocationClient.getLastLocation().addOnSuccessListener(l -> {
            if (l == null) {
                tvLocationValue.setText("Lokasi belum tersedia");
                return;
            }
            try {
                Geocoder g = new Geocoder(this, new Locale("id", "ID"));
                Address a = g.getFromLocation(l.getLatitude(), l.getLongitude(), 1).get(0);
                tvLocationValue.setText(
                        a.getSubAdminArea() + ", " + a.getAdminArea()
                );
            } catch (Exception e) {
                tvLocationValue.setText("Lokasi tidak tersedia");
            }
        });
    }

    // ===== RELATIVE TIME =====
    private String formatRelativeTime(long millis) {
        long now = System.currentTimeMillis();
        long diff = now - millis;
        long oneDay = 24 * 60 * 60 * 1000;

        if (diff < oneDay) {
            return "Hari ini, " +
                    new SimpleDateFormat("HH:mm", Locale.getDefault())
                            .format(new Date(millis));
        } else if (diff < 2 * oneDay) {
            return "Kemarin, " +
                    new SimpleDateFormat("HH:mm", Locale.getDefault())
                            .format(new Date(millis));
        } else {
            return new SimpleDateFormat("dd MMM yyyy, HH:mm",
                    Locale.getDefault()).format(new Date(millis));
        }
    }

    // ===== MODEL =====
    static class Note {
        String id, title, content;
        long createdAt;
        Note(String i, String t, String c, Long time) {
            id = i; title = t; content = c;
            createdAt = time != null ? time : 0;
        }
    }

    static class Todo {
        String id, title;
        boolean done;
        Todo(String i, String t, boolean d) { id = i; title = t; done = d; }
    }

    // ===== NOTE ADAPTER =====
    class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.VH> {
        List<Note> l;
        NoteAdapter(List<Note> l) { this.l = l; }
        void update(List<Note> n) { l = n; notifyDataSetChanged(); }

        public VH onCreateViewHolder(ViewGroup p, int v) {
            View view = LayoutInflater.from(p.getContext())
                    .inflate(R.layout.item_note, p, false);
            return new VH(view);
        }

        public void onBindViewHolder(VH h, int p) {
            Note n = l.get(p);
            h.title.setText(n.title);
            h.content.setText(n.content);
            h.date.setText(formatRelativeTime(n.createdAt));
        }

        public int getItemCount() { return l.size(); }

        class VH extends RecyclerView.ViewHolder {
            TextView title, content, date;
            VH(View v) {
                super(v);
                title = v.findViewById(R.id.tvNoteTitle);
                content = v.findViewById(R.id.tvNoteContent);
                date = v.findViewById(R.id.tvNoteDate);
            }
        }
    }

    // ===== TODO ADAPTER =====
    class TodoAdapter extends RecyclerView.Adapter<TodoAdapter.VH> {
        List<Todo> l;
        TodoAdapter(List<Todo> l) { this.l = l; }

        public VH onCreateViewHolder(ViewGroup p, int v) {
            return new VH(new CheckBox(p.getContext()));
        }

        public void onBindViewHolder(VH h, int p) {
            Todo t = l.get(p);
            h.cb.setText(t.title);
            h.cb.setChecked(t.done);
            h.cb.setOnCheckedChangeListener((b, c) ->
                    db.collection("todos").document(t.id).update("isDone", c));
        }

        public int getItemCount() { return l.size(); }

        class VH extends RecyclerView.ViewHolder {
            CheckBox cb;
            VH(View v) { super(v); cb = (CheckBox) v; }
        }
    }
}
