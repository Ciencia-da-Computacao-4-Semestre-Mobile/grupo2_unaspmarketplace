package com.unasp.unaspmarketplace.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class AuthService {
    private val auth = FirebaseAuth.getInstance()

    fun loginUser(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        Log.d("AuthService", "Attempting to login user with email: $email")

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("AuthService", "Login successful")
                    onResult(true, null)
                } else {
                    Log.e("AuthService", "Login failed", task.exception)
                    onResult(false, task.exception?.message)
                }
            }
    }

    fun getCurrentUser(): FirebaseUser? = auth.currentUser
}