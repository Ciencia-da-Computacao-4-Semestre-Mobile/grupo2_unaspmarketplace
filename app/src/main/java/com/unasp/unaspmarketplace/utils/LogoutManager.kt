package com.unasp.unaspmarketplace.utils

import android.app.Activity
import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.unasp.unaspmarketplace.auth.GoogleAuthHelper

/**
 * Utilitário para gerenciar logout de forma centralizada
 */
object LogoutManager {

    /**
     * Realiza logout completo do aplicativo
     * - Firebase Auth
     * - Google Sign In
     * - Login Preferences
     */
    fun performCompleteLogout(context: Context) {
        try {
            // 1. Logout do Firebase Auth
            FirebaseAuth.getInstance().signOut()

            // 2. Logout do Google Sign In (apenas se context for uma Activity)
            if (context is Activity) {
                val googleSignInClient = GoogleAuthHelper.getClient(context)
                googleSignInClient.signOut()
            }

            // 3. Limpar preferências de login
            val loginPreferences = LoginPreferences(context)
            loginPreferences.clearAllPreferences()

            android.util.Log.d("LogoutManager", "Logout completo realizado com sucesso")

        } catch (e: Exception) {
            android.util.Log.e("LogoutManager", "Erro durante logout", e)
        }
    }

    /**
     * Realiza logout mas mantém as credenciais salvas se "lembrar de mim" estiver ativo
     */
    fun performSoftLogout(context: Context) {
        try {
            // 1. Logout do Firebase Auth
            FirebaseAuth.getInstance().signOut()

            // 2. Logout do Google Sign In (apenas se context for uma Activity)
            if (context is Activity) {
                val googleSignInClient = GoogleAuthHelper.getClient(context)
                googleSignInClient.signOut()
            }

            // 3. Manter preferências de login se "lembrar de mim" estiver ativo
            val loginPreferences = LoginPreferences(context)
            if (!loginPreferences.isRememberMeEnabled()) {
                loginPreferences.clearAllPreferences()
            }

            android.util.Log.d("LogoutManager", "Soft logout realizado com sucesso")

        } catch (e: Exception) {
            android.util.Log.e("LogoutManager", "Erro durante soft logout", e)
        }
    }
}

