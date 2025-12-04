package com.unasp.unaspmarketplace.auth

import android.app.Activity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.unasp.unaspmarketplace.R
import com.unasp.unaspmarketplace.models.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Suppress("DEPRECATION")
object GoogleAuthHelper {
    fun getClient(activity: Activity): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(activity.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(activity, gso)
    }

    fun firebaseAuthWithGoogle(idToken: String, onResult: (Boolean, String?) -> Unit) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val auth = FirebaseAuth.getInstance()
        val firestore = FirebaseFirestore.getInstance()

        auth.signInWithCredential(credential)
            .addOnCompleteListener { task: Task<AuthResult> ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    println("Login Google bem-sucedido: ${user?.email}")

                    // Criar dados do usuário no Firestore automaticamente
                    user?.let { firebaseUser ->
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                // Verificar se o usuário já existe no Firestore
                                val userDoc = firestore.collection("users")
                                    .document(firebaseUser.uid)
                                    .get()
                                    .await()

                                // Se não existe, criar novo documento
                                if (!userDoc.exists()) {
                                    val userData = User(
                                        id = firebaseUser.uid,
                                        name = firebaseUser.displayName ?: "",
                                        email = firebaseUser.email ?: "",
                                        profileImageUrl = firebaseUser.photoUrl?.toString() ?: "",
                                        createdAt = System.currentTimeMillis(),
                                        isActive = true
                                    )

                                    firestore.collection("users")
                                        .document(firebaseUser.uid)
                                        .set(userData)
                                        .await()

                                    println("Dados do usuário criados no Firestore: ${userData.email}")
                                } else {
                                    println("Usuário já existe no Firestore: ${firebaseUser.email}")
                                }
                            } catch (e: Exception) {
                                println("Erro ao salvar dados do usuário: ${e.message}")
                                // Não falha o login por causa disso
                            }
                        }
                    }

                    onResult(true, null)
                } else {
                    println("Erro login Google: ${task.exception?.message}")
                    onResult(false, task.exception?.message)
                }
            }
    }

    /**
     * Verifica se o usuário já está logado e pode vincular GitHub
     */
    fun isUserLoggedIn(): Boolean {
        return FirebaseAuth.getInstance().currentUser != null
    }

    /**
     * Obtém o email do usuário atual se estiver logado
     */
    fun getCurrentUserEmail(): String? {
        return FirebaseAuth.getInstance().currentUser?.email
    }
}