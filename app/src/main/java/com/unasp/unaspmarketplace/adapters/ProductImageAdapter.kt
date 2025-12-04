package com.unasp.unaspmarketplace.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.unasp.unaspmarketplace.R

class ProductImageAdapter(
    private val images: MutableList<Uri>,
    private val onRemoveClick: (Int) -> Unit
) : RecyclerView.Adapter<ProductImageAdapter.ImageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product_image, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.bind(images[position], position)
    }

    override fun getItemCount(): Int = images.size

    fun addImage(uri: Uri) {
        images.add(uri)
        notifyItemInserted(images.size - 1)
    }

    fun removeImage(position: Int) {
        if (position in 0 until images.size) {
            images.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, images.size)
        }
    }

    fun getImages(): List<Uri> = images.toList()

    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.ivProductImage)
        private val btnRemove: ImageButton = itemView.findViewById(R.id.btnRemoveImage)

        fun bind(uri: Uri, position: Int) {
            try {
                Glide.with(itemView.context)
                    .load(uri)
                    .centerCrop()
                    .placeholder(R.color.gray_light)
                    .error(R.color.gray_light)
                    .into(imageView)

                btnRemove.setOnClickListener {
                    onRemoveClick(position)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Fallback para erro de carregamento de imagem
                imageView.setBackgroundColor(itemView.context.getColor(R.color.gray_light))
            }
        }
    }
}
