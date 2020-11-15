package com.healthbuzz.healthbuzz.Retrofit;

import com.healthbuzz.healthbuzz.data.model.LoggedInUser;
import com.healthbuzz.healthbuzz.data.model.User;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface RetrofitAPI{

    @POST("/api/signin/")
    Call<LoggedInUser> postSignIn(@Body User user);

    @POST("/api/signup/")
    Call<User> postSignUp(@Body User user);

    @GET("/api/signout/")
    Call<LoggedInUser> getSignOut();

}
