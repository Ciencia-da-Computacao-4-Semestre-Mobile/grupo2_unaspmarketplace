package com.unasp.unaspmarketplace.utils

import android.app.Activity
import android.app.AlertDialog
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.OAuthProvider
import com.unasp.unaspmarketplace.auth.AccountLinkingHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

object GitHubLinkingUtils {
    private val auth = FirebaseAuth.getInstance()

    /**
     * Verifica se o usuário pode vincular GitHub e oferece a opção
     */
    fun checkAndOfferGitHubLinking(activity: Activity) {
        val currentUser = auth.currentUser
        if (currentUser == null) return

        // Verificar se já tem GitHub vinculado
        val hasGitHub = AccountLinkingHelper.hasGitHubLinked()
        if (hasGitHub) {
            Log.d("GitHubLinking", "User already has GitHub linked")
            return
        }

        // Oferecer vincular GitHub
        AlertDialog.Builder(activity)
            .setTitle("Vincular GitHub")
            .setMessage(
                "Você pode vincular sua conta GitHub para ter mais opções de login.\n\n" +
                "Deseja vincular agora?"
            )
            .setPositiveButton("Sim, vincular") { _, _ ->
                linkGitHubToCurrentUser(activity)
            }
            .setNegativeButton("Agora não") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    /**
     * Vincula GitHub à conta atual do usuário
     */
    private fun linkGitHubToCurrentUser(activity: Activity) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val result = AccountLinkingHelper.linkGitHubAccount(activity)

                if (result.isSuccess) {
                    AlertDialog.Builder(activity)
                        .setTitle("GitHub Vinculado!")
                        .setMessage("Sua conta GitHub foi vinculada com sucesso! Agora você pode fazer login com GitHub ou com seu método original.")
                        .setPositiveButton("Ótimo!") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .show()
                } else {
                    val error = result.exceptionOrNull()
                    Log.e("GitHubLinking", "Error linking GitHub", error)

                    AlertDialog.Builder(activity)
                        .setTitle("Erro ao Vincular")
                        .setMessage("Não foi possível vincular o GitHub: ${error?.message}")
                        .setPositiveButton("OK") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .show()
                }
            } catch (e: Exception) {
                Log.e("GitHubLinking", "Error in linking process", e)
                AlertDialog.Builder(activity)
                    .setTitle("Erro")
                    .setMessage("Erro durante o processo de vinculação: ${e.message}")
                    .setPositiveButton("OK") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            }
        }
    }

    /**
     * Mostra informações sobre contas vinculadas
     */
    fun showLinkedAccounts(activity: Activity) {
        val currentUser = auth.currentUser ?: return

        val providers = currentUser.providerData.map { provider ->
            when (provider.providerId) {
                "google.com" -> "Google (${provider.email})"
                "github.com" -> "GitHub (${provider.email})"
                "password" -> "Email/Senha (${provider.email})"
                "facebook.com" -> "Facebook (${provider.email})"
                else -> "${provider.providerId} (${provider.email})"
            }
        }.filter { it.isNotEmpty() }

        val message = if (providers.isNotEmpty()) {
            "Contas vinculadas:\n\n" + providers.joinToString("\n• ", "• ")
        } else {
            "Nenhuma conta vinculada encontrada."
        }

        AlertDialog.Builder(activity)
            .setTitle("Contas Vinculadas")
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .setNeutralButton("Vincular GitHub") { _, _ ->
                if (!AccountLinkingHelper.hasGitHubLinked()) {
                    linkGitHubToCurrentUser(activity)
                } else {
                    AlertDialog.Builder(activity)
                        .setTitle("GitHub já vinculado")
                        .setMessage("Sua conta GitHub já está vinculada!")
                        .setPositiveButton("OK") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .show()
                }
            }
            .show()
    }
}
