package com.unasp.unaspmarketplace.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.unasp.unaspmarketplace.models.User
import kotlinx.coroutines.tasks.await

class UserRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users")

    suspend fun registerUser(name: String, email: String, password: String): Result<User> {
        return try {
            // Criar usuário no Firebase Auth
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user

            if (firebaseUser != null) {
                // Criar objeto User
                val user = User(
                    id = firebaseUser.uid,
                    name = name,
                    email = email,
                    profileImageUrl = "",
                    createdAt = System.currentTimeMillis(),
                    isActive = true
                )

                // Salvar no Firestore
                usersCollection.document(firebaseUser.uid).set(user).await()

                Result.success(user)
            } else {
                Result.failure(Exception("Falha ao criar usuário"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loginUser(email: String, password: String): Result<User> {
        return try {
            // Fazer login no Firebase Auth
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user

            if (firebaseUser != null) {
                // Buscar dados do usuário no Firestore
                val userDoc = usersCollection.document(firebaseUser.uid).get().await()
                val user = userDoc.toObject(User::class.java)

                if (user != null) {
                    Result.success(user)
                } else {
                    Result.failure(Exception("Dados do usuário não encontrados"))
                }
            } else {
                Result.failure(Exception("Falha no login"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCurrentUser(): User? {
        return try {
            val firebaseUser = auth.currentUser
            if (firebaseUser != null) {
                val userDoc = usersCollection.document(firebaseUser.uid).get().await()
                userDoc.toObject(User::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updateUser(user: User): Result<Unit> {
        return try {
            usersCollection.document(user.id).set(user).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        auth.signOut()
    }

    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }
}
