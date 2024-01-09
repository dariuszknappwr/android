package com.example.cookingpal

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recipes")
data class RecipeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    var title: String,
    var ingredients: String,
    var instructions: String,
    var imageUrl: String?,
    var favorite: Boolean = false
)
