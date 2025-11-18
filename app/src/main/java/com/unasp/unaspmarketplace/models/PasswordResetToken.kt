package com.unasp.unaspmarketplace.models

import com.google.firebase.Timestamp

/**
 * Modelo para tokens de recuperação de senha
 */
data class PasswordResetToken(
    val email: String = "",
    val token: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val expiresAt: Timestamp = Timestamp.now(),
    val isUsed: Boolean = false,
    val attempts: Int = 0
) {
    companion object {
        // Token válido por 15 minutos
        const val TOKEN_VALIDITY_MINUTES = 15
        // Máximo de 3 tentativas de validação
        const val MAX_ATTEMPTS = 3

        /**
         * Gera um token de 5 dígitos aleatório
         */
        fun generateToken(): String {
            return (10000..99999).random().toString()
        }

        /**
         * Calcula o timestamp de expiração
         */
        fun getExpirationTime(): Timestamp {
            val currentTime = System.currentTimeMillis()
            val expirationTime = currentTime + (TOKEN_VALIDITY_MINUTES * 60 * 1000)
            return Timestamp(expirationTime / 1000, ((expirationTime % 1000) * 1000000).toInt())
        }
    }

    /**
     * Verifica se o token ainda é válido
     */
    fun isValid(): Boolean {
        val now = Timestamp.now()
        return !isUsed &&
               attempts < MAX_ATTEMPTS &&
               now.seconds <= expiresAt.seconds
    }

    /**
     * Verifica se o token expirou
     */
    fun isExpired(): Boolean {
        return Timestamp.now().seconds > expiresAt.seconds
    }
}
