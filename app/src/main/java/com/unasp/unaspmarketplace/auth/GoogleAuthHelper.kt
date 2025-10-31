package com.unasp.unaspmarketplace.auth

import android.app.Activity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.unasp.unaspmarketplace.R

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

        auth.signInWithCredential(credential)
            .addOnCompleteListener { task: Task<AuthResult> ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    println("Login Google bem-sucedido: ${user?.email}")

                    // Save user data to Firestore if UserRepository exists
                    user?.let { firebaseUser ->
                        try {
                            // You can add UserRepository.createUser() here if needed
                        } catch (e: Exception) {
                            println("Erro ao salvar dados do usuário: ${e.message}")
                        }
                    }

                    onResult(true, null)
                } else {
                    println("Erro login Google: ${task.exception?.message}")
                    onResult(false, task.exception?.message)
                }
            }
    }
}