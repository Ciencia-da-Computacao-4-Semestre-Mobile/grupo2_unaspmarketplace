package com.unasp.unaspmarketplace.utils

import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

/**
 * Utilitário para gerenciar vinculação de provedores de autenticação
 */
object AccountLinkingHelper {

    /**
     * Verifica se o usuário atual fez login com Google
     */
    fun isGoogleAccount(user: FirebaseUser? = null): Boolean {
        val firebaseUser = user ?: FirebaseAuth.getInstance().currentUser
        return firebaseUser?.providerData?.any { it.providerId == "google.com" } == true
    }

    /**
     * Verifica se o usuário tem senha vinculada (provider de email/senha)
     */
    fun hasPasswordProvider(user: FirebaseUser? = null): Boolean {
        val firebaseUser = user ?: FirebaseAuth.getInstance().currentUser
        return firebaseUser?.providerData?.any { it.providerId == "password" } == true
    }

    /**
     * Verifica se o usuário pode fazer login com email/senha
     */
    fun canLoginWithPassword(user: FirebaseUser? = null): Boolean {
        return hasPasswordProvider(user)
    }

    /**
     * Obtém informações dos provedores de autenticação
     */
    fun getProviderInfo(user: FirebaseUser? = null): String {
        val firebaseUser = user ?: FirebaseAuth.getInstance().currentUser ?: return "Usuário não logado"

        val providers = firebaseUser.providerData.map { provider ->
            when (provider.providerId) {
                "google.com" -> "Google (${provider.email})"
                "password" -> "Email/Senha (${provider.email})"
                "facebook.com" -> "Facebook (${provider.email})"
                else -> provider.providerId
            }
        }.joinToString("\n")

        return if (providers.isNotEmpty()) providers else "Nenhum provedor encontrado"
    }

    /**
     * Vincula uma senha à conta Google existente
     */
    fun linkPasswordToGoogleAccount(
        email: String,
        password: String,
        onResult: (success: Boolean, message: String) -> Unit
    ) {
        val user = FirebaseAuth.getInstance().currentUser

        if (user == null) {
            onResult(false, "Usuário não está logado")
            return
        }

        if (user.email != email) {
            onResult(false, "Email não confere com a conta atual")
            return
        }

        if (!isGoogleAccount(user)) {
            onResult(false, "Esta conta não é do Google")
            return
        }

        if (hasPasswordProvider(user)) {
            onResult(false, "Esta conta já possui senha vinculada")
            return
        }

        val credential = EmailAuthProvider.getCredential(email, password)

        user.linkWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, "Senha vinculada com sucesso! Agora você pode fazer login com email/senha ou Google.")
                } else {
                    val error = when (task.exception?.message) {
                        "The email address is already in use by another account." ->
                            "Este email já está sendo usado por outra conta."
                        else ->
                            "Erro ao vincular senha: ${task.exception?.message}"
                    }
                    onResult(false, error)
                }
            }
    }

    /**
     * Tenta fazer logout e permitir novo login
     */
    fun testLoginWithNewPassword(
        email: String,
        password: String,
        onResult: (success: Boolean, message: String) -> Unit
    ) {
        val originalUser = FirebaseAuth.getInstance().currentUser
        val originalUid = originalUser?.uid

        // Fazer logout temporário
        FirebaseAuth.getInstance().signOut()

        // Tentar login com email/senha
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val newUser = FirebaseAuth.getInstance().currentUser
                    if (newUser?.uid == originalUid) {
                        onResult(true, "Login com senha funcionando corretamente!")
                    } else {
                        onResult(false, "Erro: UID diferente após login com senha")
                    }
                } else {
                    onResult(false, "Erro no login com senha: ${task.exception?.message}")
                }
            }
    }
}
