package com.unasp.unaspmarketplace.models

/**
 * Modelo para representar um pedido no sistema
 */
data class Order(
    val id: String = "",
    val buyerId: String = "", // ID do comprador
    val sellerId: String = "", // ID do vendedor
    val sellerName: String = "", // Nome do vendedor
    val buyerName: String = "", // Nome do comprador
    val buyerEmail: String = "", // Email do comprador
    val buyerWhatsApp: String = "", // WhatsApp do comprador
    val items: List<OrderItem> = emptyList(), // Itens do pedido
    val totalAmount: Double = 0.0, // Valor total
    val paymentMethod: String = "", // Forma de pagamento
    val status: OrderStatus = OrderStatus.PENDING, // Status do pedido
    val createdAt: Long = System.currentTimeMillis(), // Data de criação
    val updatedAt: Long = System.currentTimeMillis(), // Data de atualização
    val completedAt: Long? = null, // Data de conclusão
    val whatsAppMessage: String = "", // Mensagem enviada para o WhatsApp
    val notes: String = "" // Observações adicionais
)

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
