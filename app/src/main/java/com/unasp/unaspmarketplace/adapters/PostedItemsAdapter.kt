package com.unasp.unaspmarketplace.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.unasp.unaspmarketplace.R
import com.unasp.unaspmarketplace.models.Product

class PostedItemsAdapter(
    private val products: List<Product>,
    private val onEditClick: (Product) -> Unit,
    private val onToggleVisibilityClick: (Product) -> Unit,
    private val onDeleteClick: (Product) -> Unit
) : RecyclerView.Adapter<PostedItemsAdapter.ProductViewHolder>() {

    class ProductViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgProduct: ImageView = view.findViewById(R.id.imgProduct)
        val tvProductName: TextView = view.findViewById(R.id.tvProductName)
        val tvProductPrice: TextView = view.findViewById(R.id.tvProductPrice)
        val tvProductStock: TextView = view.findViewById(R.id.tvProductStock)
        val tvProductStatus: TextView = view.findViewById(R.id.tvProductStatus)
        val btnEdit: ImageButton = view.findViewById(R.id.btnEdit)
        val btnToggleVisibility: ImageButton = view.findViewById(R.id.btnToggleVisibility)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_posted_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]

        // Carregar imagem do produto
        if (product.imageUrls.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(product.imageUrls.first())
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)
                .centerCrop()
                .into(holder.imgProduct)
        } else {
            holder.imgProduct.setImageResource(R.drawable.ic_launcher_background)
        }

        holder.tvProductName.text = product.name
        holder.tvProductPrice.text = "R$ ${String.format("%.2f", product.price)}"
        holder.tvProductStock.text = "Estoque: ${product.stock}"

        // Status do produto
        if (product.active) {
            holder.tvProductStatus.text = "Visível"
            holder.tvProductStatus.setTextColor(holder.itemView.context.getColor(R.color.blue_primary))
            holder.btnToggleVisibility.setImageResource(R.drawable.ic_visibility)
        } else {
            holder.tvProductStatus.text = "Oculto"
            holder.tvProductStatus.setTextColor(holder.itemView.context.getColor(R.color.gray_500))
            holder.btnToggleVisibility.setImageResource(R.drawable.ic_visibility_off)
        }

        // Configurar cliques dos botões
        holder.btnEdit.setOnClickListener {
            onEditClick(product)
        }

        holder.btnToggleVisibility.setOnClickListener {
            onToggleVisibilityClick(product)
        }

        holder.btnDelete.setOnClickListener {
            onDeleteClick(product)
        }
    }

    override fun getItemCount() = products.size
}
