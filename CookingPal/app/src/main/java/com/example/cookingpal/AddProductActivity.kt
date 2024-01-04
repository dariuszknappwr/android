package com.example.cookingpal

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.cookingpal.databinding.ActivityAddProductBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddProductActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddProductBinding
    private val productAdapter = ProductAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddProductBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.recyclerViewProducts.adapter = productAdapter

        AppDatabase.getDatabase(this).productDao().getAll().observe(this, { products ->
            productAdapter.setProducts(products)
        })

        binding.buttonAddProduct.setOnClickListener {
            val productName = binding.editTextProductName.text.toString()

            val product = Product(name = productName)
            CoroutineScope(Dispatchers.IO).launch {
                AppDatabase.getDatabase(this@AddProductActivity).productDao().insert(product)
            }
        }
    }
}
