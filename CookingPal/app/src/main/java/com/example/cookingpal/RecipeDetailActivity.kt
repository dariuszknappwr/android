package com.example.cookingpal

import android.os.Bundle
import android.text.Html
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class RecipeDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_detail)

        // Get the passed recipe id
        val recipeId = intent.getIntExtra("recipe_id", -1)

        if (recipeId != -1) {
            fetchRecipeDetails(recipeId)
        }
    }

    private fun fetchRecipeDetails(id: Int) {
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

                var instructions = jsonResponse.getString("summary")
                instructions = Html.fromHtml(instructions, Html.FROM_HTML_MODE_LEGACY).toString()

                if (jsonResponse.has("extendedIngredients")) {
                    val ingredients = jsonResponse.getJSONArray("extendedIngredients")
                    val ingredientsList = mutableListOf<String>()

                    for (i in 0 until ingredients.length()) {
                        val ingredient = ingredients.getJSONObject(i)
                        val name = ingredient.getString("name")
                        val measures = ingredient.getJSONObject("measures")
                        val metric = measures.getJSONObject("metric")
                        val amount = metric.getDouble("amount")
                        val unit = metric.getString("unitShort")
                        ingredientsList.add("$name: $amount $unit")
                    }

                    runOnUiThread {
                        val titleTextView = findViewById<TextView>(R.id.textViewTitle)
                        titleTextView.text = title

                        val ingredientsTextView = findViewById<TextView>(R.id.textViewIngredients)
                        ingredientsTextView.text = ingredientsList.toString()

                        val imageView = findViewById<ImageView>(R.id.imageView)
                        if (imageUrl != null) {
                            Glide.with(this@RecipeDetailActivity).load(imageUrl).into(imageView)
                        } else {
                            imageView.setImageResource(R.drawable.default_image)
                        }

                        val instructionsTextView = findViewById<TextView>(R.id.textViewInstructions)
                        instructionsTextView.text = instructions
                    }
                }
            }
        })
    }
}