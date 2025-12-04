package com.unasp.unaspmarketplace.models

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.unasp.unaspmarketplace.R

class CategoryAdapter(
    private val categorys: List<Category>,
    private val onCategoryClick: ((String) -> Unit)? = null
) : RecyclerView.Adapter<CategoryAdapter.CategoriaViewHolder>() {

    private var selectedPosition = 0 // Posição da categoria selecionada

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

        // Adicionar click listener para buscar por categoria
        holder.itemView.setOnClickListener {
            // Atualizar posição selecionada
            val clickedPosition = holder.adapterPosition
            if (clickedPosition != RecyclerView.NO_POSITION) {
                val previousPosition = selectedPosition
                selectedPosition = clickedPosition

                // Notificar mudanças
                notifyItemChanged(previousPosition)
                notifyItemChanged(selectedPosition)

                // Invocar callback
                onCategoryClick?.invoke(categoria.name)
            }
        }

        // Aplicar estilo visual baseado na seleção
        if (position == selectedPosition) {
            // Categoria selecionada - destaque visual
            holder.itemView.alpha = 1.0f
            holder.txt.setTextColor(holder.itemView.context.getColor(R.color.blue_default))
            holder.itemView.scaleX = 1.1f
            holder.itemView.scaleY = 1.1f
        } else {
            // Categoria não selecionada - visual normal
            holder.itemView.alpha = 1.0f   // agora todos ficam totalmente visíveis
            holder.txt.setTextColor(holder.itemView.context.getColor(R.color.gray_700))
            holder.itemView.scaleX = 1.0f
            holder.itemView.scaleY = 1.0f
        }
    }

    override fun getItemCount(): Int = categorys.size

    // Método público para definir categoria selecionada programaticamente
    fun setSelectedCategory(categoryName: String) {
        val newPosition = categorys.indexOfFirst { it.name == categoryName }
        if (newPosition != -1 && newPosition != selectedPosition) {
            val previousPosition = selectedPosition
            selectedPosition = newPosition
            notifyItemChanged(previousPosition)
            notifyItemChanged(selectedPosition)
        }
    }
}
