package com.unasp.unaspmarketplace.models

import java.text.SimpleDateFormat
import java.util.*

data class Order(
    val id: String = "",
    val userId: String = "", // ID do usuário que fez o pedido
    val customerName: String = "",
    val pickupLocation: String = "UNASP Store",
    val items: List<OrderItem> = emptyList(),
    val orderDate: String = "",
    val paymentMethod: String = "",
    val status: String = "Concluído", // Pode ser expandido futuramente
    val timestamp: Long = System.currentTimeMillis(),
    val totalAmount: Double = 0.0
) {
    // Construtor vazio necessário para o Firebase
    constructor() : this("", "", "", "UNASP Store", emptyList(), "", "", "Concluído", System.currentTimeMillis(), 0.0)

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
        val calculatedTotal = items.sumOf { it.totalPrice }

        return """
🛒 *NOVO PEDIDO - UNASP MARKETPLACE*

📋 *ID do Pedido:* $id
👤 *Nome:* $customerName
📍 *Local de Retirada:* $pickupLocation
📅 *Data da Compra:* $orderDate
💳 *Forma de Pagamento:* $paymentMethod (na retirada)

🛍️ *Itens Comprados:*
$itemsList

💰 *Total:* R$ ${String.format("%.2f", calculatedTotal)}

_Por favor, confirme o recebimento deste pedido._
        """.trimIndent()
    }
}

