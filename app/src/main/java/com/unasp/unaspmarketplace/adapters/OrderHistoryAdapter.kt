package com.unasp.unaspmarketplace.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.unasp.unaspmarketplace.R
import com.unasp.unaspmarketplace.models.Order
import com.unasp.unaspmarketplace.models.OrderStatus
import java.text.SimpleDateFormat
import java.util.*

/**
 * Adapter para exibir o histórico de pedidos
 */
class OrderHistoryAdapter(
    private val orders: List<Order>,
    private val onOrderClick: (Order) -> Unit
) : RecyclerView.Adapter<OrderHistoryAdapter.OrderViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order_history, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(orders[position])
    }

    override fun getItemCount(): Int = orders.size

    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtOrderId: TextView = itemView.findViewById(R.id.txtOrderId)
        private val txtOrderDate: TextView = itemView.findViewById(R.id.txtOrderDate)
        private val txtOrderStatus: TextView = itemView.findViewById(R.id.txtOrderStatus)
        private val txtOrderTotal: TextView = itemView.findViewById(R.id.txtOrderTotal)
        private val txtOrderItems: TextView = itemView.findViewById(R.id.txtOrderItems)
        private val txtSellerName: TextView = itemView.findViewById(R.id.txtSellerName)

        fun bind(order: Order) {
            // ID do pedido (primeiros 8 caracteres)
            val shortId = if (order.id.length > 8) order.id.substring(0, 8) else order.id
            txtOrderId.text = "Pedido #$shortId"

            // Data do pedido
            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val date = Date(order.createdAt)
            txtOrderDate.text = dateFormat.format(date)

            // Status do pedido com cores
            val orderStatus = order.getOrderStatus()
            txtOrderStatus.text = orderStatus.displayName
            txtOrderStatus.setBackgroundColor(getStatusColor(orderStatus))

            // Total do pedido
            txtOrderTotal.text = "R$ ${String.format("%.2f", order.totalAmount)}"

            // Itens do pedido (primeiros 2 itens + "e mais X")
            val itemsText = when {
                order.items.isEmpty() -> "Nenhum item"
                order.items.size == 1 -> order.items[0].productName
                order.items.size == 2 -> "${order.items[0].productName} e ${order.items[1].productName}"
                else -> "${order.items[0].productName}, ${order.items[1].productName} e mais ${order.items.size - 2}"
            }
            txtOrderItems.text = itemsText

            // Nome do vendedor (ou "Vendedor não informado")
            txtSellerName.text = if (order.sellerName.isNotEmpty()) {
                "Vendedor: ${order.sellerName}"
            } else {
                "Vendedor: Não informado"
            }

            // Click listener
            itemView.setOnClickListener {
                onOrderClick(order)
            }
        }

        private fun getStatusColor(status: OrderStatus): Int {
            return when (status) {
                OrderStatus.PENDING -> itemView.context.getColor(android.R.color.holo_orange_light)
                OrderStatus.CONFIRMED -> itemView.context.getColor(android.R.color.holo_blue_light)
                OrderStatus.PREPARING -> itemView.context.getColor(android.R.color.holo_purple)
                OrderStatus.READY -> itemView.context.getColor(android.R.color.holo_green_light)
                OrderStatus.COMPLETED -> itemView.context.getColor(android.R.color.holo_green_dark)
                OrderStatus.CANCELLED -> itemView.context.getColor(android.R.color.holo_red_light)
            }
        }
    }
}
