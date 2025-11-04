package com.unasp.unaspmarketplace.models

import java.text.SimpleDateFormat
import java.util.*

data class Order(
    val id: String,
    val customerName: String,
    val pickupLocation: String = "UNASP Store",
    val items: List<OrderItem>,
    val orderDate: String,
    val paymentMethod: String
) {
    companion object {
        fun generateOrderId(): String {
            val timestamp = System.currentTimeMillis()
            return "PED${timestamp.toString().takeLast(8)}"
        }

        fun getCurrentDate(): String {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            return dateFormat.format(Date())
        }
    }

    fun formatForWhatsApp(): String {
        val itemsList = items.joinToString("\n") { "• ${it.productName} (Qtd: ${it.quantity}) - R$ ${String.format("%.2f", it.totalPrice)}" }
        val totalAmount = items.sumOf { it.totalPrice }

        return """
🛒 *NOVO PEDIDO - UNASP MARKETPLACE*

📋 *ID do Pedido:* $id
👤 *Nome:* $customerName
📍 *Local de Retirada:* $pickupLocation
📅 *Data da Compra:* $orderDate
💳 *Forma de Pagamento:* $paymentMethod (na retirada)

🛍️ *Itens Comprados:*
$itemsList

💰 *Total:* R$ ${String.format("%.2f", totalAmount)}

_Por favor, confirme o recebimento deste pedido._
        """.trimIndent()
    }
}

data class OrderItem(
    val productName: String,
    val quantity: Int,
    val unitPrice: Double,
    val totalPrice: Double
)
