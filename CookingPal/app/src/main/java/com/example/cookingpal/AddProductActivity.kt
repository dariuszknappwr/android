package com.example.cookingpal

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cookingpal.databinding.ActivityAddProductBinding
import com.google.zxing.integration.android.IntentIntegrator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddProductActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddProductBinding
    private lateinit var productDao: ProductDao
    private lateinit var cookingApp: CookingPalApp

    private val productAdapter = ProductAdapter { product ->
        CoroutineScope(Dispatchers.IO).launch {
            productDao.delete(product)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cookingApp = CookingPalApp(this, this)

        binding.buttonScanProducts.setOnClickListener {
            cookingApp.scanBarcode()
        }

        // Set the LayoutManager and the adapter for the RecyclerView
        binding.recyclerViewProducts.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewProducts.adapter = productAdapter

        // Get the ProductDao from the database
        productDao = AppDatabase.getDatabase(this).productDao()

        // Observe the products in the database and submit them to the adapter
        productDao.getAll().observe(this, Observer<List<Product>> { products ->
            productAdapter.setProducts(products)
        })

        // Load the ingredients from ingredients.xml
        val ingredients = resources.getStringArray(R.array.ingredients)

        // Set the ingredients as the adapter for the AutoCompleteTextView
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, ingredients)
        binding.autoCompleteTextViewProductName.setAdapter(adapter)

        binding.buttonAddProduct.setOnClickListener {
            val productName = binding.autoCompleteTextViewProductName.text.toString()
        
            // Check if the product name is in the list of ingredients
            if (productName in ingredients) {
                // Create a new Product
                val product = Product(name = productName)

                // Insert the product into the database in a new coroutine
                CoroutineScope(Dispatchers.IO).launch {
                    productDao.insert(product)
                    withContext(Dispatchers.Main) {
                        binding.autoCompleteTextViewProductName.text.clear()
                    }
                }
            }
            else{
                Toast.makeText(this, "Proszę wybrać składnik z listy składników", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IntentIntegrator.REQUEST_CODE) {
            val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
            cookingApp.handleScanResult(result)
        }
    }
}