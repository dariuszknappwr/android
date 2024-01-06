package com.example.cookingpal

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.Spinner
import androidx.appcompat.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
    private lateinit var searchSpinner: Spinner
    private lateinit var adapter: RecipeAdapter
    private lateinit var cookingPalApp: CookingPalApp

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_recipe)

        val searchSpinner: Spinner = findViewById(R.id.searchSpinner)
        val cuisines = resources.getStringArray(R.array.cuisines)
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, cuisines)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        searchSpinner.adapter = spinnerAdapter

        val searchButton = findViewById<Button>(R.id.searchRecipeButton)
        searchButton.setOnClickListener {
            val query = searchView.query.toString()
            val cuisine = searchSpinner.selectedItem.toString()
            searchRecipes(query, cuisine)
        }

        cookingPalApp = CookingPalApp(this, this)

        searchView = findViewById(R.id.searchView)
        recyclerView = findViewById(R.id.recyclerView)

        adapter = RecipeAdapter(listOf())
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Set the listener for the adapter
        adapter.listener = object : RecipeAdapter.OnRecipeClickListener {
            override fun onRecipeClick(recipe: Recipe) {
                val intent = Intent(this@SearchRecipeActivity, RecipeDetailActivity::class.java)
                intent.putExtra("recipe_id", recipe.id)
                startActivity(intent)
            }
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    val cuisine = searchSpinner.selectedItem.toString()
                    searchRecipes(it, cuisine)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })
    }
    
    private fun searchRecipes(query: String, cuisine: String) {
        val base_url = "https://api.spoonacular.com/recipes/complexSearch?query=$query&apiKey=9091e7d35cc74b1baca0b9194fa229bc"
        var url = if (cuisine != "Dowolna") base_url else "$base_url&cuisine=$cuisine"
        val onlyAvailableIngredientsCheckBox: CheckBox = findViewById(R.id.onlyAvailableIngredientsCheckBox)
        if (onlyAvailableIngredientsCheckBox.isChecked) {
            val productDao = AppDatabase.getDatabase(this@SearchRecipeActivity).productDao()
            productDao.getAll().observe(this, Observer { products ->
                val ingredients = products.joinToString("+") { it.name }
                url += "&includeIngredients=$ingredients"
                makeRequest(url)
            })
        } else {
            makeRequest(url)
        }
    }
    
    private fun makeRequest(url: String) {
        val request = Request.Builder().url(url).build()
    
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }
    
            override fun onResponse(call: Call, response: Response) {
                val strResponse = response.body()?.string()
                val jsonResponse = JSONObject(strResponse)
    
                if (jsonResponse.has("results")) {
                    val recipes = jsonResponse.getJSONArray("results")
                    val recipeList = mutableListOf<Recipe>()
    
                    for (i in 0 until recipes.length()) {
                        val recipeJson = recipes.getJSONObject(i)
    
                        val id = recipeJson.getInt("id")
                        val title = recipeJson.getString("title")
                        val imageUrl = if (recipeJson.has("image")) recipeJson.getString("image") else null
                    
                        val recipe = Recipe(id, title, imageUrl)
                        recipeList.add(recipe)
                    }
    
                    runOnUiThread {
                        adapter.updateRecipes(recipeList)
                    }
                }
            }
        })
    }
}
        