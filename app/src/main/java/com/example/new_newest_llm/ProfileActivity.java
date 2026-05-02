package com.example.new_newest_llm;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.new_newest_llm.data.model.MeResponse;
import com.example.new_newest_llm.data.remote.RetrofitClient;
import com.example.new_newest_llm.ui.auth.AuthActivity;
import com.example.new_newest_llm.utils.LocaleHelper;
import com.example.new_newest_llm.utils.TokenManager;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvUsername;
    private TextView btnChangePassword; // XML changed to TextView for style
    private TextView btnLogout;        // XML changed to TextView for style
    private TextView btnLang;
    private TextView navHome, navFavorites, navProfile;
    private String lastLocale;
    private String username = "";

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.wrapContext(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lastLocale = LocaleHelper.getLanguage(this);
        setContentView(R.layout.activity_profile);

        tvUsername = findViewById(R.id.tv_username);
        btnChangePassword = findViewById(R.id.btn_change_password);
        btnLogout = findViewById(R.id.btn_logout);
        btnLang = findViewById(R.id.btn_lang);

        btnLang.setText(LocaleHelper.getToggleLabel(this));
        btnLang.setOnClickListener(v -> {
            LocaleHelper.toggleLanguage(this);
            LocaleHelper.applyLocale(this);
            recreate();
        });

        navHome = findViewById(R.id.nav_home);
        navFavorites = findViewById(R.id.nav_favorites);
        navProfile = findViewById(R.id.nav_profile);

        navHome.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
        navFavorites.setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, FavoritesActivity.class));
            finish();
        });
        navProfile.setOnClickListener(v -> { });

        btnChangePassword.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, ChangePasswordActivity.class);
            intent.putExtra("username", username);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(ProfileActivity.this)
                    .setTitle(R.string.logout)
                    .setMessage(R.string.logout_confirm)
                    .setPositiveButton(R.string.logout, (d, w) -> {
                        new TokenManager(ProfileActivity.this).clearToken();
                        Toast.makeText(ProfileActivity.this, R.string.logout_success, Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(ProfileActivity.this, AuthActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
        });

        loadUserInfo();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!LocaleHelper.getLanguage(this).equals(lastLocale)) {
            recreate();
        }
    }

    private void loadUserInfo() {
        executor.execute(() -> {
            try {
                retrofit2.Response<MeResponse> resp = RetrofitClient.INSTANCE.getAuthApiJava().me().execute();
                if (resp.isSuccessful() && resp.body() != null) {
                    username = resp.body().getUsername();
                    mainHandler.post(() -> {
                        if (isFinishing()) return;
                        tvUsername.setText(username);
                    });
                } else {
                    mainHandler.post(() -> {
                        if (isFinishing()) return;
                        tvUsername.setText("—");
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
                mainHandler.post(() -> {
                    if (isFinishing()) return;
                    tvUsername.setText("—");
                });
            }
        });
    }
}
