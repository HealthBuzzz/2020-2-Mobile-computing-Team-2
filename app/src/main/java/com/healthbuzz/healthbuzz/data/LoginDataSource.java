package com.healthbuzz.healthbuzz.data;

import android.content.ContentValues;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;

import com.healthbuzz.healthbuzz.HTTP.RequestHttpURLConnection;
import com.healthbuzz.healthbuzz.Retrofit.RetrofitAPI;
import com.healthbuzz.healthbuzz.data.URL.OurURL;
import com.healthbuzz.healthbuzz.data.model.LoggedInUser;
import com.healthbuzz.healthbuzz.data.model.User;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
public class LoginDataSource {
    private static final String TAG = "MYAPI";
    private final String BASE_URL = OurURL.ourHome;
    private RetrofitAPI mMyAPI;
    public static boolean loggedIn = false;
    public static String name = null;


    private void initMyAPI(String baseUrl){

        Log.d(TAG,"initMyAPI : " + baseUrl);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        mMyAPI = retrofit.create(RetrofitAPI.class);
    }
    public Result<LoggedInUser> login(String email, String password) {
        initMyAPI(BASE_URL);
        Log.d(TAG,"POST");

        User user = new User("No need", email, password);
        Call<User> postCall = mMyAPI.postSignIn(user);
        postCall.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if(response.isSuccessful()){
                    Log.d(TAG,"로그인 완료");
                    assert response.body() != null;
                    LoginDataSource.loggedIn = true;
                    LoginDataSource.name = response.body().getUsername();
                }else {
                    Log.d(TAG,"Status Code : " + response.code());
                    Log.d(TAG,response.errorBody().toString());
                    Log.d(TAG,call.request().body().toString());
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.d(TAG,"Fail msg : " + t.getMessage());
            }
        });
        SystemClock.sleep(2000);
        LoggedInUser fakeUser =
                new LoggedInUser(
                        java.util.UUID.randomUUID().toString(),
                        name);
        if(loggedIn) {
            return new Result.Success<>(fakeUser);
        }else{
            return new Result.Error(new IOException("Error in login"));
        }
    }

    public void logout() {
        // TODO: revoke authentication
    }
    public static String loginPOST(String url, User person){
        InputStream is = null;
        String result = "";
        try {
            URL urlCon = new URL(url);
            HttpURLConnection httpCon = (HttpURLConnection)urlCon.openConnection();
            String json = "";
            // build jsonObject
            JSONObject jsonObject = new JSONObject();
            jsonObject.accumulate("email", person.getEmail());
            jsonObject.accumulate("password", person.getPassword());
            // convert JSONObject to JSON to String
            json = jsonObject.toString();
            // ** Alternative way to convert Person object to JSON string usin Jackson Lib
            // ObjectMapper mapper = new ObjectMapper();
            // json = mapper.writeValueAsString(person);
            // Set some headers to inform server about the type of the content
            httpCon.setRequestProperty("Accept", "application/json");
            httpCon.setRequestProperty("Content-type", "application/json");
            // OutputStream으로 POST 데이터를 넘겨주겠다는 옵션.
            httpCon.setDoOutput(true);
            // InputStream으로 서버로 부터 응답을 받겠다는 옵션.
            httpCon.setDoInput(true);
            Log.i("tttt","1");
            OutputStream os = null;
            os = httpCon.getOutputStream();
            Log.i("tttt","2");
            os.write(json.getBytes("euc-kr"));

            Log.i("tttt","3");
            os.flush();
            Log.i("tttt","4");
            // receive response as inputStream
            try {
                is = httpCon.getInputStream();
                // convert inputstream to string
                if(is != null)
                    result = convertInputStreamToString(is);
                else
                    result = "Did not work!";
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            finally {
                httpCon.disconnect();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
        }
        return result;
    }

    private static String convertInputStreamToString(InputStream is) {
        //creating an InputStreamReader object
        InputStreamReader isReader = new InputStreamReader(is);
        //Creating a BufferedReader object
        BufferedReader reader = new BufferedReader(isReader);
        StringBuffer sb = new StringBuffer();
        String str;
        try {
            while ((str = reader.readLine()) != null) {
                sb.append(str);
            }
        } catch(IOException e){
            Log.i("error","convertInputStreamToString in LoginDataSource error");
        }
        return sb.toString();
    }
}