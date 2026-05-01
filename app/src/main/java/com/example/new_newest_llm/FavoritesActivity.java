package com.example.new_newest_llm;

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
import java.util.ArrayList;
import java.util.List;

public class FavoritesActivity extends AppCompatActivity {

    private RecyclerView rvFavorites;
    private TextView tvEmpty;
    private ImageView ivBack;
    private NewsAdapter newsAdapter;
    private List<ItemEntity> favoriteList = new ArrayList<>();
    private FeedRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        rvFavorites = findViewById(R.id.rv_favorites);
        tvEmpty = findViewById(R.id.tv_empty);
        ivBack = findViewById(R.id.iv_back);

        rvFavorites.setLayoutManager(new LinearLayoutManager(this));

        repository = new FeedRepository(this);

        // 返回按钮
        ivBack.setOnClickListener(v -> finish());

        // 设置适配器
        newsAdapter = new NewsAdapter(this, favoriteList, (item, position) -> {
            // 在收藏界面点击"取消收藏"
            repository.toggleFavorite(item.id, false, () -> {
                Toast.makeText(FavoritesActivity.this, "已取消收藏", Toast.LENGTH_SHORT).show();
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