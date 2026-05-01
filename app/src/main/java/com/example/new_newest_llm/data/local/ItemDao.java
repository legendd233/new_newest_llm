package com.example.new_newest_llm.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import java.util.List;

@Dao
public interface ItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<ItemEntity> items);

    @Query("SELECT * FROM item ORDER BY display_rank ASC")
    LiveData<List<ItemEntity>> observeAll();

    @Query("SELECT * FROM item WHERE is_favorited = 1 ORDER BY display_rank ASC")
    LiveData<List<ItemEntity>> observeFavorites();

    @Query("SELECT MAX(id) FROM item")
    Integer getMaxId();

    @Query("UPDATE item SET is_favorited = :favorited WHERE id = :itemId")
    void setFavorited(int itemId, boolean favorited);

    @Query("DELETE FROM item")
    void clearAll();
}