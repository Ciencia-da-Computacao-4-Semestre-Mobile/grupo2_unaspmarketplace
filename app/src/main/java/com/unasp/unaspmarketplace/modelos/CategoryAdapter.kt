package com.unasp.unaspmarketplace.modelos

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.unasp.unaspmarketplace.R
class CategoryAdapter(private val categorys: List<Category>) :
    RecyclerView.Adapter<CategoryAdapter.CategoriaViewHolder>() {

    class CategoriaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val img: ImageView = itemView.findViewById(R.id.imgCategory)
        val txt: TextView = itemView.findViewById(R.id.txtCategory)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoriaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        return CategoriaViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoriaViewHolder, position: Int) {
        val categoria = categorys[position]
        holder.img.setImageResource(categoria.iconRes)
        holder.txt.text = categoria.name
    }

    override fun getItemCount() = categorys.size
}