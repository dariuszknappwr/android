package com.example.cookingpal

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ProductAdapter(private val onDelete: (Product) -> Unit) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {
    private var products: List<Product> = listOf()

    class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewName: TextView = itemView.findViewById(R.id.textViewName)
        val iconDelete: ImageView = itemView.findViewById(R.id.icon_delete)

        fun bind(product: Product, onDelete: (Product) -> Unit) {
            textViewName.text = product.name
            iconDelete.setOnClickListener { onDelete(product) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]
        holder.bind(product, onDelete)
    }

    override fun getItemCount() = products.size

    fun setProducts(products: List<Product>) {
        this.products = products
        notifyDataSetChanged()
    }
}