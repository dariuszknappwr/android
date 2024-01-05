package com.example.cookingpal

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.integration.android.IntentIntegrator
import com.example.cookingpal.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var cookingApp: CookingPalApp

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cookingApp = CookingPalApp(this, this)

        binding.scanButton.setOnClickListener {
            cookingApp.scanBarcode()
        }

        binding.addIngredientButton.setOnClickListener {
            val intent = Intent(this, AddProductActivity::class.java)
            startActivity(intent)
        }

        binding.searchButton.setOnClickListener {
            val category = binding.categorySpinner.selectedItem.toString()
            val recipes = cookingApp.searchRecipes(category)
            if (recipes.isNotEmpty()) {
                val firstRecipe = recipes.first()
                displayRecipeDetails(firstRecipe)
            }
        }

        binding.addToFavoritesButton.setOnClickListener {
            val currentRecipe = displayedRecipe ?: return@setOnClickListener
            cookingApp.addToFavorites(currentRecipe)
        }

        binding.buttonShowRecipes.setOnClickListener {
            val intent = Intent(this, RecipeListActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IntentIntegrator.REQUEST_CODE) {
            val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
            cookingApp.handleScanResult(result)
        }
    }

    private var displayedRecipe: Recipe? = null

    private fun displayRecipeDetails(recipe: Recipe) {
        binding.recipeDetailsTitle.text = recipe.getTitle()
        binding.recipeDetailsIngredients.text = recipe.getIngredients().joinToString(", ")
        binding.recipeDetailsInstructions.text = recipe.getInstructions()
        displayedRecipe = recipe
    }
}

