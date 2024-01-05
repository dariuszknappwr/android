package com.example.cookingpal

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class SearchRecipeActivity : AppCompatActivity() {

    private lateinit var searchView: SearchView
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RecipeAdapter
    private lateinit var cookingPalApp: CookingPalApp

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_recipe)

        val searchButton = findViewById<Button>(R.id.searchRecipeButton)
        searchButton.setOnClickListener {
            val query = searchView.query.toString()
            searchRecipes(query)
        }

        cookingPalApp = CookingPalApp(this, this)

        searchView = findViewById(R.id.searchView)
        recyclerView = findViewById(R.id.recyclerView)

        adapter = RecipeAdapter(listOf())
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    searchRecipes(it)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })
    }

    private fun searchRecipes(query: String) {
        val url = "https://api.spoonacular.com/recipes/complexSearch?query=$query&apiKey=9091e7d35cc74b1baca0b9194fa229bc"
        val request = Request.Builder().url(url).build()
    
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                val strResponse = response.body()?.string()
                val jsonResponse = JSONObject(strResponse)
            
                val recipes = jsonResponse.getJSONArray("results")
                val recipeList = mutableListOf<Recipe>()
            
                for (i in 0 until recipes.length()) {
                    val recipeJson = recipes.getJSONObject(i)
            
                    // Get the list of ingredients
                    val productsList = mutableListOf<Product>()
                    if (recipeJson.has("ingredients")) {
                        val ingredientsJson = recipeJson.getJSONArray("ingredients")
                        for (j in 0 until ingredientsJson.length()) {
                            productsList.add(Product(0, ingredientsJson.getString(j)))
                        }
                    }
            
                    val recipe = Recipe(
                        recipeJson.getString("title"),
                        productsList
                    )
                    recipeList.add(recipe)
                }
            
                runOnUiThread {
                    adapter.updateRecipes(recipeList)
                }
            }
        })
        }
    }
        