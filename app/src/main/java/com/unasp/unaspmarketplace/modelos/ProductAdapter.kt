package com.unasp.unaspmarketplace.modelos

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.unasp.unaspmarketplace.ProductDetailActivity
import com.unasp.unaspmarketplace.R
import com.unasp.unaspmarketplace.models.Product

class ProductAdapter(private var products: MutableList<Product>) :
    RecyclerView.Adapter<ProductAdapter.ProdutoViewHolder>() {

    class ProdutoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val img = itemView.findViewById<ImageView>(R.id.imgProduct)
        val name = itemView.findViewById<TextView>(R.id.txtProductName)
        val price = itemView.findViewById<TextView>(R.id.txtProductPrice)
        val category = itemView.findViewById<TextView>(R.id.txtProductCategory)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProdutoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProdutoViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProdutoViewHolder, position: Int) {
        val product = products[position]

        // Por enquanto usar imagem padrão até implementarmos upload de imagens
        holder.img.setImageResource(R.drawable.ic_launcher_background)
        holder.name.text = product.name
        holder.price.text = "R$ %.2f".format(product.price)

        // Mostrar categoria se houver TextView para isso
        holder.category?.text = product.category

        // Adicionar clique no item para abrir detalhes do produto
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, ProductDetailActivity::class.java).apply {
                putExtra("productId", product.id)
                putExtra("productName", product.name)
                putExtra("productDescription", product.description)
                putExtra("productPrice", product.price)
                putExtra("productStock", product.stock)
                putExtra("productCategory", product.category)
                putExtra("productSellerId", product.sellerId)
                putExtra("productSellerName", product.sellerName)
                putExtra("productActive", product.active)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = products.size

    // Método para atualizar a lista de produtos
    fun updateProducts(newProducts: List<Product>) {
        products.clear()
        products.addAll(newProducts)
        notifyDataSetChanged()
    }

    // Método para adicionar um produto
    fun addProduct(product: Product) {
        products.add(0, product) // Adicionar no início da lista
        notifyItemInserted(0)
    }
}