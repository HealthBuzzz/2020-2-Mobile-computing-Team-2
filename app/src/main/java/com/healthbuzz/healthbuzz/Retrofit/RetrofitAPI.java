package com.healthbuzz.healthbuzz.Retrofit;

import com.google.gson.JsonObject;
import com.healthbuzz.healthbuzz.data.model.LoggedInUser;
import com.healthbuzz.healthbuzz.data.model.TodayData;
import com.healthbuzz.healthbuzz.data.model.TodayStretching;
import com.healthbuzz.healthbuzz.data.model.TodayWater;
import com.healthbuzz.healthbuzz.data.model.User;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface RetrofitAPI{

    @POST("/api/signin/")
    Call<LoggedInUser> postSignIn(@Body User user);

    @POST("/api/signup/")
    Call<User> postSignUp(@Body User user);

    @GET("/api/signout/")
    Call<Void> getSignOut();

    @GET("/api/today/")
    Call<TodayData> getTodayData();

    @POST("/api/today/stretching/")
    Call<TodayData> postTodayStretching(@Body TodayStretching todayStretching);

    @GET("/api/today/stretching/")
    Call<List<TodayStretching>> getTodayStretching();

    @POST("/api/today/water/")
    Call<TodayData> postTodayWater(@Body TodayWater todayWater);

    @GET("/api/today/water/")
    Call<List<TodayWater>> getTodayWater();

    ////

    @GET("/api/waterdata/")
    Call<LoggedInUser> getWaterData();

    @POST("/api/waterdata/")
    Call<LoggedInUser> postWaterData();

    @GET("/api/stretchingdata/")
    Call<LoggedInUser> getStretchingData();

    @POST("/api/stretchingdata/")
    Call<LoggedInUser> postStretchingData();

    @GET("weather?")
    Call<JsonObject> getCurrentWeather(
            @Query("lat") String lat,
            @Query("lon") String lon,
            @Query("APPID") String APPID);

    @GET("onecall/timemachine?")
    Call<JsonObject> getHourlyWeather(
            @Query("lat") String lat,
            @Query("lon") String lon,
            @Query("dt") String dt,
            @Query("APPID") String APPID);
}
