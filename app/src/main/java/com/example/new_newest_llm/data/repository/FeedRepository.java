package com.example.new_newest_llm.data.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import com.example.new_newest_llm.data.local.AppDatabase;
import com.example.new_newest_llm.data.local.ItemDao;
import com.example.new_newest_llm.data.local.ItemEntity;
import com.example.new_newest_llm.data.model.FeedItem;
import com.example.new_newest_llm.data.model.FeedResponse;
import com.example.new_newest_llm.data.remote.FeedApi;
import com.example.new_newest_llm.data.remote.RetrofitClient;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FeedRepository {

    private final FeedApi api;
    private final Context appContext;
    private final ExecutorService executor;
    private final Handler mainHandler;

    public FeedRepository(Context context) {
        this.api = RetrofitClient.INSTANCE.getFeedApi();
        this.appContext = context.getApplicationContext();
        this.executor = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    private ItemDao dao() {
        return AppDatabase.getInstance(appContext).itemDao();
    }

    public void fetchFeed(Runnable onComplete) {
        executor.execute(() -> {
            try {
                ItemDao d = dao();
                Integer maxId = d.getMaxId();
                int since = (maxId != null) ? maxId : 0;
                retrofit2.Response<FeedResponse> resp = api.getFeed(since, 50).execute();
                if (resp.isSuccessful() && resp.body() != null) {
                    List<FeedItem> items = resp.body().items;
                    if (items != null && !items.isEmpty()) {
                        List<ItemEntity> entities = new ArrayList<>();
                        for (FeedItem item : items) {
                            entities.add(ItemEntity.fromFeedItem(item));
                        }
                        d.insertAll(entities);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (onComplete != null) {
                mainHandler.post(onComplete);
            }
        });
    }

    /** 同步拉取（给 WorkManager 后台线程用） */
    public int fetchFeedBlocking() {
        try {
            ItemDao d = dao();
            Integer maxId = d.getMaxId();
            int since = (maxId != null) ? maxId : 0;
            retrofit2.Response<FeedResponse> resp = api.getFeed(since, 50).execute();
            if (resp.isSuccessful() && resp.body() != null) {
                List<FeedItem> items = resp.body().items;
                if (items != null && !items.isEmpty()) {
                    List<ItemEntity> entities = new ArrayList<>();
                    for (FeedItem item : items) {
                        entities.add(ItemEntity.fromFeedItem(item));
                    }
                    d.insertAll(entities);
                    return entities.size();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void toggleFavorite(int itemId, boolean favorite, Runnable onComplete) {
        executor.execute(() -> {
            try {
                if (favorite) {
                    retrofit2.Response<?> resp = api.addFavorite(itemId).execute();
                    if (resp.isSuccessful()) {
                        dao().setFavorited(itemId, true);
                    }
                } else {
                    retrofit2.Response<?> resp = api.removeFavorite(itemId).execute();
                    if (resp.isSuccessful()) {
                        dao().setFavorited(itemId, false);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (onComplete != null) {
                mainHandler.post(onComplete);
            }
        });
    }
}