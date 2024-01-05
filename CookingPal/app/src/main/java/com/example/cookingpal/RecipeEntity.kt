package com.example.cookingpal

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recipes")
data class RecipeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val ingredients: String,
    val instructions: String,
    val imageUrl: String?,
    var favorite: Boolean = false
)
