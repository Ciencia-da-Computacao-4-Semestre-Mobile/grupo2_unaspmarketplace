package com.unasp.unaspmarketplace.auth

import android.R
import android.app.Activity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider


object GoogleAuthHelper {
    fun getClient(activity: Activity): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(activity.getString(R.string.defaultMsisdnAlphaTag))
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(activity, gso)
    }

    fun firebaseAuthWithGoogle(idToken: String, onResult: (Boolean, String?) -> Unit) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val auth = FirebaseAuth.getInstance()

        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    println("✅ Login Google bem-sucedido: ${user?.email}")
                    onResult(true, null)
                } else {
                    println("❌ Erro login Google: ${task.exception?.message}")
                    onResult(false, task.exception?.message)
                }
            }
    }

    fun signOut(activity: Activity, onComplete: () -> Unit = {}) {
        getClient(activity).signOut().addOnCompleteListener {
            FirebaseAuth.getInstance().signOut()
            onComplete()
        }
    }

    fun revokeAccess(activity: Activity, onComplete: () -> Unit = {}) {
        getClient(activity).revokeAccess().addOnCompleteListener {
            FirebaseAuth.getInstance().signOut()
            onComplete()
        }
    }
}