package com.unasp.unaspmarketplace.models

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val profileImageUrl: String = "",
    val whatsappNumber: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
)
