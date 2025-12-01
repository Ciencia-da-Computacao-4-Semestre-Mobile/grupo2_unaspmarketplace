package com.unasp.unaspmarketplace.models

data class OrderItem(
    val productId: String = "",
    val productName: String = "",
    val productImage: String = "", // Primeira imagem do produto
    val quantity: Int = 0,
    val unitPrice: Double = 0.0,
    val totalPrice: Double = 0.0
) {
    // Construtor vazio necessário para o Firebase
    constructor() : this("", "", "", 0, 0.0, 0.0)
}
