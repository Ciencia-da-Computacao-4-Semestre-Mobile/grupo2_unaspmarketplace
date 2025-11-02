package com.unasp.unaspmarketplace.auth

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.unasp.unaspmarketplace.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val userRepository = UserRepository()

    fun registerUser(name: String, email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        val profileUpdates = UserProfileChangeRequest.Builder()
                            .setDisplayName(name)
                            .build()

                        user.updateProfile(profileUpdates).addOnCompleteListener { updateTask ->
                            if (updateTask.isSuccessful) {
                                CoroutineScope(Dispatchers.IO).launch {
                                    try {
                                        val result = userRepository.registerUser(name, email, password)
                                        if (result.isSuccess) {
                                            onResult(true, null)
                                        } else {
                                            onResult(false, result.exceptionOrNull()?.message)
                                        }
                                    } catch (e: Exception) {
                                        onResult(false, e.message)
                                    }
                                }
                            } else {
                                onResult(false, updateTask.exception?.message)
                            }
                        }
                    } else {
                        onResult(false, "Erro ao criar usuário")
                    }
                } else {
                    onResult(false, task.exception?.message)
                }
            }
    }

    fun loginUser(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = userRepository.loginUser(email, password)
                if (result.isSuccess) {
                    onResult(true, null)
                } else {
                    onResult(false, result.exceptionOrNull()?.message)
                }
            } catch (e: Exception) {
                onResult(false, e.message)
            }
        }
    }

    fun loginWithGitHub(onResult: (Boolean, String?) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val existingUser = userRepository.getCurrentUser()
                if (existingUser == null) {
                    // Criar usuário no Firestore se não existir
                    val user = auth.currentUser
                    if (user != null) {
                        val result = userRepository.registerUser(
                            name = user.displayName ?: "",
                            email = user.email ?: "",
                            password = "" // GitHub login não precisa de senha
                        )
                        if (result.isSuccess) {
                            onResult(true, null)
                        } else {
                            onResult(false, result.exceptionOrNull()?.message)
                        }
                    } else {
                        onResult(false, "Usuário não encontrado")
                    }
                } else {
                    onResult(true, null)
                }
            } catch (e: Exception) {
                onResult(false, e.message)
            }
        }
    }
}
