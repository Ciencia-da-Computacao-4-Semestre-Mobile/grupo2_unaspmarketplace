package com.unasp.unaspmarketplace.auth

import android.app.Activity
import android.util.Log
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.unasp.unaspmarketplace.models.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

object FacebookAuthHelper {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    fun createCallbackManager(): CallbackManager {
        return CallbackManager.Factory.create()
    }

    fun signInWithFacebook(
        activity: Activity,
        callbackManager: CallbackManager,
        onResult: (Boolean, String?) -> Unit
    ) {
        try {
            // Verificar se já está logado no Facebook
            val currentAccessToken = AccessToken.getCurrentAccessToken()
            if (currentAccessToken != null && !currentAccessToken.isExpired) {
                Log.d("FacebookAuth", "Using existing Facebook token")
                handleFacebookAccessToken(currentAccessToken, onResult)
                return
            }

            // Limpar login anterior
            LoginManager.getInstance().logOut()

            // Fazer novo login
            LoginManager.getInstance().logInWithReadPermissions(
                activity,
                listOf("email", "public_profile")
            )

            LoginManager.getInstance().registerCallback(callbackManager,
                object : FacebookCallback<LoginResult> {
                    override fun onSuccess(result: LoginResult) {
                        Log.d("FacebookAuth", "Facebook login success")
                        handleFacebookAccessToken(result.accessToken, onResult)
                    }

                    override fun onCancel() {
                        Log.d("FacebookAuth", "Facebook login cancelled")
                        onResult(false, "SILENT_FAIL")
                    }

                    override fun onError(error: FacebookException) {
                        Log.e("FacebookAuth", "Facebook login error", error)

                        // Tratar diferentes tipos de erro
                        when {
                            error.message?.contains("User logged in as different Facebook user", ignoreCase = true) == true -> {
                                onResult(false, "SILENT_FAIL")
                            }
                            error.message?.contains("account already exists", ignoreCase = true) == true -> {
                                onResult(false, "SILENT_FAIL")
                            }
                            error.message?.contains("network", ignoreCase = true) == true -> {
                                onResult(false, "Erro de conexão. Verifique sua internet.")
                            }
                            error.message?.contains("URL", ignoreCase = true) == true -> {
                                Log.e("FacebookAuth", "Facebook configuration error - falling back")
                                onResult(false, "SILENT_FAIL")
                            }
                            else -> {
                                onResult(false, "SILENT_FAIL")
                            }
                        }
                    }
                }
            )
        } catch (e: Exception) {
            Log.e("FacebookAuth", "Error setting up Facebook login", e)
            onResult(false, "SILENT_FAIL")
        }
    }

    private fun handleFacebookAccessToken(token: AccessToken, onResult: (Boolean, String?) -> Unit) {
        Log.d("FacebookAuth", "Handling Facebook access token")

        try {
            val credential = FacebookAuthProvider.getCredential(token.token)
            auth.signInWithCredential(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("FacebookAuth", "Firebase authentication successful")
                        val user = auth.currentUser

                        // Criar dados do usuário no Firestore
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
                                            name = firebaseUser.displayName ?: "Usuário Facebook",
                                            email = firebaseUser.email ?: "",
                                            createdAt = System.currentTimeMillis(),
                                            isActive = true
                                        )

                                        firestore.collection("users")
                                            .document(firebaseUser.uid)
                                            .set(userData)
                                            .await()

                                        Log.d("FacebookAuth", "User data created in Firestore: ${userData.email}")
                                    } else {
                                        Log.d("FacebookAuth", "User already exists in Firestore: ${firebaseUser.email}")
                                    }
                                } catch (e: Exception) {
                                    Log.e("FacebookAuth", "Error saving user data", e)
                                }
                            }
                        }

                        onResult(true, null)
                    } else {
                        Log.e("FacebookAuth", "Firebase authentication failed", task.exception)

                        val exception = task.exception
                        if (exception?.message?.contains("account already exists", ignoreCase = true) == true) {
                            onResult(false, "SILENT_FAIL")
                        } else {
                            onResult(false, "SILENT_FAIL")
                        }
                    }
                }
        } catch (e: Exception) {
            Log.e("FacebookAuth", "Error handling Facebook token", e)
            onResult(false, "SILENT_FAIL")
        }
    }
}
