package com.unasp.unaspmarketplace.utils

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.unasp.unaspmarketplace.models.User
import kotlinx.coroutines.tasks.await

object UserUtils {
    private const val TAG = "UserUtils"
    private const val USERS_COLLECTION = "users"

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun ensureUserDataExists() {
        val currentUser = auth.currentUser ?: return

        try {
            val userDoc = firestore.collection(USERS_COLLECTION)
                .document(currentUser.uid)
                .get()
                .await()

            if (!userDoc.exists()) {
                // Criar novo usuário no Firestore
                val user = User(
                    id = currentUser.uid,
                    name = currentUser.displayName ?: "",
                    email = currentUser.email ?: "",
                    profileImageUrl = currentUser.photoUrl?.toString() ?: ""
                )

                firestore.collection(USERS_COLLECTION)
                    .document(currentUser.uid)
                    .set(user)
                    .await()

                Log.d(TAG, "Usuário criado no Firestore: ${user.email}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao garantir dados do usuário", e)
            throw e
        }
    }

    suspend fun getCurrentUser(): User? {
        val currentUser = auth.currentUser ?: return null

        return try {
            val userDoc = firestore.collection(USERS_COLLECTION)
                .document(currentUser.uid)
                .get()
                .await()

            userDoc.toObject(User::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao buscar dados do usuário", e)
            null
        }
    }

    suspend fun updateUser(user: User): Boolean {
        val currentUser = auth.currentUser ?: return false

        return try {
            firestore.collection(USERS_COLLECTION)
                .document(currentUser.uid)
                .set(user)
                .await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao atualizar usuário", e)
            false
        }
    }

    suspend fun updateUserName(newName: String): Boolean {
        val currentUser = auth.currentUser ?: return false

        return try {
            firestore.collection(USERS_COLLECTION)
                .document(currentUser.uid)
                .update("name", newName)
                .await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao atualizar nome do usuário", e)
            false
        }
    }

    suspend fun updateUserProfileImage(imageUrl: String): Boolean {
        val currentUser = auth.currentUser ?: return false

        return try {
            firestore.collection(USERS_COLLECTION)
                .document(currentUser.uid)
                .update("profileImageUrl", imageUrl)
                .await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao atualizar foto do usuário", e)
            false
        }
    }
}
