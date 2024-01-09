package com.example.cookingpal

import android.os.Bundle
import android.text.Html
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
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

class RecipeDetailActivity : AppCompatActivity() {
    private lateinit var recipeDao: RecipeDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_detail)

        val recipeId = intent.getIntExtra("recipe_id", -1)

        recipeDao = AppDatabase.getDatabase(this).recipeDao()

        if (recipeId != -1) {
            fetchRecipeDetails(recipeId)
        }
    }

    private fun fetchRecipeDetails(id: Int) {
        var existingRecipe: RecipeEntity? = null
        CoroutineScope(Dispatchers.IO).launch{
            existingRecipe = recipeDao.getRecipeById(id)
            if(existingRecipe != null){
                makeNetworkRequest(id)
            } else {
                withContext(Dispatchers.Main){
                    val gson = Gson()
                    val ingredientsType = object : TypeToken<List<Product>>() {}.type
                    val ingredients: List<Product> = gson.fromJson(existingRecipe!!.ingredients, ingredientsType)
                    val recipe: Recipe = Recipe(existingRecipe!!.id, existingRecipe!!.title, existingRecipe!!.imageUrl, ingredients, existingRecipe!!.instructions)
                    updateUI(recipe)
                }
            }
        }
    }

    private fun makeNetworkRequest(id: Int) {
        val url = "https://api.spoonacular.com/recipes/$id/information?includeNutrition=false&apiKey=3520307f240e4b4e85f839761e09ffbd"

        val request = Request.Builder().url(url).build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                val strResponse = response.body()?.string()
                val jsonResponse = JSONObject(strResponse)

                val title = jsonResponse.getString("title")
                val imageUrl = if (jsonResponse.has("image")) jsonResponse.getString("image") else null

                var instructions: String = ""
                if(jsonResponse.has("summary")){
                instructions = jsonResponse.getString("summary")
                }
                instructions = Html.fromHtml(instructions, Html.FROM_HTML_MODE_LEGACY).toString()

                if (jsonResponse.has("extendedIngredients")) {
                    val ingredients = jsonResponse.getJSONArray("extendedIngredients")
                    val ingredientsList = mutableListOf<Product>()

                    for (i in 0 until ingredients.length()) {
                        val ingredient = ingredients.getJSONObject(i)
                        val name = ingredient.getString("name")
                        val measures = ingredient.getJSONObject("measures")
                        val metric = measures.getJSONObject("metric")
                        val amount = metric.getDouble("amount")
                        val unit = metric.getString("unitShort")
                        ingredientsList.add(Product(0, "$name: $amount $unit"))
                    }

                    runOnUiThread {
                        updateUI(Recipe(id, title, imageUrl, ingredientsList, instructions))
                    }
                }
            }
        })
    }

    private fun updateUI(recipe: Recipe) {
        val titleTextView = findViewById<TextView>(R.id.textViewTitle)
        titleTextView.text = recipe.title

        val ingredientsTextView = findViewById<TextView>(R.id.textViewIngredients)
        ingredientsTextView.text = recipe.ingredients.map{it.name}.toString()

        val imageView = findViewById<ImageView>(R.id.imageView)
        if (recipe.imageUrl != null) {
            Glide.with(this@RecipeDetailActivity).load(recipe.imageUrl).into(imageView)
        } else {
            imageView.setImageResource(R.drawable.default_image)
        }

        val instructionsTextView = findViewById<TextView>(R.id.textViewInstructions)
        instructionsTextView.text = recipe.instructions
    }
}