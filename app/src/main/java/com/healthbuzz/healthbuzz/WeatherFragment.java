package com.healthbuzz.healthbuzz;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.healthbuzz.healthbuzz.Retrofit.RetrofitAPI;
import com.healthbuzz.healthbuzz.data.LoginDataSource;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class WeatherFragment extends Fragment {
    // TODO: Rename and change types of parameters
    private final String TAG = "Weather Frag";
    private final String OPEN_WEATHER_MAP_KEY = "6a7cef582a7b2dcb6dd7fa7a76cb053c";
    private String latitude = "37.48";
    private String longitude = "126.95";
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    ArrayList<String> temps = new ArrayList<>();
    ArrayList<String> datetimes = new ArrayList<>();
    ArrayList<String> weather_icons = new ArrayList<>();

    private String current_temp = "";
    private String current_dt = "";
    private String current_weather = "";
    private String current_weather_icon = "";
    private String current_city = "";
    private ImageView imageView;
    private TextView textView_city;
    private TextView textView_temp;
    private TextView textView_weather;

    public WeatherFragment() {
        // Required empty public constructor
    }

    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void startLocationPermissionRequest() {
        requestPermissions(
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_PERMISSIONS_REQUEST_CODE);
    }

    private void askPermissions() {
        boolean shouldProvideRationale =
                shouldShowRequestPermissionRationale(
                        Manifest.permission.ACCESS_COARSE_LOCATION);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");

            Snackbar.make(getActivity().findViewById(android.R.id.content), "SnackBar 나와라", Snackbar.LENGTH_LONG)
                    .setAction("Action", view -> {
                        // Request permission
                        startLocationPermissionRequest();
                    }).show();

        } else {
            Log.i(TAG, "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            startLocationPermissionRequest();
        }
    }


    public int getWeather() {
        getLocation();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.openweathermap.org/data/2.5/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RetrofitAPI myAPI = retrofit.create(RetrofitAPI.class);
        long unixTime = System.currentTimeMillis() / 1000L;
        Log.d("time", String.valueOf(unixTime));
        Call<JsonObject> weatherCall = myAPI.getCurrentWeather(latitude, longitude, OPEN_WEATHER_MAP_KEY);
        Call<JsonObject> weatherHourlyCall = myAPI.getHourlyWeather(latitude, longitude, String.valueOf(unixTime), OPEN_WEATHER_MAP_KEY);

        weatherCall.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NotNull Call<JsonObject> call, @NotNull Response<JsonObject> response) {
                //JSONObject jsonObj = new JSONObject(response.body().toString());
                JsonObject response_body = response.body();
                String res = response_body.toString();
                Log.d("weather", res);


                //Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
                Geocoder geocoder = new Geocoder(getActivity(), Locale.US);
                // Address found using the Geocoder.
                List<Address> addresses = null;
                try {
                    // Using getFromLocation() returns an array of Addresses for the area immediately
                    // surrounding the given latitude and longitude. The results are a best guess and are
                    // not guaranteed to be accurate.
                    // In this sample, we get just a single address.
                    addresses = geocoder.getFromLocation(Double.parseDouble(latitude), Double.parseDouble(longitude), 1);
                } catch (IOException ioException) {
                    Log.e(TAG, "Failed to ", ioException);
                    return;
                } catch (IllegalArgumentException illegalArgumentException) {
                    Log.e(TAG, "Fialed to ", illegalArgumentException);
                    return;
                }

                Address address = addresses.get(0);
                current_city = address.getSubLocality();
                //current_city = address.getAddressLine(0);
                Log.d("weather_city", current_city);
                JsonObject current_weather_obj = response.body().getAsJsonArray("weather").get(0).getAsJsonObject();
                Log.d("curr_weather_obj", current_weather_obj.toString());
                current_weather = current_weather_obj.get("main").toString();
                current_weather_icon = current_weather_obj.get("icon").getAsString();
                Log.d("curr_weather_icon", current_weather_icon);
                Log.d("curr_weather", current_weather);
                current_temp = "" + String.format("%.1f", (response.body().getAsJsonObject("main").get("temp").getAsFloat() - 273f)) + "°C";
                updateViews();
            }

            @Override
            public void onFailure(@NotNull Call<JsonObject> call, @NotNull Throwable t) {
                Log.d("weather", "Fail msg : " + t.getMessage());
                LoginDataSource.resultFlag = 2;

            }
        });

        // TODO: show hourly weathers
        weatherHourlyCall.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NotNull Call<JsonObject> call, @NotNull Response<JsonObject> response_hourly) {
                //JSONObject jsonObj = new JSONObject(response.body().toString());
                System.out.println(response_hourly.body());
                String res_hourly = response_hourly.body().toString();
                Log.d("weather_Hour", res_hourly);
                JsonArray weather_array = response_hourly.body().getAsJsonArray("hourly");
               /* for (int i = 0; i < weather_array.size(); i++) {
                    JsonElement weather = weather_array.get(i);
                    JsonObject weather_obj = weather.getAsJsonObject();
                    Log.d("weather_parse", weather.toString());
                    temps.add("" + (weather_obj.get("temp").getAsFloat() - 273f));
                    datetimes.add(weather_obj.get("dt").toString());
                    weather_icons.add(weather_obj.getAsJsonArray("weather").get(0).getAsJsonObject().get("icon").getAsString());
                }*/
                Log.d("weather_temps", temps.toString());
                Log.d("weather_icons", weather_icons.toString());
                updateViews();
            }

            @Override
            public void onFailure(@NotNull Call<JsonObject> call, @NotNull Throwable t) {
                Log.d("weather_Hour", "Fail msg : " + t.getMessage());
                LoginDataSource.resultFlag = 2; // FIXME

            }
        });

        return 1;
        /*
        if(resultFlag == 0) {
            return new Result.Success<>(myUser);
        }else{
            return new Result.Error(new IOException("Error in login"));
        }
        */
    }

    private void updateViews() {
        String day_weather_icon = "";
        if(current_weather_icon != "")
        {
            Log.d("weather", current_weather_icon);
            day_weather_icon = current_weather_icon.substring(0,2) + "d";
        }
        //String weather_icon_string = "https://openweathermap.org/img/wn/" + current_weather_icon + "@2x.png";
        String weather_icon_string = "https://openweathermap.org/img/wn/" + day_weather_icon + "@2x.png";
        Glide.with(this).load(weather_icon_string).into(imageView);
        textView_city.setText(current_city);
        textView_temp.setText(current_temp);
        textView_weather.setText(current_weather);
    }

    private void getLocation() {
        Log.d("getlocation", "getlocation");
        Log.d("permission", String.valueOf(ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED));
        LocationManager locationManager;
        LocationListener locationListener;

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                latitude = String.valueOf(location.getLatitude());
                longitude = String.valueOf(location.getLongitude());
                //Log.d("latitude",latitude);
                //Log.d("latitude",longitude);
            }
        };

        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.d("locationlisten", "locationlisten");
            locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, 0, 0, locationListener);
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        //locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, 0, 0, locationListener);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ViewGroup weatherView = (ViewGroup) inflater.inflate(R.layout.fragment_weather, container, false);
        int result = getWeather();
        imageView = (ImageView) weatherView.findViewById(R.id.imageView_weather);
        textView_city = (TextView) weatherView.findViewById(R.id.textView_city);
        textView_temp = (TextView) weatherView.findViewById(R.id.textView_temp);
        textView_weather = (TextView) weatherView.findViewById(R.id.textView_weather);
        Log.d("curr_weather_icon", current_weather_icon);
        updateViews();
        return weatherView;
    }
}