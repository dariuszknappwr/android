package com.example.cookingpal

import java.util.UUID

data class Ingredient(val name: String, val id: Int = UUID.randomUUID().hashCode() )