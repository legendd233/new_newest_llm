package com.example.new_newest_llm.worker;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.example.new_newest_llm.data.repository.FeedRepository;

/**
 * WorkManager 后台任务：定时拉取最新资讯写入 Room。
 * 系统自动选择合适时机执行（充电/WiFi/Idle），不保证精确间隔。
 */
public class FeedWorker extends Worker {

    private static final String TAG = "FeedWorker";

    public FeedWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "Periodic feed fetch started");
        FeedRepository repository = new FeedRepository(getApplicationContext());
        int count = repository.fetchFeedBlocking();
        Log.d(TAG, "Periodic feed fetch done: " + count + " new items");
        return Result.success();
    }
}
