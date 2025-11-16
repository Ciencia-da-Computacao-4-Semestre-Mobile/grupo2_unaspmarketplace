package com.unasp.unaspmarketplace.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.unasp.unaspmarketplace.R
import com.unasp.unaspmarketplace.models.Order
import java.text.SimpleDateFormat
import java.util.*

class OrderHistoryAdapter(
    private val orders: List<Order>,
    private val onOrderClick: (Order) -> Unit
) : RecyclerView.Adapter<OrderHistoryAdapter.OrderViewHolder>() {

    class OrderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvOrderId: TextView = view.findViewById(R.id.tvOrderId)
        val tvOrderDate: TextView = view.findViewById(R.id.tvOrderDate)
        val tvOrderItems: TextView = view.findViewById(R.id.tvOrderItems)
        val tvOrderTotal: TextView = view.findViewById(R.id.tvOrderTotal)
        val tvOrderStatus: TextView = view.findViewById(R.id.tvOrderStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order_history, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]

        holder.tvOrderId.text = "Pedido #${order.id}"
        holder.tvOrderDate.text = order.orderDate

        // Mostrar quantidade de itens
        val itemCount = order.items.size
        val itemText = if (itemCount == 1) "1 item" else "$itemCount itens"
        holder.tvOrderItems.text = itemText

        // Calcular total
        val total = order.items.sumOf { it.totalPrice }
        holder.tvOrderTotal.text = "R$ ${String.format("%.2f", total)}"

        // Status do pedido (pode ser expandido futuramente)
        holder.tvOrderStatus.text = "Concluído"

        holder.itemView.setOnClickListener {
            onOrderClick(order)
        }
    }

    override fun getItemCount() = orders.size
}
