package com.healthbuzz.healthbuzz.data;

import android.util.Log;

import com.healthbuzz.healthbuzz.RealtimeModel;
import com.healthbuzz.healthbuzz.Retrofit.RetrofitAPI;
import com.healthbuzz.healthbuzz.UserInfo;
import com.healthbuzz.healthbuzz.data.URL.OurURL;
import com.healthbuzz.healthbuzz.data.model.LoggedInUser;
import com.healthbuzz.healthbuzz.data.model.TodayData;
import com.healthbuzz.healthbuzz.data.model.TodayStretching;
import com.healthbuzz.healthbuzz.data.model.User;
import com.healthbuzz.healthbuzz.ui.login.LoginActivity;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
public class LoginDataSource {
    private static final String TAG = "MYAPI";
    private final String BASE_URL = OurURL.ourHome;
    public static RetrofitAPI mMyAPI;
    public static int userId = 0;
    public static int resultFlag = 0; // 0 is not yet, 1 is success, 2 is failed
    public static String name = null;


    private void initMyAPI(String baseUrl) {

        Log.d(TAG, "initMyAPI : " + baseUrl);

        /*
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
           */
        mMyAPI = LoginActivity.retrofit.create(RetrofitAPI.class);
        LoginDataSource.resultFlag = 0;
    }

    public Result<LoggedInUser> login(String email, String password) {
        initMyAPI(BASE_URL);
        User user = new User("No need", email, password, 0);
        Call<LoggedInUser> postCall = mMyAPI.postSignIn(user);

        //   This is for async.
        postCall.enqueue(new Callback<LoggedInUser>() {
            @Override
            public void onResponse(Call<LoggedInUser> call, Response<LoggedInUser> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "로그인 완료");
                    LoggedInUser postResponse = response.body();
                    assert response.body() != null;
                    LoginDataSource.userId = response.body().getId();
                    LoginDataSource.resultFlag = 1;
                    UserInfo.INSTANCE.getUserName().setValue(response.body().getDisplayName());
                    Log.d(TAG, "After setting static variable");
                    LoginDataSource.getTodayData();
                } else {
                    Log.d(TAG, "Status Code : " + response.code());
                    Log.d(TAG, response.errorBody().toString());
                    Log.d(TAG, call.request().body().toString());
                }
            }

            @Override
            public void onFailure(Call<LoggedInUser> call, Throwable t) {
                Log.d(TAG, "Fail msg : " + t.getMessage());
                LoginDataSource.resultFlag = 2;

            }
        });
        Log.d(TAG, "I need static set");
        LoggedInUser myUser =
                new LoggedInUser(
                        userId,
                        name);
        if (resultFlag == 0) {
            return new Result.Success<>(myUser);
        } else {
            return new Result.Error(new IOException("Error in login"));
        }
    }

    public static void logout() {
        Log.d(TAG, "로그아웃 시도1");
        Call<Void> postCall = mMyAPI.getSignOut();
        Log.d(TAG, "로그아웃 시도2");

        postCall.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "로그아웃 완료");
                    assert response.body() == null;
                    UserInfo.INSTANCE.getUserName().setValue("");
                    RealtimeModel.INSTANCE.getRanking().setValue(null);
                    LoginDataSource.resultFlag = 1;
                } else {
                    Log.d(TAG, "Status Code : " + response.code());
                    Log.d(TAG, response.errorBody().toString());
                    Log.d(TAG, call.request().body().toString());
                    UserInfo.INSTANCE.getUserName().setValue("");

                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.d(TAG, "Fail msg : " + t.getMessage());
            }
        });
    }
    public static void getTodayData() {
        Log.d(TAG, "get TodayData Trial");
        Call<TodayData> postCall = mMyAPI.getTodayData();
        Log.d(TAG, "로그아웃 시도2");

        postCall.enqueue(new Callback<TodayData>() {
            @Override
            public void onResponse(Call<TodayData> call, Response<TodayData> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "getTodayData 완료");
                    TodayData responseData = response.body();
                    RealtimeModel.INSTANCE.getStretching_count()
                            .setValue(new Long(responseData.getToday_stretching_count()));
                    RealtimeModel.INSTANCE.getWater_count()
                            .setValue(new Long(responseData.getToday_water_count()));
                    RealtimeModel.INSTANCE.getRanking()
                            .setValue(new Long(responseData.getToday_ranking()));
                    Log.d(TAG, "getTodayData 완료2");
                } else {
                    Log.d(TAG, "Status Code : " + response.code());
                    Log.d(TAG, response.errorBody().toString());
                    Log.d(TAG, call.request().body().toString());
                }
            }

            @Override
            public void onFailure(Call<TodayData> call, Throwable t) {
                Log.d(TAG, "Fail msg : " + t.getMessage());
            }
        });
    }
    public static void postTodayStretching() {
        Log.d(TAG, "get TodayStretching Trial");
        Date date = new Date();   // given date
        Calendar calendar = GregorianCalendar.getInstance(); // creates a new calendar instance
        calendar.setTime(date);   // assigns calendar to given date
        int hour = calendar.get(Calendar.HOUR_OF_DAY); // gets hour in 24h format
        int minute =calendar.get(Calendar.MINUTE);

        TodayStretching todayStretching = new TodayStretching(hour,minute);
        Call<TodayData> postCall = mMyAPI.postTodayStretching(todayStretching);

        postCall.enqueue(new Callback<TodayData>() {
            @Override
            public void onResponse(Call<TodayData> call, Response<TodayData> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "getTodayStretching 완료");
                    TodayData responseData = response.body();
                    RealtimeModel.INSTANCE.getStretching_count()
                            .setValue(new Long(responseData.getToday_stretching_count()));
                    RealtimeModel.INSTANCE.getWater_count()
                            .setValue(new Long(responseData.getToday_water_count()));
                    RealtimeModel.INSTANCE.getRanking()
                            .setValue(new Long(responseData.getToday_ranking()));
                    Log.d(TAG, "getTodayStretching 완료2");
                } else {
                    Log.d(TAG, "Status Code : " + response.code());
                    Log.d(TAG, response.errorBody().toString());
                    Log.d(TAG, call.request().body().toString());
                }
            }

            @Override
            public void onFailure(Call<TodayData> call, Throwable t) {
                Log.d(TAG, "Fail msg : " + t.getMessage());
            }
        });
    }
}