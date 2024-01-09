package com.example.cookingpal

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.cookingpal.databinding.ActivityFavoriteRecipesBinding

class FavoriteRecipesActivity : AppCompatActivity(), RecipeAdapter.OnRecipeClickListener {
    private lateinit var binding: ActivityFavoriteRecipesBinding
    private lateinit var recipeDao: RecipeDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavoriteRecipesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        recipeDao = AppDatabase.getDatabase(this).recipeDao()

        binding.recyclerView.layoutManager = LinearLayoutManager(this@FavoriteRecipesActivity)
        binding.recyclerView.adapter = RecipeAdapter(this@FavoriteRecipesActivity, emptyList())

        loadFavoriteRecipes()
    }

    override fun onRecipeClick(recipe: Recipe) {
        val intent = Intent(this, RecipeDetailActivity::class.java)
        intent.putExtra("recipe_id", recipe.id)
        startActivity(intent)
    }

    private fun loadFavoriteRecipes() {
        Log.d("FavoriteRecipesActivity", "loadFavoriteRecipes")
        CoroutineScope(Dispatchers.IO).launch {
            val favoriteRecipesEntities = recipeDao.getFavoriteRecipes()
            val favoriteRecipes = favoriteRecipesEntities.map {
                Recipe(it.id, it.title,it.imageUrl, listOf(Product(0,it.ingredients)), it.instructions, it.favorite)
                
            }
            Log.d("FavoriteRecipesActivity", "loadFavoriteRecipes: favoriteRecipes = $favoriteRecipes")
            withContext(Dispatchers.Main) {
                binding.recyclerView.adapter = RecipeAdapter(this@FavoriteRecipesActivity, favoriteRecipes)
            }
        }
    }
}