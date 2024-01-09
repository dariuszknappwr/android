package com.example.cookingpal

import java.io.Serializable
import java.util.UUID

data class Recipe(
    val id: Int = UUID.randomUUID().hashCode(),
    val title: String,
    val imageUrl: String? = null,
    val ingredients: List<Product> = emptyList(),
    val instructions: String? = "",
    var favorite: Boolean = false

) : Serializable
{
    @JvmName("recipeId")
    fun getId(): Int = id
    @JvmName("recipeTitle")
    fun getTitle(): String = title
    @JvmName("recipeIngredients")
    fun getIngredients(): List<Product> = ingredients
    @JvmName("recipeImage")
    fun getImage(): String? = imageUrl
    @JvmName("recipeInstructions")
    fun getInstructions(): String = instructions!!
} 