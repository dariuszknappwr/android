package com.example.cookingpal

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [RecipeEntity::class, Product::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun recipeDao(): RecipeDao
    abstract fun productDao(): ProductDao
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Drop the old tables
                database.execSQL("DROP TABLE IF EXISTS 'RecipeEntity'")
                database.execSQL("DROP TABLE IF EXISTS 'Product'")
        
                // Create the new tables
                database.execSQL("CREATE TABLE 'RecipeEntity' ('id' INTEGER PRIMARY KEY NOT NULL, /* other columns */)")
                database.execSQL("CREATE TABLE 'Product' ('id' INTEGER PRIMARY KEY NOT NULL, 'name' TEXT, /* other columns */)")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                .addMigrations(MIGRATION_1_2)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}