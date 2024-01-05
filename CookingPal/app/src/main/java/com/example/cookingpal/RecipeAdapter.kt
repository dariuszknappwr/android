package com.example.cookingpal

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class RecipeAdapter(private var recipes: List<Recipe>) : RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>() {

    // Define the listener interface
    interface OnRecipeClickListener {
        fun onRecipeClick(recipe: Recipe)
    }

    // Variable to hold the listener
    var listener: OnRecipeClickListener? = null

    class RecipeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.textViewTitle)
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_recipe, parent, false)
        return RecipeViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val recipe = recipes[position]
        holder.titleTextView.text = recipe.title
        if (recipe.imageUrl != null) {
            Glide.with(holder.itemView.context).load(recipe.imageUrl).into(holder.imageView)
        } else {
            holder.imageView.setImageResource(R.drawable.default_image) // set a default image
        }

        // Set the click listener
        holder.itemView.setOnClickListener {
            listener?.onRecipeClick(recipe)
        }
    }

    override fun getItemCount() = recipes.size

    fun updateRecipes(newRecipes: List<Recipe>) {
        recipes = newRecipes
        notifyDataSetChanged()
    }
}