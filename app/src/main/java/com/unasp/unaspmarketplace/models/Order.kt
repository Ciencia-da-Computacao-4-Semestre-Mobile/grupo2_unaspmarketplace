package com.unasp.unaspmarketplace.models

import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName

/**
 * Modelo para representar um pedido no sistema
 */
@IgnoreExtraProperties
data class Order(
    @PropertyName("id") val id: String = "",
    @PropertyName("buyerId") val buyerId: String = "", // ID do comprador
    @PropertyName("sellerId") val sellerId: String = "", // ID do vendedor
    @PropertyName("sellerName") val sellerName: String = "", // Nome do vendedor
    @PropertyName("buyerName") val buyerName: String = "", // Nome do comprador
    @PropertyName("buyerEmail") val buyerEmail: String = "", // Email do comprador
    @PropertyName("buyerWhatsApp") val buyerWhatsApp: String = "", // WhatsApp do comprador
    @PropertyName("items") val items: List<OrderItem> = emptyList(), // Itens do pedido
    @PropertyName("totalAmount") val totalAmount: Double = 0.0, // Valor total
    @PropertyName("paymentMethod") val paymentMethod: String = "", // Forma de pagamento
    @PropertyName("status") val status: String = OrderStatus.PENDING.name, // Status do pedido como String
    @PropertyName("createdAt") val createdAt: Long = System.currentTimeMillis(), // Data de criação
    @PropertyName("updatedAt") val updatedAt: Long = System.currentTimeMillis(), // Data de atualização
    @PropertyName("completedAt") val completedAt: Long? = null, // Data de conclusão
    @PropertyName("whatsAppMessage") val whatsAppMessage: String = "", // Mensagem enviada para o WhatsApp
    @PropertyName("notes") val notes: String = "" // Observações adicionais
) {
    // Construtor vazio necessário para Firebase
    constructor() : this(
        "", "", "", "", "", "", "",
        emptyList(), 0.0, "", OrderStatus.PENDING.name,
        System.currentTimeMillis(), System.currentTimeMillis(), null, "", ""
    )

    // Função helper para converter string para OrderStatus
    fun getOrderStatus(): OrderStatus = OrderStatus.fromString(status)
}

/**
 * Status possíveis de um pedido
 */
enum class OrderStatus(val displayName: String, val description: String) {
    PENDING("Pendente", "Aguardando confirmação do vendedor"),
    CONFIRMED("Confirmado", "Pedido confirmado pelo vendedor"),
    PREPARING("Preparando", "Produto sendo preparado"),
    READY("Pronto", "Produto pronto para retirada"),
    COMPLETED("Concluído", "Pedido finalizado com sucesso"),
    CANCELLED("Cancelado", "Pedido cancelado");

    companion object {
        fun fromString(value: String): OrderStatus {
            return values().find { it.name == value } ?: PENDING
        }
    }
}
