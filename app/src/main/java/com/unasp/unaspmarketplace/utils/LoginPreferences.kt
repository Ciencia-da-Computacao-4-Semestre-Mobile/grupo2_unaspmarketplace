package com.unasp.unaspmarketplace.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * Classe para gerenciar as preferências relacionadas ao login,
 * incluindo a funcionalidade "Lembrar de mim"
 */
class LoginPreferences(context: Context) {

    companion object {
        private const val PREFS_NAME = "login_preferences"
        private const val KEY_REMEMBER_ME = "remember_me"
        private const val KEY_SAVED_EMAIL = "saved_email"
        private const val KEY_SAVED_PASSWORD = "saved_password"
        private const val KEY_AUTO_LOGIN_ENABLED = "auto_login_enabled"
        private const val KEY_LAST_LOGIN_TIME = "last_login_time"

        // Constantes para tempo de expiração
        private const val REMEMBER_DURATION_DAYS = 30 // 30 dias
        private const val REMEMBER_DURATION_MS = REMEMBER_DURATION_DAYS * 24 * 60 * 60 * 1000L
    }

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Verifica se a opção "Lembrar de mim" está ativada
     */
    fun isRememberMeEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_REMEMBER_ME, false)
    }

    /**
     * Define se a opção "Lembrar de mim" está ativada
     */
    fun setRememberMe(remember: Boolean) {
        sharedPreferences.edit()
            .putBoolean(KEY_REMEMBER_ME, remember)
            .apply()
    }

    /**
     * Salva as credenciais quando "Lembrar de mim" está ativado
     */
    fun saveCredentials(email: String, password: String) {
        sharedPreferences.edit()
            .putString(KEY_SAVED_EMAIL, email)
            .putString(KEY_SAVED_PASSWORD, password)
            .putLong(KEY_LAST_LOGIN_TIME, System.currentTimeMillis())
            .apply()
    }

    /**
     * Recupera o email salvo
     */
    fun getSavedEmail(): String? {
        return if (isRememberDataValid()) {
            sharedPreferences.getString(KEY_SAVED_EMAIL, null)
        } else {
            clearSavedCredentials()
            null
        }
    }

    /**
     * Recupera a senha salva
     */
    fun getSavedPassword(): String? {
        return if (isRememberDataValid()) {
            sharedPreferences.getString(KEY_SAVED_PASSWORD, null)
        } else {
            clearSavedCredentials()
            null
        }
    }

    /**
     * Verifica se o auto-login está habilitado
     */
    fun isAutoLoginEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_AUTO_LOGIN_ENABLED, false) &&
               isRememberDataValid()
    }

    /**
     * Define se o auto-login está habilitado
     */
    fun setAutoLoginEnabled(enabled: Boolean) {
        sharedPreferences.edit()
            .putBoolean(KEY_AUTO_LOGIN_ENABLED, enabled)
            .apply()
    }

    /**
     * Verifica se os dados salvos ainda são válidos (não expiraram)
     */
    private fun isRememberDataValid(): Boolean {
        val lastLoginTime = sharedPreferences.getLong(KEY_LAST_LOGIN_TIME, 0)
        val currentTime = System.currentTimeMillis()
        return (currentTime - lastLoginTime) <= REMEMBER_DURATION_MS
    }

    /**
     * Limpa todas as credenciais salvas
     */
    fun clearSavedCredentials() {
        sharedPreferences.edit()
            .remove(KEY_SAVED_EMAIL)
            .remove(KEY_SAVED_PASSWORD)
            .remove(KEY_LAST_LOGIN_TIME)
            .remove(KEY_AUTO_LOGIN_ENABLED)
            .apply()
    }

    /**
     * Limpa todas as preferências de login
     */
    fun clearAllPreferences() {
        sharedPreferences.edit().clear().apply()
    }

    /**
     * Atualiza o tempo do último login (para renovar o período de lembrar)
     */
    fun updateLastLoginTime() {
        sharedPreferences.edit()
            .putLong(KEY_LAST_LOGIN_TIME, System.currentTimeMillis())
            .apply()
    }

    /**
     * Verifica se existem credenciais salvas
     */
    fun hasSavedCredentials(): Boolean {
        return !getSavedEmail().isNullOrEmpty() &&
               !getSavedPassword().isNullOrEmpty()
    }

    /**
     * Salva as configurações completas de login
     */
    fun saveLoginSession(email: String, password: String, rememberMe: Boolean, autoLogin: Boolean = false) {
        sharedPreferences.edit()
            .putBoolean(KEY_REMEMBER_ME, rememberMe)
            .putBoolean(KEY_AUTO_LOGIN_ENABLED, autoLogin)
            .putLong(KEY_LAST_LOGIN_TIME, System.currentTimeMillis())
            .apply()

        if (rememberMe) {
            saveCredentials(email, password)
        } else {
            clearSavedCredentials()
        }
    }
}
