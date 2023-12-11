package com.example.cookingpal

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RecipeListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var recipeAdapter: RecipeAdapter
    private var apiRecipes: List<Recipe> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_list)

        recyclerView = findViewById(R.id.recyclerViewRecipes)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recipeAdapter = RecipeAdapter(emptyList())
        recyclerView.adapter = recipeAdapter


        var call: Call<RecipeResponse>
        call = RecipeApiClient.recipeApi.getRecipes("9091e7d35cc74b1baca0b9194fa229bc", 1)
        //call = RecipeApiClient.recipeApi.getVegetarianRecipes("9091e7d35cc74b1baca0b9194fa229bc", 1)
        call.enqueue(object : Callback<RecipeResponse> {
            override fun onResponse(call: Call<RecipeResponse>, response: Response<RecipeResponse>) {
                if (response.isSuccessful) {
                    val recipes = response.body()?.results
                    if (recipes != null) {
                        apiRecipes = recipes.toList()

                    }
                } else {
                    // błąd
                }
                updateUIWithRecipes()
            }

            override fun onFailure(call: Call<RecipeResponse>, t: Throwable) {
                updateUIWithRecipes()
            }
        })
        updateUIWithRecipes()
    }

    private fun updateUIWithRecipes() {
        val sampleRecipes = getSampleRecipesString()
        recipeAdapter.updateRecipes(sampleRecipes)
    }

    private fun getSampleRecipesString(): List<Recipe> {
        val recipies = mutableListOf(
            Recipe("Spaghetti Bolognese",
                listOf(Ingredient("Pasta"), Ingredient("Meat"), Ingredient("Tomato Sauce")),
                "Cook pasta. Brown meat. Add sauce."),
            Recipe("Caesar Salad",
                listOf(Ingredient("Lettuce"), Ingredient("Chicken"), Ingredient("Croutons")),
                "Mix ingredients. Add dressing.")
        )

        if (apiRecipes.isNotEmpty()) {
            val apiRecipe = apiRecipes.first()
            recipies +=  Recipe(apiRecipe.getTitle().toString(), listOf(), "")
                    Recipe(
                        apiRecipe.getTitle().toString(),
                        listOf(Ingredient("Pasta"), Ingredient("Meat"), Ingredient("Tomato Sauce")),
                        "Mix everything"
                    )
        }
        return recipies
    }
}

