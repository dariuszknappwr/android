package com.example.cookingpal

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.util.Log
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
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class SearchRecipeActivity : AppCompatActivity() {
    val cuisinesMap: Map<String, String> = mapOf(
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
    private lateinit var adapter: RecipeAdapter
    private lateinit var cookingPalApp: CookingPalApp
    private val recipeListLiveData = MutableLiveData<List<Recipe>>()
    private val recipeList = mutableListOf<Recipe>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_recipe)

        val cuisinesPl = resources.getStringArray(R.array.cuisines_pl)
        val checkedCuisines = BooleanArray(cuisinesPl.size)
        val selectedCuisines = mutableListOf<String>()
        val cuisineButton: Button = findViewById(R.id.cuisineButton)
        cuisineButton.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Wybierz kuchnię")
                .setMultiChoiceItems(cuisinesPl, checkedCuisines) { _, which, isChecked ->
                    checkedCuisines[which] = isChecked
                }
                .setPositiveButton("OK") { _, _ ->
                    selectedCuisines.clear()
                    for (i in checkedCuisines.indices) {
                        if (checkedCuisines[i]) {
                            selectedCuisines.add(cuisinesPl[i])
                        }
                    }
                }
                .setNegativeButton("Anuluj", null)
                .show()
        }

        val intolerances = resources.getStringArray(R.array.intolerances)
        val checkedIntolerances = BooleanArray(intolerances.size)
        val selectedIntolerances = mutableListOf<String>()
        val intolerancesButton: Button = findViewById(R.id.intolerancesButton)
        intolerancesButton.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Wybierz składniki, których nie chcesz używać")
                .setMultiChoiceItems(intolerances, checkedIntolerances) { _, which, isChecked ->
                    checkedIntolerances[which] = isChecked
                }
                .setPositiveButton("OK") { _, _ ->
                    selectedIntolerances.clear()
                    for (i in checkedIntolerances.indices) {
                        if (checkedIntolerances[i]) {
                            selectedIntolerances.add(intolerances[i])
                        }
                    }
                }
                .setNegativeButton("Anuluj", null)
                .show()
        }

        val diets = resources.getStringArray(R.array.diets)
        val checkedDiets = BooleanArray(diets.size)
        val selectedDiets = mutableListOf<String>()
        val dietButton: Button = findViewById(R.id.dietButton)
        dietButton.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Wybierz dietę")
                .setMultiChoiceItems(diets, checkedDiets) { _, which, isChecked ->
                    checkedDiets[which] = isChecked
                }
                .setPositiveButton("OK") { _, _ ->
                    selectedDiets.clear()
                    for (i in checkedDiets.indices) {
                        if (checkedDiets[i]) {
                            selectedDiets.add(diets[i])
                        }
                    }
                }
                .setNegativeButton("Anuluj", null)
                .show()
        }

        val infoIcon: ImageView = findViewById(R.id.infoIcon)
        infoIcon.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            val inflater = layoutInflater
            val dialogLayout = inflater.inflate(R.layout.dialog_with_image, null)
            val textView = dialogLayout.findViewById<TextView>(R.id.text)
            textView.text = getString(R.string.diets_description)
            builder.setView(dialogLayout)
            builder.show()
        }

        val searchButton = findViewById<Button>(R.id.searchRecipeButton)
        searchButton.setOnClickListener {
            val query = searchView.query.toString()
            searchRecipes(query, selectedCuisines, selectedIntolerances, selectedDiets)
        }

        cookingPalApp = CookingPalApp(this, this)

        searchView = findViewById(R.id.searchView)
        recyclerView = findViewById(R.id.recyclerView)

        val onRecipeClickListener = object : RecipeAdapter.OnRecipeClickListener {
            override fun onRecipeClick(recipe: Recipe) {
                val intent = Intent(this@SearchRecipeActivity, RecipeDetailActivity::class.java)
                intent.putExtra("recipe_id", recipe.id)
                startActivity(intent)
            }
        }

        adapter = RecipeAdapter(onRecipeClickListener, listOf())
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    searchRecipes(it, selectedCuisines, selectedIntolerances, selectedDiets)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })

        recipeListLiveData.observe(this, Observer { recipes ->
            adapter.updateRecipes(recipes)
        })
    }

    private fun searchRecipes(
        query: String,
        cuisines: List<String>,
        intolerances: List<String>,
        diets: List<String>
    ) {

        var url =
            "https://api.spoonacular.com/recipes/complexSearch?apiKey=3520307f240e4b4e85f839761e09ffbd"

        if (!query.isEmpty()) {
            url += "&query=$query"
        }

        if (cuisines.isNotEmpty()) {
            val cuisinesEn = cuisines.map { cuisinesMap[it] ?: "Any" }
            if (!cuisinesEn.contains("Any")) {
                url += "&cuisine=${cuisinesEn.joinToString(",")}"
            }
        }

        if (intolerances.isNotEmpty()) {
            url += "&intolerances=${intolerances.joinToString(",")}"
        }

        if (diets.isNotEmpty()) {
            url += "&diet=${diets.joinToString(",")}"
        }

        val onlyAvailableIngredientsCheckBox: CheckBox =
            findViewById(R.id.onlyAvailableIngredientsCheckBox)
        if (onlyAvailableIngredientsCheckBox.isChecked) {
            CoroutineScope(Dispatchers.IO).launch {
                val productDao = AppDatabase.getDatabase(this@SearchRecipeActivity).productDao()
                val products: List<Product> = productDao.getAllDirect()
                val ingredients: List<Product> = products.map { Product(it.id, it.name) }
                url += "&includeIngredients=" + ingredients.joinToString("+") { it.name }

                withContext(Dispatchers.Main) {
                    println("url = $url")
                    makeRequest(url)
                }
            }
        } else {
            makeRequest(url)
        }

        println("url = $url")
        println(onlyAvailableIngredientsCheckBox.isChecked)

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
                Log.d("SearchRecipeActivity", "jsonResponse = $jsonResponse")

                if (jsonResponse.has("results")) {
                    val recipes = jsonResponse.getJSONArray("results")
                    val recipeList = mutableListOf<Recipe>()

                    recipeList.clear()
                    for (i in 0 until recipes.length()) {
                        val recipeJson = recipes.getJSONObject(i)

                        val id = recipeJson.getInt("id")
                        val title = recipeJson.getString("title")
                        val imageUrl =
                            if (recipeJson.has("image")) recipeJson.getString("image") else null

                        val recipe = Recipe(
                            id,
                            title,
                            imageUrl
                        )
                        recipeList.add(recipe)
                    }

                    runOnUiThread {
                        recipeListLiveData.postValue(recipeList)
                        Log.d("SearchRecipeActivity", "recipeListLiveData = $recipeListLiveData")
                        adapter.updateRecipes(recipeList)
                    }
                }
            }
        })
    }
}
        