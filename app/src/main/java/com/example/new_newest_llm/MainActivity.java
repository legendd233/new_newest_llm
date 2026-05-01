package com.example.new_newest_llm;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.new_newest_llm.data.local.AppDatabase;
import com.example.new_newest_llm.data.local.ItemEntity;
import com.example.new_newest_llm.data.repository.FeedRepository;
import com.example.new_newest_llm.utils.LocaleHelper;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView rvNews;
    private NewsAdapter newsAdapter;
    private List<ItemEntity> newsList = new ArrayList<>();
    private FeedRepository repository;
    private String lastLocale;

    // 底部导航栏
    private TextView navHome, navFavorites, navProfile;
    private TextView btnLang;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.wrapContext(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lastLocale = LocaleHelper.getLanguage(this);
        setContentView(R.layout.activity_main);

        rvNews = findViewById(R.id.rv_news);
        navHome = findViewById(R.id.nav_home);
        navFavorites = findViewById(R.id.nav_favorites);
        navProfile = findViewById(R.id.nav_profile);
        btnLang = findViewById(R.id.btn_lang);
        btnLang.setText(LocaleHelper.getToggleLabel(this));
        btnLang.setOnClickListener(v -> {
            LocaleHelper.toggleLanguage(this);
            LocaleHelper.applyLocale(this);
            recreate();
        });

        rvNews.setLayoutManager(new LinearLayoutManager(this));

        repository = new FeedRepository(this);

        newsAdapter = new NewsAdapter(this, newsList, (item, position) -> {
            // 切换收藏状态
            boolean newState = !item.isFavorited;
            repository.toggleFavorite(item.id, newState, () -> {
                String msg = newState ? getString(R.string.favorite_saved) : getString(R.string.favorite_removed);
                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
            });
        }, (item, position) -> {
            // 按需翻译：发送整个 summary_en（描述 + README）去翻译
            String text = item.summaryEn;
            if (text == null || text.trim().isEmpty()) {
                // 没有英文内容可翻译
                String noContent = LocaleHelper.isChinese(MainActivity.this) ? "没有可翻译的英文内容" : "No English content to translate";
                Toast.makeText(MainActivity.this, noContent, Toast.LENGTH_SHORT).show();
                return;
            }
            newsAdapter.setTranslating(position, true);
            repository.translate(text, new FeedRepository.TranslateCallback() {
                @Override
                public void onResult(String translated) {
                    newsAdapter.setTranslated(position, translated);
                }
                @Override
                public void onError(String error) {
                    newsAdapter.clearTranslating(position);
                    Toast.makeText(MainActivity.this, error, Toast.LENGTH_SHORT).show();
                }
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
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        // 首次拉取真实数据
        fetchData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 语言切换后重建页面
        if (!LocaleHelper.getLanguage(this).equals(lastLocale)) {
            recreate();
            return;
        }
        // 从收藏页回来时，刷新收藏状态
        fetchData();
    }

    private void fetchData() {
        repository.fetchFeed(() -> {
            // LiveData 自动更新 UI
        });
    }
}