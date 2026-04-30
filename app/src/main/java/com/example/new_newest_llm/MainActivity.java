package com.example.new_newest_llm;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.new_newest_llm.ui.auth.AuthActivity;
import com.example.new_newest_llm.utils.TokenManager;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView rvNews;
    private NewsAdapter newsAdapter;
    private List<NewsItem> newsList = new ArrayList<>();

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

        initData();
        loadFavoriteStatus(); // 加载已收藏的状态

        newsAdapter = new NewsAdapter(newsList, (news, position) -> {
            // 切换收藏状态
            news.setFavorited(!news.isFavorited());
            newsAdapter.updateList(newsList);

            // 保存到收藏夹
            if (news.isFavorited()) {
                addToFavorites(news);
                Toast.makeText(MainActivity.this, "已收藏", Toast.LENGTH_SHORT).show();
            } else {
                removeFromFavorites(news);
                Toast.makeText(MainActivity.this, "已取消收藏", Toast.LENGTH_SHORT).show();
            }
        });

        rvNews.setAdapter(newsAdapter);

        // 底部导航栏点击事件
        navHome.setOnClickListener(v -> {
            // 已经在首页，不做跳转
        });

        navFavorites.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, FavoritesActivity.class);
            startActivity(intent);
        });

        navProfile.setOnClickListener(v -> {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle(R.string.logout)
                    .setMessage(R.string.logout_confirm)
                    .setPositiveButton(R.string.logout, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            TokenManager tokenManager = new TokenManager(MainActivity.this);
                            tokenManager.clearToken();
                            Toast.makeText(MainActivity.this, R.string.logout_success, Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(MainActivity.this, AuthActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
        });
    }

    private void initData() {
        for (int i = 1; i <= 20; i++) {
            newsList.add(new NewsItem(
                    i,
                    "新闻标题 " + i,
                    "这是第 " + i + " 条新闻的摘要内容，会在这里显示两行文字，超过的部分会被截断...",
                    "2024-04-" + (i < 10 ? "0" + i : i),
                    false
            ));
        }
    }

    // 加载已收藏的新闻ID，恢复收藏状态
    private void loadFavoriteStatus() {
        SharedPreferences prefs = getSharedPreferences("favorites", MODE_PRIVATE);
        String json = prefs.getString("favorite_list", "");
        if (!json.isEmpty()) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<NewsItem>>() {}.getType();
            List<NewsItem> savedFavorites = gson.fromJson(json, type);
            if (savedFavorites != null) {
                for (NewsItem favorite : savedFavorites) {
                    for (NewsItem news : newsList) {
                        if (news.getId() == favorite.getId()) {
                            news.setFavorited(true);
                            break;
                        }
                    }
                }
            }
        }
    }

    private void addToFavorites(NewsItem news) {
        List<NewsItem> favorites = getFavoritesList();
        // 检查是否已存在
        boolean exists = false;
        for (NewsItem item : favorites) {
            if (item.getId() == news.getId()) {
                exists = true;
                break;
            }
        }
        if (!exists) {
            favorites.add(news);
            saveFavoritesList(favorites);
        }
    }

    private void removeFromFavorites(NewsItem news) {
        List<NewsItem> favorites = getFavoritesList();
        favorites.removeIf(item -> item.getId() == news.getId());
        saveFavoritesList(favorites);
    }

    private List<NewsItem> getFavoritesList() {
        SharedPreferences prefs = getSharedPreferences("favorites", MODE_PRIVATE);
        String json = prefs.getString("favorite_list", "");
        if (!json.isEmpty()) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<NewsItem>>() {}.getType();
            List<NewsItem> list = gson.fromJson(json, type);
            if (list != null) {
                return list;
            }
        }
        return new ArrayList<>();
    }

    private void saveFavoritesList(List<NewsItem> favorites) {
        SharedPreferences prefs = getSharedPreferences("favorites", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(favorites);
        editor.putString("favorite_list", json);
        editor.apply();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 从收藏页回来时，刷新收藏状态
        loadFavoriteStatus();
        newsAdapter.updateList(newsList);
    }
}
