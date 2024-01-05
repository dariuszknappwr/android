package com.example.cookingpal
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.cookingpal.CookingPalApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ScanResultActivity : AppCompatActivity() {
    private lateinit var productDao: ProductDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_result)

        var productName = intent.getStringExtra("productName").toString()
        val textViewScannedProduct: TextView = findViewById(R.id.textViewScannedProduct)
        textViewScannedProduct.text = "Zeskanowany produkt: $productName"

        // Get the ProductDao from the database
        productDao = AppDatabase.getDatabase(this).productDao()

        // Load the ingredients from ingredients.xml
        val ingredients = resources.getStringArray(R.array.ingredients)
    

        // Get the Spinner from the layout
        val spinnerScannedProduct: Spinner = findViewById(R.id.spinnerScannedProduct)

        // Try to match the scanned product name to an ingredient
        productName = productName.replace(Regex("[^A-Za-z0-9 ]"), "").toLowerCase()
        val words = productName.split(" ")
        val matchedIngredients = mutableListOf<String>()
        
        for (windowSize in words.size downTo 1) {
            for (i in 0 until words.size - windowSize + 1) {
                val candidate = words.subList(i, i + windowSize).joinToString(" ")
                if (candidate in ingredients) {
                    matchedIngredients.add(candidate)
                }
            }
        }

        // Set the matched ingredients as the data for the Spinner
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, matchedIngredients)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerScannedProduct.adapter = adapter

        val buttonAddScannedProduct: Button = findViewById(R.id.button_add_scanned_product)
        buttonAddScannedProduct.setOnClickListener {
            val matchedProduct = spinnerScannedProduct.selectedItem.toString()
    
            // Check if the matched product is in the list of ingredients
            if (matchedProduct in ingredients) {
                // Create a new Product
                val product = Product(name = matchedProduct)
    
                // Insert the scanned product into the database in a new coroutine
                CoroutineScope(Dispatchers.IO).launch {
                    productDao.insert(product)
                    withContext(Dispatchers.Main) {
                        finish() // Close this activity and return to the previous one
                    }
                }
            } else {
                // Show a toast message if the product is not in the list of ingredients
                Toast.makeText(this, "Proszę wybrać produkt z listy produktów", Toast.LENGTH_SHORT).show()
            }
        }
    }
}