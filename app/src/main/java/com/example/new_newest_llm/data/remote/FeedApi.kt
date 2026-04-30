package com.example.new_newest_llm.data.remote

import com.example.new_newest_llm.data.model.FeedResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface FeedApi {

    @GET("feed")
    suspend fun getFeed(
        @Query("since") since: Int? = null,
        @Query("limit") limit: Int = 50
    ): Response<FeedResponse>
}
