package com.unasp.unaspmarketplace.models

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Product(
    @DocumentId
    var id: String = "",
    var name: String = "",
    var description: String = "",
    var price: Double = 0.0,
    var stock: Int = 0,
    var category: String = "",
    var imageUrls: List<String> = emptyList(),
    var sellerId: String = "",
    var sellerName: String = "",
    var active: Boolean = true,
    @ServerTimestamp
    var createdAt: Date? = null,
    @ServerTimestamp
    var updatedAt: Date? = null
) {
    // Construtor vazio necessário para o Firestore
    constructor() : this("", "", "", 0.0, 0, "", emptyList(), "", "", true, null, null)
}

data class Category(
    var id: String = "",
    var name: String = "",
    var iconUrl: String = "",
    var isActive: Boolean = true
) {
    constructor() : this("", "", "", true)
}
