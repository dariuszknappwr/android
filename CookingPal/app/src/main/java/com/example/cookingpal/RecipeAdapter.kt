package com.example.cookingpal

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RecipeAdapter(private var recipes: List<Recipe>) : RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>() {
    private lateinit var recipeDao: RecipeDao
    private lateinit var context: Context

    // Define the listener interface
    interface OnRecipeClickListener {
        fun onRecipeClick(recipe: Recipe)
    }

    // Variable to hold the listener
    var listener: OnRecipeClickListener? = null

    class RecipeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.textViewTitle)
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
        val favoriteImageView: ImageView = itemView.findViewById(R.id.favoriteImageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        context = parent.context
        val view = LayoutInflater.from(context).inflate(R.layout.item_recipe, parent, false)
        val db = Room.databaseBuilder(
            context,
            RecipeDatabase::class.java, "recipe-database"
        ).build()
        recipeDao = db.recipeDao()
        return RecipeViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val recipe = recipes[position]
        holder.titleTextView.text = recipe.title
        Glide.with(context).load(recipe.imageUrl).into(holder.imageView)
        holder.favoriteImageView.setImageResource(
            if (recipe.favorite) R.drawable.ic_favorite else R.drawable.ic_favorite_border
        )

        holder.favoriteImageView.setOnClickListener {
            recipe.favorite = !recipe.favorite
            notifyItemChanged(position)
            saveFavorite(recipe)
        }

        holder.itemView.setOnClickListener {
            listener?.onRecipeClick(recipe)
        }
    }

    override fun getItemCount() = recipes.size

    private fun saveFavorite(recipe: Recipe) {
        val recipeEntity = RecipeEntity(recipe.id, recipe.title, "","", recipe.imageUrl, recipe.favorite)
        CoroutineScope(Dispatchers.IO).launch {
            recipeDao.insertRecipe(recipeEntity)
        }
    }

    fun updateRecipes(newRecipes: List<Recipe>) {
        recipes = newRecipes
        notifyDataSetChanged()
    }
}