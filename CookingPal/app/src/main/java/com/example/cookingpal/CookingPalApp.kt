package com.example.cookingpal

import android.app.Activity
import android.content.Intent
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import java.util.*
import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.widget.TextView
import android.widget.Toast
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class CookingPalApp(private val activity: Activity, context: Context) {

    private val database: AppDatabase = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "cooking_pal_database"
    ).build()

    fun scanBarcode() {
        val integrator = IntentIntegrator(activity)
        integrator.setPrompt("Zeskanuj kod kreskowy")
        integrator.setBeepEnabled(true)
        integrator.setOrientationLocked(false)
        integrator.initiateScan()
    }

    fun handleScanResult(result: IntentResult?) {
        if (result != null) {
            val scannedContent = result.contents
            println("Zeskanowano kod kreskowy: $scannedContent")

            val url = "https://world.openfoodfacts.org/api/v0/product/$scannedContent.json"
            val request = Request.Builder().url(url).build()

            val client = OkHttpClient()
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    e.printStackTrace()
                }

                override fun onResponse(call: Call, response: Response) {
                    val strResponse = response.body()?.string()
                    val jsonResponse = JSONObject(strResponse)

                    if (jsonResponse.getString("status_verbose") == "product found") {
                        val product = jsonResponse.getJSONObject("product")

                        val productName = product.optString("product_name")

                        val ingredientsText = product.optString("ingredients_text")

                        val intent = Intent(activity, ScanResultActivity::class.java)
                        intent.putExtra("productName", productName)
                        intent.putExtra("ingredientsText", ingredientsText)
                        intent.putExtra("scannedContent", scannedContent)
                        activity.startActivity(intent)
                    } else {
                        activity.runOnUiThread {
                            Toast.makeText(activity, "Nie znaleziono produktu", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                }
            })
        }
    }

}




