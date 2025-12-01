package com.unasp.unaspmarketplace.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.unasp.unaspmarketplace.R
import com.unasp.unaspmarketplace.models.Order
import com.unasp.unaspmarketplace.models.OrderStatus
import java.text.SimpleDateFormat
import java.util.*

/**
 * Adapter para exibir pedidos recebidos pelo vendedor
 */
class SellerOrdersAdapter(
    private val orders: List<Order>,
    private val onCompleteOrder: (Order) -> Unit,
    private val onViewDetails: (Order) -> Unit
) : RecyclerView.Adapter<SellerOrdersAdapter.SellerOrderViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SellerOrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_seller_order, parent, false)
        return SellerOrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: SellerOrderViewHolder, position: Int) {
        holder.bind(orders[position])
    }

    override fun getItemCount(): Int = orders.size

    inner class SellerOrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtOrderId: TextView = itemView.findViewById(R.id.txtOrderId)
        private val txtOrderDate: TextView = itemView.findViewById(R.id.txtOrderDate)
        private val txtOrderStatus: TextView = itemView.findViewById(R.id.txtOrderStatus)
        private val txtCustomerName: TextView = itemView.findViewById(R.id.txtCustomerName)
        private val txtCustomerWhatsApp: TextView = itemView.findViewById(R.id.txtCustomerWhatsApp)
        private val txtOrderTotal: TextView = itemView.findViewById(R.id.txtOrderTotal)
        private val txtOrderItems: TextView = itemView.findViewById(R.id.txtOrderItems)
        private val txtPaymentMethod: TextView = itemView.findViewById(R.id.txtPaymentMethod)
        private val btnCompleteOrder: MaterialButton = itemView.findViewById(R.id.btnCompleteOrder)
        private val btnViewDetails: MaterialButton = itemView.findViewById(R.id.btnViewDetails)

        fun bind(order: Order) {
            // ID do pedido (primeiros 8 caracteres)
            val shortId = if (order.id.length > 8) order.id.substring(0, 8) else order.id
            txtOrderId.text = "Pedido #$shortId"

            // Data do pedido
            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val date = Date(order.createdAt)
            txtOrderDate.text = dateFormat.format(date)

            // Status do pedido
            txtOrderStatus.text = order.status.displayName
            txtOrderStatus.setBackgroundColor(getStatusColor(order.status))

            // Informações do cliente
            txtCustomerName.text = "Cliente: ${order.buyerName}"
            txtCustomerWhatsApp.text = "WhatsApp: ${order.buyerWhatsApp}"

            // Total do pedido
            txtOrderTotal.text = "R$ ${String.format("%.2f", order.totalAmount)}"

            // Itens do pedido
            val itemsText = when {
                order.items.isEmpty() -> "Nenhum item"
                order.items.size == 1 -> "${order.items[0].quantity}x ${order.items[0].productName}"
                order.items.size == 2 -> "${order.items[0].quantity}x ${order.items[0].productName}, ${order.items[1].quantity}x ${order.items[1].productName}"
                else -> "${order.items[0].quantity}x ${order.items[0].productName}, ${order.items[1].quantity}x ${order.items[1].productName} e mais ${order.items.size - 2}"
            }
            txtOrderItems.text = itemsText

            // Método de pagamento
            txtPaymentMethod.text = "Pagamento: ${order.paymentMethod}"

            // Botão de concluir pedido
            btnCompleteOrder.visibility = if (order.status == OrderStatus.PENDING ||
                order.status == OrderStatus.CONFIRMED ||
                order.status == OrderStatus.PREPARING ||
                order.status == OrderStatus.READY) View.VISIBLE else View.GONE

            when (order.status) {
                OrderStatus.PENDING -> {
                    btnCompleteOrder.text = "✅ Confirmar Pedido"
                    btnCompleteOrder.setBackgroundColor(itemView.context.getColor(android.R.color.holo_green_light))
                }
                OrderStatus.CONFIRMED, OrderStatus.PREPARING -> {
                    btnCompleteOrder.text = "📦 Marcar como Pronto"
                    btnCompleteOrder.setBackgroundColor(itemView.context.getColor(android.R.color.holo_blue_light))
                }
                OrderStatus.READY -> {
                    btnCompleteOrder.text = "✅ Concluir Entrega"
                    btnCompleteOrder.setBackgroundColor(itemView.context.getColor(android.R.color.holo_green_dark))
                }
                OrderStatus.COMPLETED -> {
                    btnCompleteOrder.text = "✅ Concluído"
                    btnCompleteOrder.isEnabled = false
                    btnCompleteOrder.alpha = 0.5f
                }
                OrderStatus.CANCELLED -> {
                    btnCompleteOrder.visibility = View.GONE
                }
            }

            // Click listeners
            btnCompleteOrder.setOnClickListener {
                when (order.status) {
                    OrderStatus.PENDING -> {
                        // Confirmar pedido (muda para CONFIRMED)
                        onCompleteOrder(order.copy(status = OrderStatus.CONFIRMED))
                    }
                    OrderStatus.CONFIRMED, OrderStatus.PREPARING -> {
                        // Marcar como pronto (muda para READY)
                        onCompleteOrder(order.copy(status = OrderStatus.READY))
                    }
                    OrderStatus.READY -> {
                        // Concluir entrega (muda para COMPLETED)
                        onCompleteOrder(order.copy(status = OrderStatus.COMPLETED))
                    }
                    else -> { /* Não faz nada */ }
                }
            }

            btnViewDetails.setOnClickListener {
                onViewDetails(order)
            }

            // Click no card para ver detalhes
            itemView.setOnClickListener {
                onViewDetails(order)
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
