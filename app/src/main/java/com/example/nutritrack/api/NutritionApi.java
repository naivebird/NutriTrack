package com.example.nutritrack.api;

import com.example.nutritrack.data.model.FoodResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface NutritionApi {
    @GET("foods/search")
    Call<FoodResponse> searchFood(
            @Query("query") String query,
            @Query("api_key") String apiKey
    );
}