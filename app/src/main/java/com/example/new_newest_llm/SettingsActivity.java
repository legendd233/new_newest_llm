package com.example.new_newest_llm;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.new_newest_llm.ui.auth.AuthActivity;
import com.example.new_newest_llm.utils.LocaleHelper;
import com.example.new_newest_llm.utils.TokenManager;

public class SettingsActivity extends AppCompatActivity {

    private Button btnLogout;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.wrapContext(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        btnLogout = findViewById(R.id.btn_logout);

        btnLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(SettingsActivity.this)
                    .setTitle(R.string.logout)
                    .setMessage(R.string.logout_confirm)
                    .setPositiveButton(R.string.logout, (d, w) -> {
                        new TokenManager(SettingsActivity.this).clearToken();
                        Toast.makeText(SettingsActivity.this, R.string.logout_success, Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(SettingsActivity.this, AuthActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
        });
    }
}
