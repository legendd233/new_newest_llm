package com.example.new_newest_llm.data.remote

import com.example.new_newest_llm.data.model.FeedResponse
import com.example.new_newest_llm.data.model.OkResponse
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface FavoriteApi {

    @POST("favorites/{item_id}")
    suspend fun addFavorite(@Path("item_id") itemId: Int): Response<OkResponse>

    @DELETE("favorites/{item_id}")
    suspend fun removeFavorite(@Path("item_id") itemId: Int): Response<OkResponse>

    @GET("favorites")
    suspend fun getFavorites(): Response<FeedResponse>
}
