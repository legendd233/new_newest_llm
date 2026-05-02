package com.example.new_newest_llm.data.remote;

import com.example.new_newest_llm.data.model.MeResponse;
import com.example.new_newest_llm.data.model.OkResponse;
import com.example.new_newest_llm.data.model.ResetPasswordRequest;
import com.example.new_newest_llm.data.model.SecurityQuestionResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface AuthApiJava {

    @GET("security-question")
    Call<SecurityQuestionResponse> getSecurityQuestion();

    @POST("reset-password")
    Call<OkResponse> resetPassword(@Body ResetPasswordRequest body);

    @GET("me")
    Call<MeResponse> me();
}
