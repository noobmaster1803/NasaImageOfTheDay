package com.example.nasaimageoftheday;

import android.content.SharedPreferences;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NasaImageViewModel extends ViewModel {

    private static final String KEY_TITLE = "image_title";
    private static final String KEY_DATE = "image_date";
    private static final String KEY_DESCRIPTION = "image_description";
    private static final String KEY_MEDIA_URL = "image_media_url";


    private MutableLiveData<NasaImageResponse> imageData = new MutableLiveData<>();
    private String apiKey;
    private SharedPreferences sharedPreferences;

    private NasaApiService apiService;  // Added this line


    public LiveData<NasaImageResponse> getImageData() {
        return imageData;
    }

    public void init(String apiKey, SharedPreferences sharedPreferences) {
        this.apiKey = apiKey;
        this.sharedPreferences = sharedPreferences;



        fetchNasaImageData();
    }

    private void fetchNasaImageData() {

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(chain -> {
            okhttp3.Request original = chain.request();
            okhttp3.HttpUrl originalHttpUrl = original.url();

            okhttp3.HttpUrl url = originalHttpUrl.newBuilder()
                    .addQueryParameter("api_key", apiKey)
                    .build();

            okhttp3.Request.Builder requestBuilder = original.newBuilder()
                    .url(url);

            okhttp3.Request request = requestBuilder.build();
            return chain.proceed(request);
        });

        // Add logging interceptor for HTTP requests and responses
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        httpClient.addInterceptor(loggingInterceptor);





        Retrofit retrofit = new Retrofit.Builder().baseUrl("https://api.nasa.gov/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build();

        NasaApiService apiService = retrofit.create(NasaApiService.class);

        Call<NasaImageResponse> call = apiService.getImageOfTheDay(apiKey);
        call.enqueue(new Callback<NasaImageResponse>() {
            @Override
            public void onResponse(Call<NasaImageResponse> call, Response<NasaImageResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    NasaImageResponse apiResponse = response.body();
                    NasaImageResponse data = new NasaImageResponse();
                    data.setTitle(apiResponse.getTitle());
                    data.setDate(apiResponse.getDate());
                    data.setExplanation(apiResponse.getExplanation());
                    data.setUrl(apiResponse.getUrl());
                    imageData.setValue(data);
                    saveToCache(data);
                } else {
                    // Handle API error
                    imageData.setValue(null);
                }
            }

            @Override
            public void onFailure(Call<NasaImageResponse> call, Throwable t) {
                // Handle API call failure
                NasaImageResponse cachedData = loadFromCache();
                if (cachedData != null) {
                    imageData.setValue(cachedData);
                } else {
                    imageData.setValue(null);
                }
            }
        });
    }

    private void saveToCache(NasaImageResponse data) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_TITLE, data.getTitle());
        editor.putString(KEY_DATE, data.getDate());
        editor.putString(KEY_DESCRIPTION, data.getExplanation());
        editor.putString(KEY_MEDIA_URL, data.getUrl());
        editor.apply();
    }

    private NasaImageResponse loadFromCache() {
        String title = sharedPreferences.getString(KEY_TITLE, null);
        String date = sharedPreferences.getString(KEY_DATE, null);
        String description = sharedPreferences.getString(KEY_DESCRIPTION, null);
        String mediaUrl = sharedPreferences.getString(KEY_MEDIA_URL, null);

        if (title != null && date != null && description != null && mediaUrl != null) {
            NasaImageResponse data = new NasaImageResponse();
            data.setTitle(title);
            data.setDate(date);
            data.setExplanation(description);
            data.setUrl(mediaUrl);
            return data;
        }

        return null;
    }
}
