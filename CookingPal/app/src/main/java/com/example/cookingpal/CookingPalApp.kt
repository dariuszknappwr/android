package com.example.cookingpal

import android.app.Activity
import android.content.Intent
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import java.util.*
import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.widget.TextView
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class CookingPalApp(private val activity: Activity, context: Context) {
    private val ingredients: MutableList<Ingredient> = mutableListOf()
    private val recipes: MutableList<Recipe> = mutableListOf()
    private val favorites: MutableList<Recipe> = mutableListOf()

    private val database: AppDatabase = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "cooking_pal_database"
    ).build()

    private val recipeDao: RecipeDao = database.recipeDao()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            recipeDao.insertRecipe(RecipeEntity(title = "Przykładowy Przepis", ingredients = "Składnik 1, Składnik 2", instructions = "Kroki"))
        }
    }


    fun scanBarcode() {
        val integrator = IntentIntegrator(activity)
        integrator.setPrompt("Zeskanuj kod kreskowy")
        integrator.setBeepEnabled(true)
        integrator.setOrientationLocked(false)
        integrator.initiateScan()
    }

fun handleScanResult(result: IntentResult?) {
    if (result != null) {
        val scannedContent = result.contents
        println("Zeskanowano kod kreskowy: $scannedContent")

        // Send a request to the UPCItemDB API
        val url = "https://api.upcitemdb.com/prod/trial/lookup?upc=$scannedContent"
        val request = Request.Builder().url(url).build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")

                    val responseData = response.body()?.string()
                    val jsonObject = JSONObject(responseData)

                    // Extract the product name from the JSON response
                    val itemsArray = jsonObject.getJSONArray("items")
                    var productName = ""
                    if (itemsArray.length() == 0) {
                        productName = "Nie znaleziono produktu."                        
                    } else{
                        val firstItemObject = itemsArray.getJSONObject(0)
                        productName = firstItemObject.getString("title")
                    }

                    // Start a new Activity
                    val intent = Intent(activity, ScanResultActivity::class.java)
                    intent.putExtra("productName", productName)
                    activity.startActivity(intent)
                }
            }
        })
    }
}

    fun addIngredient(name: String) {
        val ingredient = Ingredient(name = name)
        ingredients.add(ingredient)
        println("$name dodano do składników.")
    }

    fun searchRecipes(category: String): List<Recipe> {
        println("Wyszukiwanie przepisów w kategorii: $category")
        return emptyList() // Pusta lista - do zaimplementowania
    }

    fun viewRecipeDetails(recipe: Recipe) {
        println("Szczegóły przepisu: ${recipe.getTitle()}")
        println("Składniki: ${recipe.getIngredients().joinToString(", ")}")
        println("Instrukcje: ${recipe.getInstructions()}")
    }

    fun addToFavorites(recipe: Recipe) {
        favorites.add(recipe)
        println("${recipe.getTitle()} dodano do ulubionych.")
    }
}




