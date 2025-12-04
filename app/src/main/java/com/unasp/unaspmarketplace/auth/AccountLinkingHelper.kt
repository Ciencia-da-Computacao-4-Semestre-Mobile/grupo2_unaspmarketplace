package com.unasp.unaspmarketplace.auth

import android.app.Activity
import android.app.AlertDialog
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.OAuthProvider
import kotlinx.coroutines.tasks.await

object AccountLinkingHelper {
    private val auth = FirebaseAuth.getInstance()

    /**
     * Verifica se existe conflito de contas e oferece opções ao usuário
     */
    fun handleAccountConflict(
        activity: Activity,
        exception: Exception,
        onRetry: () -> Unit,
        onCancel: () -> Unit,
        onLinkAccount: () -> Unit
    ) {
        when (exception) {
            is FirebaseAuthUserCollisionException -> {
                showAccountLinkingDialog(activity, onRetry, onCancel, onLinkAccount)
            }
            else -> {
                if (exception.message?.contains("account already exists", ignoreCase = true) == true) {
                    showAccountLinkingDialog(activity, onRetry, onCancel, onLinkAccount)
                } else {
                    onCancel()
                }
            }
        }
    }

    private fun showAccountLinkingDialog(
        activity: Activity,
        onRetry: () -> Unit,
        onCancel: () -> Unit,
        onLinkAccount: () -> Unit
    ) {
        AlertDialog.Builder(activity)
            .setTitle("Vincular Contas")
            .setMessage(
                "Este email já está associado a uma conta existente.\n\n" +
                "Você pode:\n" +
                "• Vincular sua conta GitHub à conta existente\n" +
                "• Usar o método de login original (Google/Email)\n" +
                "• Ou usar uma conta GitHub diferente"
            )
            .setPositiveButton("Vincular GitHub") { _, _ ->
                onLinkAccount()
            }
            .setNegativeButton("Cancelar") { _, _ ->
                onCancel()
            }
            .setNeutralButton("Login Original") { _, _ ->
                onRetry()
            }
            .setCancelable(false)
            .show()
    }

    /**
     * Vincula uma conta GitHub à conta atual do usuário
     */
    suspend fun linkGitHubAccount(activity: Activity): Result<Boolean> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                return Result.failure(Exception("Usuário não está logado"))
            }

            val provider = OAuthProvider.newBuilder("github.com")
            provider.scopes = listOf("user:email")

            // Iniciar processo de linking via OAuth
            val authResult = auth.startActivityForSignInWithProvider(activity, provider.build()).await()

            // Se chegou aqui, o GitHub foi autenticado - agora vincular
            val githubCredential = authResult.credential
            if (githubCredential != null) {
                currentUser.linkWithCredential(githubCredential).await()
                Log.d("AccountLinking", "GitHub account linked successfully")
                Result.success(true)
            } else {
                Result.failure(Exception("Não foi possível obter credencial do GitHub"))
            }
        } catch (e: Exception) {
            Log.e("AccountLinking", "Error linking GitHub account", e)
            Result.failure(e)
        }
    }

    /**
     * Faz login com o método original e depois tenta vincular GitHub
     */
    suspend fun loginAndLinkGitHub(
        activity: Activity,
        email: String,
        onNeedGoogleLogin: () -> Unit,
        onNeedPasswordLogin: (String) -> Unit
    ): Result<Boolean> {
        return try {
            val signInMethods = getSignInMethodsForEmail(email)

            when {
                signInMethods.isNullOrEmpty() -> {
                    Result.failure(Exception("Email não encontrado"))
                }
                signInMethods.contains("google.com") -> {
                    // Precisa fazer login com Google primeiro
                    onNeedGoogleLogin()
                    Result.success(false) // Não completou ainda
                }
                signInMethods.contains("password") -> {
                    // Precisa fazer login com senha primeiro
                    onNeedPasswordLogin(email)
                    Result.success(false) // Não completou ainda
                }
                else -> {
                    Result.failure(Exception("Método de login não suportado: ${signInMethods.joinToString(", ")}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Tenta fazer logout e permitir novo login
     */
    suspend fun signOutAndRetry(): Boolean {
        return try {
            auth.signOut()
            true
        } catch (e: Exception) {
            Log.e("AccountLinking", "Error signing out", e)
            false
        }
    }

    /**
     * Obtém informações sobre provedores de login existentes para um email
     */
    suspend fun getSignInMethodsForEmail(email: String): List<String>? {
        return try {
            val result = auth.fetchSignInMethodsForEmail(email).await()
            result.signInMethods
        } catch (e: Exception) {
            Log.e("AccountLinking", "Error fetching sign-in methods", e)
            null
        }
    }

    /**
     * Verifica se o usuário atual tem GitHub vinculado
     */
    fun hasGitHubLinked(): Boolean {
        val currentUser = auth.currentUser ?: return false
        return currentUser.providerData.any { it.providerId == "github.com" }
    }

    /**
     * Verifica se o usuário atual tem Google vinculado
     */
    fun hasGoogleLinked(): Boolean {
        val currentUser = auth.currentUser ?: return false
        return currentUser.providerData.any { it.providerId == "google.com" }
    }
}
