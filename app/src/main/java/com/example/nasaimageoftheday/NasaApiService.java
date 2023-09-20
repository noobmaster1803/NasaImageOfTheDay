package com.example.nasaimageoftheday;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface NasaApiService {
    @GET("planetary/apod")
    Call<NasaImageResponse> getImageOfTheDay(@Query("api_key") String apiKey);
}
