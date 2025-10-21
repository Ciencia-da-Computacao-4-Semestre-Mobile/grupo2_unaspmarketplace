package com.unasp.unaspmarketplace.data.firestore

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class UserData(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val plan: String = "Gratuito"
)

object UserRepository {

    private val db = FirebaseFirestore.getInstance()
    private val usersRef = db.collection("users")

    suspend fun createUser(uid: String, name: String, email: String) {
        val user = UserData(uid, name, email)
        usersRef.document(uid).set(user).await()
    }

    suspend fun getCurrentUser(): UserData? {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return null
        val snapshot = usersRef.document(uid).get().await()
        return snapshot.toObject(UserData::class.java)
    }

    suspend fun updatePlan(plan: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        usersRef.document(uid).update("plan", plan).await()
    }
}