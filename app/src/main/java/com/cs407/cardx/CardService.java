package com.cs407.cardx;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface CardService {
    @GET("getCards")
    Call<CardIdsResponse> getCards(@Query("userId") String userId);

    @GET("getUserInfo")
    Call<Card> getUserInfo(@Query("userId") String userId);

    @POST("addCard")
    Call<ResponseBody> addCard(
            @Query("userId") String userId,
            @Query("cardUserId") String cardUserId
    );

    @DELETE("delCard")
    Call<ResponseBody> deleteCard(
            @Query("userId") String userId,
            @Query("cardUserId") String cardUserId
    );
}

