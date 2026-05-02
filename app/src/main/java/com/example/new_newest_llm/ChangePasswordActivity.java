package com.example.new_newest_llm;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.new_newest_llm.data.model.MeResponse;
import com.example.new_newest_llm.data.model.OkResponse;
import com.example.new_newest_llm.data.model.ResetPasswordRequest;
import com.example.new_newest_llm.data.model.SecurityQuestionResponse;
import com.example.new_newest_llm.data.remote.RetrofitClient;
import com.example.new_newest_llm.ui.auth.AuthActivity;
import com.example.new_newest_llm.utils.LocaleHelper;
import com.example.new_newest_llm.utils.SecurityAnswerHelper;
import com.example.new_newest_llm.utils.TokenManager;
import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChangePasswordActivity extends AppCompatActivity {

    private TextView tvSecurityQuestion;
    private TextInputEditText etSecurityAnswer;
    private TextInputEditText etNewPassword;
    private TextInputEditText etConfirmPassword;
    private Button btnSubmit;
    private ProgressBar progressBar;
    private String username;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.wrapContext(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        username = getIntent().getStringExtra("username");
        if (username == null || username.isEmpty()) {
            fetchUsername();
        }

        tvSecurityQuestion = findViewById(R.id.tvSecurityQuestion);
        etSecurityAnswer = findViewById(R.id.etSecurityAnswer);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnSubmit = findViewById(R.id.btnSubmit);
        progressBar = findViewById(R.id.progressBar);

        btnSubmit.setOnClickListener(v -> submitChangePassword());

        loadSecurityQuestion();
    }

    private void fetchUsername() {
        executor.execute(() -> {
            try {
                retrofit2.Response<MeResponse> resp =
                        RetrofitClient.INSTANCE.getAuthApiJava().me().execute();
                if (resp.isSuccessful() && resp.body() != null) {
                    username = resp.body().getUsername();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void loadSecurityQuestion() {
        setLoading(true);
        executor.execute(() -> {
            try {
                retrofit2.Response<SecurityQuestionResponse> resp =
                        RetrofitClient.INSTANCE.getAuthApiJava().getSecurityQuestion().execute();
                if (resp.isSuccessful() && resp.body() != null) {
                    String question = getLocalizedQuestion(resp.body());
                    mainHandler.post(() -> {
                        if (isFinishing()) return;
                        tvSecurityQuestion.setText(question);
                        setLoading(false);
                    });
                } else {
                    mainHandler.post(() -> {
                        if (isFinishing()) return;
                        showError(getString(R.string.err_network));
                        setLoading(false);
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
                mainHandler.post(() -> {
                    if (isFinishing()) return;
                    showError(getString(R.string.err_network));
                    setLoading(false);
                });
            }
        });
    }

    private String getLocalizedQuestion(SecurityQuestionResponse resp) {
        String lang = Locale.getDefault().getLanguage();
        if (Locale.CHINESE.getLanguage().equals(lang)) {
            return resp.getQuestion().getZh();
        }
        return resp.getQuestion().getEn();
    }

    private void submitChangePassword() {
        String rawAnswer = etSecurityAnswer.getText().toString().trim();
        String answer = SecurityAnswerHelper.normalize(rawAnswer);
        String newPassword = etNewPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText().toString();

        // 校验密保答案
        if (answer.isEmpty()) {
            showError(getString(R.string.err_security_answer_empty));
            return;
        }
        if (answer.length() > 64) {
            showError(getString(R.string.err_security_answer_too_long));
            return;
        }

        // 校验新密码
        if (newPassword.isEmpty()) {
            showError(getString(R.string.err_password_empty));
            return;
        }
        if (newPassword.length() < 8) {
            showError(getString(R.string.err_password_too_short));
            return;
        }
        if (newPassword.length() > 64) {
            showError(getString(R.string.err_password_too_long));
            return;
        }

        // 校验确认密码
        if (!newPassword.equals(confirmPassword)) {
            showError(getString(R.string.err_confirm_password_mismatch));
            return;
        }

        if (username == null || username.isEmpty()) {
            showError(getString(R.string.err_network));
            return;
        }

        setLoading(true);
        executor.execute(() -> {
            try {
                ResetPasswordRequest req = new ResetPasswordRequest(username, answer, newPassword);
                retrofit2.Response<OkResponse> resp =
                        RetrofitClient.INSTANCE.getAuthApiJava().resetPassword(req).execute();
                if (resp.isSuccessful() && resp.body() != null) {
                    mainHandler.post(() -> {
                        if (isFinishing()) return;
                        new TokenManager(ChangePasswordActivity.this).clearToken();
                        Toast.makeText(ChangePasswordActivity.this,
                                R.string.password_changed, Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(ChangePasswordActivity.this, AuthActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    });
                } else {
                    String code = parseErrorCode(resp.errorBody() != null ? resp.errorBody().string() : null);
                    String msg = mapErrorCode(code);
                    mainHandler.post(() -> {
                        if (isFinishing()) return;
                        showError(msg);
                        setLoading(false);
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
                mainHandler.post(() -> {
                    if (isFinishing()) return;
                    showError(getString(R.string.err_network));
                    setLoading(false);
                });
            }
        });
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnSubmit.setEnabled(!loading);
        etSecurityAnswer.setEnabled(!loading);
        etNewPassword.setEnabled(!loading);
        etConfirmPassword.setEnabled(!loading);
    }

    private void showError(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private String parseErrorCode(String errorBody) {
        if (errorBody == null || errorBody.isEmpty()) return "UNKNOWN_ERROR";
        try {
            com.google.gson.Gson gson = new com.google.gson.Gson();
            com.example.new_newest_llm.data.model.ErrorResponse err =
                    gson.fromJson(errorBody, com.example.new_newest_llm.data.model.ErrorResponse.class);
            return err.getDetail();
        } catch (Exception e) {
            return "UNKNOWN_ERROR";
        }
    }

    private String mapErrorCode(String code) {
        switch (code) {
            case "INVALID_CREDENTIALS":
                return getString(R.string.err_invalid_credentials);
            case "PASSWORD_REUSED":
                return getString(R.string.err_password_reused);
            default:
                return getString(R.string.err_network);
        }
    }
}
