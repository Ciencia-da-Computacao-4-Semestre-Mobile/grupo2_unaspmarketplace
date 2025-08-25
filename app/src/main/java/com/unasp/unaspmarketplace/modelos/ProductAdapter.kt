package com.unasp.unaspmarketplace.modelos

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.unasp.unaspmarketplace.R

class ProductAdapter(private val products: List<Product>) :
    RecyclerView.Adapter<ProductAdapter.ProdutoViewHolder>() {

    class ProdutoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val img = itemView.findViewById<ImageView>(R.id.imgProduct)
        val name = itemView.findViewById<TextView>(R.id.txtProductName)
        val price = itemView.findViewById<TextView>(R.id.txtProductPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProdutoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProdutoViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProdutoViewHolder, position: Int) {
        val product = products[position]
        holder.img.setImageResource(product.imageRes)
        holder.name.text = product.name
        holder.price.text = "R$ %.2f".format(product.price)
    }

    override fun getItemCount() = products.size
}