package com.healthbuzz.healthbuzz.Retrofit;

import com.healthbuzz.healthbuzz.data.model.Community;
import com.healthbuzz.healthbuzz.data.model.LoggedInUser;
import com.healthbuzz.healthbuzz.data.model.TodayData;
import com.healthbuzz.healthbuzz.data.model.TodayStretching;
import com.healthbuzz.healthbuzz.data.model.TodayWater;
import com.healthbuzz.healthbuzz.data.model.User;
import com.healthbuzz.healthbuzz.data.model.YearData;

import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface RetrofitAPI {

    @POST("/api/signin/")
    Call<LoggedInUser> postSignIn(@Body User user);

    @POST("/api/signup/")
    Call<LoggedInUser> postSignUp(@Body User user);

    @GET("/api/signout/")
    Call<Void> getSignOut();

    @GET("/api/today/")
    Call<TodayData> getTodayData();

    @GET("/api/today/refresh/")
    Call<TodayData> getTodayRefresh();

    @POST("/api/today/stretching/")
    Call<TodayData> postTodayStretching(@Body TodayStretching todayStretching);

    @POST("/api/today/water/")
    Call<TodayData> postTodayWater(@Body TodayWater todayWater);

    ////

    @GET("/api/year/stretching/")
    Call<List<YearData>> getYearStretching();

    @GET("/api/year/water/")
    Call<List<YearData>> getYearWater();

    //// For community Dashboard

    @GET("/api/today/community/")
    Call<Community> getCommunity();

}
