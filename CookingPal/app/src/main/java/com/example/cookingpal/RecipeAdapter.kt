package com.example.cookingpal

import android.content.Context
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
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

class RecipeAdapter(private val listener: OnRecipeClickListener, private var recipes: List<Recipe>) : RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>() {
    private lateinit var recipeDao: RecipeDao
    private lateinit var context: Context

    interface OnRecipeClickListener {
        fun onRecipeClick(recipe: Recipe)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        var recipe = recipes[position]
        holder.titleTextView.text = recipe.title
        Glide.with(context).load(recipe.imageUrl).into(holder.imageView)
        holder.favoriteImageView.setImageResource(
            if (recipe.favorite) R.drawable.ic_favorite else R.drawable.ic_favorite_border
        )

        holder.favoriteImageView.setOnClickListener {
            val updatedFavorite = !recipe.favorite
            recipe.favorite = updatedFavorite //update view, don't remove
            notifyItemChanged(position)
            CoroutineScope(Dispatchers.IO).launch {
                makeRequest(recipe.id) { updatedRecipe ->
                    recipe = updatedRecipe
                    recipe.favorite = updatedFavorite //update record that goes into database, don't remove
                    println(recipe)

                    saveFavorite(recipe)
                }
            }
        }

        holder.itemView.setOnClickListener {
            listener?.onRecipeClick(recipe)
        }
    }



    class RecipeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.textViewTitle)
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
        val favoriteImageView: ImageView = itemView.findViewById(R.id.favoriteImageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        context = parent.context
        val view = LayoutInflater.from(context).inflate(R.layout.item_recipe, parent, false)

        recipeDao = AppDatabase.getDatabase(context).recipeDao()
        return RecipeViewHolder(view)
    }



    override fun getItemCount() = recipes.size

    private fun saveFavorite(recipe: Recipe) {
        val ingredientsNames = recipe.ingredients.map { it.name }
        val ingredientsString = ingredientsNames.joinToString(", ")
        val recipeEntity = RecipeEntity(recipe.id, recipe.title, ingredientsString,recipe.instructions.toString(), recipe.imageUrl, recipe.favorite)
        CoroutineScope(Dispatchers.IO).launch {
            recipeDao.insertRecipe(recipeEntity)
        }
    }

    fun updateRecipes(newRecipes: List<Recipe>) {
        recipes = newRecipes
        notifyDataSetChanged()
    }

    fun makeRequest(id: Int, callback: (Recipe) -> Unit) {
        var url = "https://api.spoonacular.com/recipes/${id}/information?apiKey=3520307f240e4b4e85f839761e09ffbd"
        val request = Request.Builder().url(url).build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                val strResponse = response.body()?.string()
                println(id)
                println(url)
                println("Response: $strResponse")
                if (strResponse?.startsWith("{") == true) {
                    val jsonResponse = JSONObject(strResponse)

                    val title = jsonResponse.getString("title")
                    val imageUrl = if (jsonResponse.has("image")) jsonResponse.getString("image") else null

                    var instructions: String = ""
                    if(jsonResponse.has("instructions")){
                        instructions = jsonResponse.getString("instructions")
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
                        callback(Recipe(id, title, imageUrl, ingredientsList, instructions))
                    }
                        // rest of your code...
                } else {
                    println("Unexpected response type: $strResponse")
                }
            }
        })
    }
}