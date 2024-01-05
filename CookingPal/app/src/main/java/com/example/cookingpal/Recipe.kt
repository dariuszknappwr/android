package com.example.cookingpal

import java.util.UUID

data class Recipe(
    val title: String,
    val products: List<Product>,
    val instructions: String? = "",
    val image: String = "",
    val id: Int = UUID.randomUUID().hashCode()
) {
    @JvmName("recipeId")
    fun getId(): Int = id
    @JvmName("recipeTitle")
    fun getTitle(): String = title
    @JvmName("recipeIngredients")
    fun getIngredients(): List<Product> = products
    @JvmName("recipeImage")
    fun getImage(): String = image
    @JvmName("recipeInstructions")
    fun getInstructions(): String = instructions!!
}