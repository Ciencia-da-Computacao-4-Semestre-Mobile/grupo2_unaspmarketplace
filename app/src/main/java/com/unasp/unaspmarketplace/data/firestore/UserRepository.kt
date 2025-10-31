package com.unasp.unaspmarketplace.data.firestore

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object UserRepository {

    suspend fun createUser(uid: String, name: String, email: String) {
        val user = UserData(
            uid = uid,
            name = name,
            email = email,
            createdAt = com.google.firebase.Timestamp.now()
        )

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .set(user)
            .await()
    }

    suspend fun getCurrentUser(): UserData? {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return null

        return try {
            val document = FirebaseFirestore.getInstance()
                .collection("users")
                .document(currentUser.uid)
                .get()
                .await()

            document.toObject(UserData::class.java)
        } catch (e: Exception) {
            Log.e("UserRepository", "Error getting current user", e)
            null
        }
    }

    suspend fun updateUser(uid: String, updates: Map<String, Any>) {
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .update(updates)
            .await()
    }

    suspend fun updatePlan(plan: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .update("plan", plan)
            .await()
    }
}

