package com.example.cookingpal

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

class FavoriteRecipesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFavoriteRecipesBinding
    private lateinit var recipeDao: RecipeDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("FavoriteRecipesActivity", "Activity starting")
        binding = ActivityFavoriteRecipesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val db = Room.databaseBuilder(
            applicationContext,
            RecipeDatabase::class.java, "recipe-database"
        ).build()
        recipeDao = db.recipeDao()

        loadFavoriteRecipes()
    }

    private fun loadFavoriteRecipes() {
        Log.d("FavoriteRecipesActivity", "About to launch coroutine")
        CoroutineScope(Dispatchers.IO).launch {
            val favoriteRecipesEntities = recipeDao.getFavoriteRecipes()
            Log.d("FavoriteRecipesActivity", "favoriteRecipesEntities: $favoriteRecipesEntities")
            val favoriteRecipes = favoriteRecipesEntities.map { 
                Recipe(it.id, it.title,it.imageUrl, emptyList(), "", it.favorite)
            }
            Log.d("FavoriteRecipesActivity", "favoriteRecipes: $favoriteRecipes")
            withContext(Dispatchers.Main) {
                val adapter = RecipeAdapter(favoriteRecipes)
                binding.recyclerView.layoutManager = LinearLayoutManager(this@FavoriteRecipesActivity)
                binding.recyclerView.adapter = adapter
            }
        }
    }
}