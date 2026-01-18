package com.faris.yinote;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class LoadingActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 1200; // 1.2 detik

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        ImageView ivLogo = findViewById(R.id.ivLogo);

        // 🔥 Jalankan animasi fade in
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        ivLogo.startAnimation(fadeIn);

        // 🔁 Delay lalu pindah activity
        new Handler().postDelayed(() -> {

            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                // Sudah login → Home
                startActivity(new Intent(this, HomeActivity.class));
            } else {
                // Belum login → Login
                startActivity(new Intent(this, LoginActivity.class));
            }

            finish();

        }, SPLASH_DELAY);
    }
}
