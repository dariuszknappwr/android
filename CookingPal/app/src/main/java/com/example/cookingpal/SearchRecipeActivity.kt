package com.example.cookingpal

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
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
    val cuisinesMap = mapOf(
        "Afrykańska" to "African",
        "Amerykańska" to "American",
        "Azjatycka" to "Asian",
        "Brytyjska" to "British",
        "Chińska" to "Chinese",
        "Francuska" to "French",
        "Grecka" to "Greek",
        "Indyjska" to "Indian",
        "Włoska" to "Italian",
        "Japońska" to "Japanese",
        "Śródziemnomorska" to "Mediterranean",
        "Meksykańska" to "Mexican",
        "Polska" to "Polish",
        "Portugalska" to "Portuguese",
        "Hiszpańska" to "Spanish",
        "Tajajska" to "Thai",
        "Wietnamska" to "Vietnamese"
        )

    private lateinit var searchView: SearchView
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchSpinner: Spinner
    private lateinit var adapter: RecipeAdapter
    private lateinit var cookingPalApp: CookingPalApp

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_recipe)

        val cuisineSpinner: Spinner = findViewById(R.id.cuisineSpinner)
        val cuisinesPl = resources.getStringArray(R.array.cuisines_pl)
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, cuisinesPl)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        cuisineSpinner.adapter = spinnerAdapter

        cuisineSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }

            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val cuisinePl = parent.getItemAtPosition(position).toString()
                val cuisineEn = cuisinesMap[cuisinePl]
            }
        }

        val intolerancesSpinner: Spinner = findViewById(R.id.intolerancesSpinner)
        val intolerances = resources.getStringArray(R.array.intolerances)
        val intolerancesAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, intolerances)
        intolerancesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        intolerancesSpinner.adapter = intolerancesAdapter

        val dietSpinner: Spinner = findViewById(R.id.dietSpinner)
        val diets = resources.getStringArray(R.array.diets)
        val dietAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, diets)
        dietAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dietSpinner.adapter = dietAdapter

        val infoIcon: ImageView = findViewById(R.id.infoIcon)
        infoIcon.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            val inflater = layoutInflater
            val dialogLayout = inflater.inflate(R.layout.dialog_with_image, null)
            val textView = dialogLayout.findViewById<TextView>(R.id.text)
            textView.text = Html.fromHtml("""
            <h6><b>Gluten Free</b></h6> Eliminating gluten means avoiding wheat, barley, rye, and other gluten-containing grains and foods made from them (or that may have been cross contaminated).\n
                                            <h6>Ketogenic<b>
            """, Html.FROM_HTML_MODE_LEGACY)
            builder.setView(dialogLayout)
            builder.show()
        }

        val searchButton = findViewById<Button>(R.id.searchRecipeButton)
        searchButton.setOnClickListener {
            val query = searchView.query.toString()
            val cuisine = cuisineSpinner.selectedItem.toString()
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
                    val cuisine = cuisineSpinner.selectedItem.toString()
                    searchRecipes(it, cuisine)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })
    }

    private fun searchRecipes(query: String, cuisinePl: String) {
        val cuisineEn = cuisinesMap[cuisinePl] ?: "Any"
        val base_url = "https://api.spoonacular.com/recipes/complexSearch?query=$query&apiKey=9091e7d35cc74b1baca0b9194fa229bc"
        var url = if (cuisineEn == "Any") base_url else "$base_url&cuisine=$cuisineEn"
        val onlyAvailableIngredientsCheckBox: CheckBox = findViewById(R.id.onlyAvailableIngredientsCheckBox)
        val intolerancesSpinner: Spinner = findViewById(R.id.intolerancesSpinner)
        val dietSpinner: Spinner = findViewById(R.id.dietSpinner)

        val intolerance = intolerancesSpinner.selectedItem.toString()
        val diet = dietSpinner.selectedItem.toString()

        if (intolerance != "None") {
            url += "&intolerances=$intolerance"
        }

        if(diet != "None") {
            url += "&diet=$diet"
        }

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
        