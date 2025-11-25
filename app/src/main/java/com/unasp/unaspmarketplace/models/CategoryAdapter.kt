package com.unasp.unaspmarketplace.models

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.unasp.unaspmarketplace.R
import android.animation.ObjectAnimator
import android.animation.AnimatorSet

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

        // Aplicar estilo visual baseado na seleção
        if (position == selectedPosition) {
            // Categoria selecionada - destaque visual
            holder.itemView.alpha = 1.0f
            holder.txt.setTextColor(holder.itemView.context.getColor(R.color.blue_default))
            holder.itemView.scaleX = 1.1f
            holder.itemView.scaleY = 1.1f
        } else {
            // Categoria não selecionada - visual normal
            holder.itemView.alpha = 0.7f
            holder.txt.setTextColor(holder.itemView.context.getColor(R.color.gray_700))
            holder.itemView.scaleX = 1.0f
            holder.itemView.scaleY = 1.0f
        }

        // Adicionar click listener para buscar por categoria
        holder.itemView.setOnClickListener {
            // Atualizar seleção
            val oldPosition = selectedPosition
            selectedPosition = position

            // Notificar mudança visual
            notifyItemChanged(oldPosition)
            notifyItemChanged(selectedPosition)

            // Animação de clique
            animateClick(holder.itemView)

            // Callback para busca
            onCategoryClick?.invoke(categoria.name)
        }

        // Configurar como clicável
        holder.itemView.isClickable = true
        holder.itemView.isFocusable = true
    }

    fun setSelectedCategory(categoryName: String) {
        val newPosition = categorys.indexOfFirst { it.name == categoryName }
        if (newPosition != -1 && newPosition != selectedPosition) {
            val oldPosition = selectedPosition
            selectedPosition = newPosition
            notifyItemChanged(oldPosition)
            notifyItemChanged(selectedPosition)
        }
    }

    private fun animateClick(view: View) {
        // Animação de escala ao clicar
        val scaleDown = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.9f)
        val scaleDownY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.9f)
        val scaleUp = ObjectAnimator.ofFloat(view, "scaleX", 0.9f, 1f)
        val scaleUpY = ObjectAnimator.ofFloat(view, "scaleY", 0.9f, 1f)

        scaleDown.duration = 100
        scaleDownY.duration = 100
        scaleUp.duration = 100
        scaleUpY.duration = 100

        val animatorSet = AnimatorSet()
        animatorSet.play(scaleDown).with(scaleDownY)
        animatorSet.play(scaleUp).with(scaleUpY).after(scaleDown)
        animatorSet.start()
    }

    override fun getItemCount() = categorys.size
}