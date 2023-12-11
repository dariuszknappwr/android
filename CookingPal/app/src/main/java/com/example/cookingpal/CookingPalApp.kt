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

            // dodać logikę po zeskanowaniu
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




