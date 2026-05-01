package com.example.new_newest_llm;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.new_newest_llm.data.local.AppDatabase;
import com.example.new_newest_llm.data.local.ItemEntity;
import com.example.new_newest_llm.data.repository.FeedRepository;
import com.example.new_newest_llm.ui.auth.AuthActivity;
import com.example.new_newest_llm.utils.LocaleHelper;
import com.example.new_newest_llm.utils.TokenManager;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView rvNews;
    private NewsAdapter newsAdapter;
    private List<ItemEntity> newsList = new ArrayList<>();
    private FeedRepository repository;

    // 底部导航栏
    private TextView navHome, navFavorites, navProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rvNews = findViewById(R.id.rv_news);
        navHome = findViewById(R.id.nav_home);
        navFavorites = findViewById(R.id.nav_favorites);
        navProfile = findViewById(R.id.nav_profile);

        rvNews.setLayoutManager(new LinearLayoutManager(this));

        repository = new FeedRepository(this);

        newsAdapter = new NewsAdapter(this, newsList, (item, position) -> {
            // 切换收藏状态
            boolean newState = !item.isFavorited;
            repository.toggleFavorite(item.id, newState, () -> {
                String msg = newState ? "已收藏" : "已取消收藏";
                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
            });
        });

        rvNews.setAdapter(newsAdapter);

        // 加载已收藏的状态 — Room LiveData 自动带上 is_favorited
        AppDatabase.getInstance(this).itemDao().observeAll().observe(this, items -> {
            if (items != null) {
                newsAdapter.updateList(items);
            }
        });

        // 底部导航栏点击事件
        navHome.setOnClickListener(v -> {
            // 已经在首页，不做跳转
        });

        navFavorites.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, FavoritesActivity.class);
            startActivity(intent);
        });

        navProfile.setOnClickListener(v -> {
            showSettingsDialog();
        });

        // 首次拉取真实数据
        fetchData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 从收藏页回来时，刷新收藏状态
        fetchData();
    }

    // 设置对话框：语言切换 + 退出登录
    private void showSettingsDialog() {
        final String[] langValues = {LocaleHelper.LANG_SYSTEM, LocaleHelper.LANG_CHINESE, LocaleHelper.LANG_ENGLISH};
        final String[] langLabels = {
                isChinese() ? "跟随系统" : "Follow System",
                "中文",
                "English"
        };

        String currentLang = LocaleHelper.getLanguage(MainActivity.this);
        int checkedItem = 0;
        for (int i = 0; i < langValues.length; i++) {
            if (langValues[i].equals(currentLang)) {
                checkedItem = i;
                break;
            }
        }

        final int[] selectedIndex = {checkedItem};

        new AlertDialog.Builder(MainActivity.this)
                .setTitle(isChinese() ? "设置" : "Settings")
                .setSingleChoiceItems(langLabels, checkedItem, (dialog, which) -> {
                    selectedIndex[0] = which;
                })
                .setPositiveButton(isChinese() ? "切换语言" : "Switch Language", (dialog, which) -> {
                    String selected = langValues[selectedIndex[0]];
                    if (!selected.equals(LocaleHelper.getLanguage(MainActivity.this))) {
                        LocaleHelper.setLanguage(MainActivity.this, selected);
                        LocaleHelper.applyLocale(MainActivity.this);
                        // 重建 Activity 以生效
                        recreate();
                    }
                })
                .setNeutralButton(R.string.logout, (dialog, which) -> {
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle(R.string.logout)
                            .setMessage(R.string.logout_confirm)
                            .setPositiveButton(R.string.logout, (d, w) -> {
                                new TokenManager(MainActivity.this).clearToken();
                                Toast.makeText(MainActivity.this, R.string.logout_success, Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(MainActivity.this, AuthActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            })
                            .setNegativeButton(android.R.string.cancel, null)
                            .show();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private boolean isChinese() {
        return LocaleHelper.isChinese(this);
    }

    private void fetchData() {
        repository.fetchFeed(() -> {
            // LiveData 自动更新 UI
        });
    }
}