package com.unasp.unaspmarketplace.data.firestore

data class UserData(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val createdAt: com.google.firebase.Timestamp? = null
)
