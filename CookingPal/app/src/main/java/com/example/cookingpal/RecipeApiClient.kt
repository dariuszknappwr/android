package com.example.cookingpal

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RecipeApiClient {
    private const val BASE_URL = "https://api.spoonacular.com/"

    val recipeApi: RecipeApi by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(RecipeApi::class.java)
    }
}
