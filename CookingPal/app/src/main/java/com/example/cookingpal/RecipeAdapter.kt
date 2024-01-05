package com.example.cookingpal

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RecipeAdapter(private var recipes: List<Recipe>) : RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>() {

    class RecipeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.textViewTitle)
        val ingredientsTextView: TextView = itemView.findViewById(R.id.textViewIngredients)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_recipe, parent, false)
        return RecipeViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val currentRecipe = recipes[position]
        holder.titleTextView.text = currentRecipe.title
        var ingredients: String = ""
        for (i in currentRecipe.products) {
            val lastIngredient = currentRecipe.products[currentRecipe.products.lastIndex]
            ingredients += i.name.toString()
            if(i != lastIngredient)
                ingredients += ", "
        }
        holder.ingredientsTextView.text = ingredients.toString()
    }

    override fun getItemCount(): Int {
        return recipes.size
    }

    fun updateRecipes(newRecipes: List<Recipe>){
        recipes = newRecipes
        notifyDataSetChanged()
    }
}
