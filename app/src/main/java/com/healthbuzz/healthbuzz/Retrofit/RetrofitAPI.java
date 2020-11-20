package com.healthbuzz.healthbuzz.Retrofit;

import com.healthbuzz.healthbuzz.data.model.LoggedInUser;
import com.healthbuzz.healthbuzz.data.model.User;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface RetrofitAPI {

    @POST("/api/signin/")
    Call<LoggedInUser> postSignIn(@Body User user);

    @POST("/api/signup/")
    Call<User> postSignUp(@Body User user);

    @GET("/api/signout/")
    Call<Void> getSignOut();

    @GET("/api/waterdata/")
    Call<LoggedInUser> getWaterData();

    @POST("/api/waterdata/")
    Call<LoggedInUser> postWaterData();

    @GET("/api/stretchingdata/")
    Call<LoggedInUser> getStretchingData();

    @POST("/api/stretchingdata/")
    Call<LoggedInUser> postStretchingData();

}
