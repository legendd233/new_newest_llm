package com.example.new_newest_llm.data.remote;

import com.example.new_newest_llm.data.model.FeedResponse;
import com.example.new_newest_llm.data.model.OkResponse;
import com.example.new_newest_llm.data.model.TranslateRequest;
import com.example.new_newest_llm.data.model.TranslateResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface FeedApi {

    @GET("feed")
    Call<FeedResponse> getFeed(@Query("since") int since, @Query("limit") int limit);

    @GET("favorites")
    Call<FeedResponse> getFavorites();

    @POST("favorites/{id}")
    Call<OkResponse> addFavorite(@Path("id") int id);

    @DELETE("favorites/{id}")
    Call<OkResponse> removeFavorite(@Path("id") int id);

    @POST("translate")
    Call<TranslateResponse> translate(@Body TranslateRequest request);
}