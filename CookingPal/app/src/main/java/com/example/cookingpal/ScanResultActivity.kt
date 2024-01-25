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

        var productName = intent.getStringExtra("productName")
        var scannedContent = intent.getStringExtra("scannedContent")

        if (!productName.isNullOrEmpty()) {
            productName = productName.toString()
        }
        val textViewScannedProduct: TextView = findViewById(R.id.textViewScannedProduct)
        textViewScannedProduct.text = "Zeskanowany produkt: $productName\n Kod kreskowy: $scannedContent"
        if (productName.isNullOrEmpty()) {
            textViewScannedProduct.text = "Zeskanowany produkt: Niestety dany produkt nie posiada nazwy\n Kod kreskowy: $scannedContent"
        }

        productDao = AppDatabase.getDatabase(this).productDao()

        val ingredients = resources.getStringArray(R.array.ingredients)

        val spinnerScannedProduct: Spinner = findViewById(R.id.spinnerScannedProduct)

        val buttonAddScannedProduct: Button = findViewById(R.id.button_add_scanned_product)
        if (productName.isNullOrEmpty()) {
            buttonAddScannedProduct.isEnabled = false
        }

        val buttonBack = findViewById<Button>(R.id.button_back)
        buttonBack.setOnClickListener {
            finish()
        }

        if (!productName.isNullOrEmpty()) {
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

            if(matchedIngredients.isNullOrEmpty())
            {
                buttonAddScannedProduct.isEnabled = false
            }


            val adapter =
                ArrayAdapter(this, android.R.layout.simple_spinner_item, matchedIngredients)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerScannedProduct.adapter = adapter



            buttonAddScannedProduct.setOnClickListener {
                val matchedProduct = spinnerScannedProduct.selectedItem.toString()

                if (matchedProduct in ingredients) {
                    val product = Product(name = matchedProduct)

                    CoroutineScope(Dispatchers.IO).launch {
                        productDao.insert(product)
                        withContext(Dispatchers.Main) {
                            finish()
                        }
                    }
                } else {
                    Toast.makeText(
                        this,
                        "Proszę wybrać produkt z listy produktów",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            }
        }
    }
}