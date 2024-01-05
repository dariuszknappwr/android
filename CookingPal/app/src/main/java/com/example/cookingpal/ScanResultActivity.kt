package com.example.cookingpal
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
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

        val productName = intent.getStringExtra("productName").toString()
        val textViewScannedProduct: TextView = findViewById(R.id.textViewScannedProduct)
        textViewScannedProduct.text = "Scanned product: $productName"

        // Get the ProductDao from the database
        productDao = AppDatabase.getDatabase(this).productDao()

        val buttonAddScannedProduct: Button = findViewById(R.id.button_add_scanned_product)
        buttonAddScannedProduct.setOnClickListener {
            val product = Product(name = productName)

            // Insert the scanned product into the database in a new coroutine
            CoroutineScope(Dispatchers.IO).launch {
                productDao.insert(product)
                withContext(Dispatchers.Main) {
                    finish() // Close this activity and return to the previous one
                }
            }
        }
    }
}