package com.example.cookingpal
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.cookingpal.CookingPalApp

class ScanResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_result)

        val productName = intent.getStringExtra("productName")
        val textViewScannedProduct: TextView = findViewById(R.id.textViewScannedProduct)
        textViewScannedProduct.text = "Scanned product: $productName"
    }
}