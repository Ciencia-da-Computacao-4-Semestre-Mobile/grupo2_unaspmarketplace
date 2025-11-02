package com.unasp.unaspmarketplace.auth

import android.app.Activity
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.OAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.unasp.unaspmarketplace.models.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

object GitHubAuthHelper {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    fun signInWithGitHub(activity: Activity, onResult: (Boolean, String?) -> Unit) {
        val provider = OAuthProvider.newBuilder("github.com")
        provider.scopes = listOf("user:email")

        val pendingResultTask = auth.pendingAuthResult
        if (pendingResultTask != null) {
            pendingResultTask
                .addOnSuccessListener { authResult ->
                    Log.d("GitHubAuth", "GitHub login success from pending result")
                    handleSuccessfulAuth(authResult.user, onResult)
                }
                .addOnFailureListener { exception ->
                    Log.e("GitHubAuth", "GitHub login failed from pending result", exception)
                    handleAuthError(exception, activity, onResult)
                }
        } else {
            auth.startActivityForSignInWithProvider(activity, provider.build())
                .addOnSuccessListener { authResult ->
                    Log.d("GitHubAuth", "GitHub login success")
                    handleSuccessfulAuth(authResult.user, onResult)
                }
                .addOnFailureListener { exception ->
                    Log.e("GitHubAuth", "GitHub login failed", exception)
                    handleAuthError(exception, activity, onResult)
                }
        }
    }

    private fun handleAuthError(exception: Exception, activity: Activity, onResult: (Boolean, String?) -> Unit) {
        when (exception) {
            is FirebaseAuthUserCollisionException -> {
                // Tentar fazer login silencioso com o método existente
                Log.d("GitHubAuth", "Account collision detected, attempting silent login")
                trySignInWithExistingAccount(exception.email ?: "", activity, onResult)
            }
            else -> {
                if (exception.message?.contains("account already exists", ignoreCase = true) == true) {
                    // Extrair email e tentar login silencioso
                    val email = extractEmailFromMessage(exception.message ?: "")
                    if (email.isNotEmpty()) {
                        trySignInWithExistingAccount(email, activity, onResult)
                    } else {
                        handleOtherErrors(exception, onResult)
                    }
                } else {
                    handleOtherErrors(exception, onResult)
                }
            }
        }
    }

    private fun trySignInWithExistingAccount(email: String, activity: Activity, onResult: (Boolean, String?) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val currentUser = auth.currentUser

                // Se o usuário já está logado com o mesmo email, considerar como sucesso
                if (currentUser != null && currentUser.email == email) {
                    Log.d("GitHubAuth", "User already logged in with same email, treating as success")
                    handleSuccessfulAuth(currentUser, onResult)
                    return@launch
                }

                // Se não está logado, falha silenciosamente sem incomodar o usuário
                Log.d("GitHubAuth", "Email exists but user not logged in - silent fail")
                onResult(false, "SILENT_FAIL")

            } catch (e: Exception) {
                Log.e("GitHubAuth", "Error in silent login attempt", e)
                onResult(false, "SILENT_FAIL")
            }
        }
    }

    private fun handleOtherErrors(exception: Exception, onResult: (Boolean, String?) -> Unit) {
        val errorMessage = when {
            exception.message?.contains("cancelled", ignoreCase = true) == true -> {
                "Login cancelado pelo usuário."
            }
            exception.message?.contains("network", ignoreCase = true) == true -> {
                "Erro de conexão. Verifique sua internet."
            }
            else -> {
                "Erro no login com GitHub: ${exception.message}"
            }
        }
        onResult(false, errorMessage)
    }

    private fun extractEmailFromMessage(message: String): String {
        val emailRegex = "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}".toRegex()
        return emailRegex.find(message)?.value ?: ""
    }

    private fun handleSuccessfulAuth(user: com.google.firebase.auth.FirebaseUser?, onResult: (Boolean, String?) -> Unit) {
        user?.let { firebaseUser ->
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val userDoc = firestore.collection("users")
                        .document(firebaseUser.uid)
                        .get()
                        .await()

                    if (!userDoc.exists()) {
                        val userData = User(
                            id = firebaseUser.uid,
                            name = firebaseUser.displayName ?: "Usuário GitHub",
                            email = firebaseUser.email ?: "",
                            createdAt = System.currentTimeMillis(),
                            isActive = true
                        )

                        firestore.collection("users")
                            .document(firebaseUser.uid)
                            .set(userData)
                            .await()

                        Log.d("GitHubAuth", "User data created in Firestore: ${userData.email}")
                    } else {
                        Log.d("GitHubAuth", "User already exists in Firestore: ${firebaseUser.email}")
                    }
                } catch (e: Exception) {
                    Log.e("GitHubAuth", "Error saving user data", e)
                }
            }
        }

        onResult(true, null)
    }
}


