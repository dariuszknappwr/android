package com.example.cookingpal

import android.content.Intent
import android.os.Bundle
import android.util.Log
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

        binding.recyclerViewProducts.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewProducts.adapter = productAdapter

        productDao = AppDatabase.getDatabase(this).productDao()

        productDao.getAll().observe(this, Observer<List<Product>> { products ->
            productAdapter.setProducts(products)
        })

        val ingredients = resources.getStringArray(R.array.ingredients)
        //add log to see all ingredients one by one
        Log.d("ingredients", ingredients.toString())
        Log.d("ingredients", ingredients.toString())

        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, ingredients)
        binding.autoCompleteTextViewProductName.setAdapter(adapter)

        binding.buttonAddProduct.setOnClickListener {
            val productName = binding.autoCompleteTextViewProductName.text.toString()
        
            if (productName in ingredients) {
                val product = Product(name = productName)

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