package com.example.new_newest_llm;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class FavoritesActivity extends AppCompatActivity {

    private RecyclerView rvFavorites;
    private TextView tvEmpty;
    private ImageView ivBack;
    private NewsAdapter newsAdapter;
    private List<NewsItem> favoriteList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        rvFavorites = findViewById(R.id.rv_favorites);
        tvEmpty = findViewById(R.id.tv_empty);
        ivBack = findViewById(R.id.iv_back);

        rvFavorites.setLayoutManager(new LinearLayoutManager(this));

        // 返回按钮
        ivBack.setOnClickListener(v -> finish());

        // 加载收藏的数据
        loadFavorites();

        // 设置适配器
        newsAdapter = new NewsAdapter(favoriteList, (news, position) -> {
            // 在收藏界面点击"取消收藏"
            news.setFavorited(false);
            // 从当前列表中移除
            favoriteList.remove(position);
            newsAdapter.updateList(favoriteList);
            // 更新存储
            saveFavorites();
            // 检查是否显示空状态
            checkEmpty();
        });

        rvFavorites.setAdapter(newsAdapter);
        checkEmpty();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 每次回到这个页面时刷新数据
        loadFavorites();
        newsAdapter.updateList(favoriteList);
        checkEmpty();
    }

    private void loadFavorites() {
        SharedPreferences prefs = getSharedPreferences("favorites", MODE_PRIVATE);
        String json = prefs.getString("favorite_list", "");
        if (!json.isEmpty()) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<NewsItem>>() {}.getType();
            favoriteList = gson.fromJson(json, type);
            if (favoriteList == null) {
                favoriteList = new ArrayList<>();
            }
        } else {
            favoriteList = new ArrayList<>();
        }
    }

    private void saveFavorites() {
        SharedPreferences prefs = getSharedPreferences("favorites", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(favoriteList);
        editor.putString("favorite_list", json);
        editor.apply();
    }

    private void checkEmpty() {
        if (favoriteList == null || favoriteList.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            rvFavorites.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            rvFavorites.setVisibility(View.VISIBLE);
        }
    }
}
