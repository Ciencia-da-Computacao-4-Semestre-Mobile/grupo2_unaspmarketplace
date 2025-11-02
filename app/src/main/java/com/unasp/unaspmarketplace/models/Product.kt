package com.unasp.unaspmarketplace.models

data class Product(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val category: String = "",
    val stock: Int = 0,
    val imageUrls: List<String> = emptyList(),
    val sellerId: String = "",
    val active: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
) {
    // Construtor vazio necessário para o Firebase
    constructor() : this("", "", "", 0.0, "", 0, emptyList(), "", true, System.currentTimeMillis())
}
