package com.example.cookingpal

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    @TypeConverter
    fun fromProductList(value: List<Product>): String {
        val gson = Gson()
        val type = object : TypeToken<List<Product>>() {}.type
        return gson.toJson(value, type)
    }

    @TypeConverter
    fun toProductList(value: String): List<Product> {
        val gson = Gson()
        val type = object : TypeToken<List<Product>>() {}.type
        return gson.fromJson(value, type)
    }
}