package com.unasp.unaspmarketplace.data.repository

import com.google.firebase.firestore.FirebaseFirestore

class FirestoreService {
    private val db = FirebaseFirestore.getInstance()

    fun saveUserData(uid: String, userData: Map<String, Any>, onResult: (Boolean) -> Unit) {
        db.collection("users").document(uid).set(userData)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }

    fun getUserData(uid: String, onResult: (Map<String, Any>?) -> Unit) {
        db.collection("users").document(uid).get()
            .addOnSuccessListener { onResult(it.data) }
            .addOnFailureListener { onResult(null) }
    }
}