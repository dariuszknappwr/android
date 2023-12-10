package com.example.cookingpal

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface RecipeApi {
    @GET("recipes/complexSearch")
    fun getRecipes(
        @Query("apiKey") apiKey: String,
        @Query("number") number: Int
    ): Call<RecipeResponse>

    @GET("recipes")
    fun getVegetarianRecipes(
        @Query("apiKey") apiKey: String,
        @Query("number") number: Int,
        @Query("cuisine") cuisine: String = "vegetarian"
    ): Call<RecipeResponse>
}