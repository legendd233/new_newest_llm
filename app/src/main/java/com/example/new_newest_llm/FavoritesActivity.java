package com.example.new_newest_llm;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
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

public class FavoritesActivity extends AppCompatActivity {

    private RecyclerView rvFavorites;
    private TextView tvEmpty;
    private ImageView ivBack;
    private TextView btnLang;
    private NewsAdapter newsAdapter;
    private List<ItemEntity> favoriteList = new ArrayList<>();
    private FeedRepository repository;
    private String lastLocale;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.wrapContext(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lastLocale = LocaleHelper.getLanguage(this);
        setContentView(R.layout.activity_favorites);

        rvFavorites = findViewById(R.id.rv_favorites);
        tvEmpty = findViewById(R.id.tv_empty);
        ivBack = findViewById(R.id.iv_back);
        btnLang = findViewById(R.id.btn_lang);
        btnLang.setText(LocaleHelper.getToggleLabel(this));
        btnLang.setOnClickListener(v -> {
            LocaleHelper.toggleLanguage(this);
            LocaleHelper.applyLocale(this);
            recreate();
        });

        rvFavorites.setLayoutManager(new LinearLayoutManager(this));

        repository = new FeedRepository(this);

        // 返回按钮
        ivBack.setOnClickListener(v -> finish());

        // 设置适配器
        newsAdapter = new NewsAdapter(this, favoriteList, (item, position) -> {
            // 在收藏界面点击"取消收藏"
            repository.toggleFavorite(item.id, false, () -> {
                Toast.makeText(FavoritesActivity.this, R.string.favorite_removed, Toast.LENGTH_SHORT).show();
            });
        }, (item, position) -> {
            // 按需翻译
            String text = item.summaryEn;
            if (text == null || text.trim().isEmpty()) {
                String noContent = LocaleHelper.isChinese(FavoritesActivity.this) ? "没有可翻译的英文内容" : "No English content to translate";
                Toast.makeText(FavoritesActivity.this, noContent, Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(FavoritesActivity.this, error, Toast.LENGTH_SHORT).show();
                }
            });
        });

        rvFavorites.setAdapter(newsAdapter);

        // 加载收藏的数据 — Room 实时观察 is_favorited=1 的条目
        AppDatabase.getInstance(this).itemDao().observeFavorites().observe(this, items -> {
            if (items != null) {
                newsAdapter.updateList(items);
                // 检查是否显示空状态
                checkEmpty(items);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!LocaleHelper.getLanguage(this).equals(lastLocale)) {
            recreate();
        }
    }

    private void checkEmpty(List<ItemEntity> items) {
        if (items == null || items.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            rvFavorites.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            rvFavorites.setVisibility(View.VISIBLE);
        }
    }
}