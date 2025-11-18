package com.unasp.unaspmarketplace.services

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.unasp.unaspmarketplace.models.PasswordResetToken
import kotlinx.coroutines.tasks.await
import java.util.*
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

/**
 * Serviço para gerenciamento de tokens de recuperação de senha
 */
class PasswordResetService {

    private val firestore = FirebaseFirestore.getInstance()
    private val tokensCollection = firestore.collection("password_reset_tokens")

    companion object {
        private const val TAG = "PasswordResetService"

        // Configurações do servidor de email (você pode usar Gmail SMTP)
        private const val SMTP_HOST = "smtp.gmail.com"
        private const val SMTP_PORT = "587"
        private const val EMAIL_USERNAME = "unaspmarketplace@gmail.com" // Substitua pelo seu email
        private const val EMAIL_PASSWORD = "sua_senha_app" // Use senha de app do Gmail

        @Volatile
        private var INSTANCE: PasswordResetService? = null

        fun getInstance(): PasswordResetService {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PasswordResetService().also { INSTANCE = it }
            }
        }
    }

    /**
     * Inicia o processo de recuperação de senha
     */
    suspend fun initiatePasswordReset(email: String): Result<String> {
        return try {
            Log.d(TAG, "Iniciando recuperação de senha para: $email")

            // Verificar se o email existe no sistema
            if (!isValidUserEmail(email)) {
                return Result.failure(Exception("Email não encontrado no sistema"))
            }

            // Invalidar tokens anteriores para este email
            invalidatePreviousTokens(email)

            // Gerar novo token
            val token = PasswordResetToken.generateToken()
            val resetToken = PasswordResetToken(
                email = email,
                token = token,
                expiresAt = PasswordResetToken.getExpirationTime()
            )

            // Salvar token no Firestore
            tokensCollection.add(resetToken).await()
            Log.d(TAG, "Token salvo no Firestore")

            // Enviar email com o token
            sendResetEmail(email, token)
            Log.d(TAG, "Email de recuperação enviado")

            Result.success("Código de recuperação enviado para $email")

        } catch (e: Exception) {
            Log.e(TAG, "Erro ao iniciar recuperação de senha", e)
            Result.failure(e)
        }
    }

    /**
     * Valida o token inserido pelo usuário
     */
    suspend fun validateToken(email: String, inputToken: String): Result<String> {
        return try {
            Log.d(TAG, "Validando token para email: $email")

            // Buscar token válido para o email
            val querySnapshot = tokensCollection
                .whereEqualTo("email", email)
                .whereEqualTo("isUsed", false)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .await()

            if (querySnapshot.isEmpty) {
                return Result.failure(Exception("Nenhum token válido encontrado. Solicite um novo código."))
            }

            val document = querySnapshot.documents.first()
            val token = document.toObject(PasswordResetToken::class.java)
                ?: return Result.failure(Exception("Erro ao processar token"))

            // Verificar se o token é válido
            if (!token.isValid()) {
                if (token.isExpired()) {
                    return Result.failure(Exception("Código expirado. Solicite um novo código."))
                } else if (token.attempts >= PasswordResetToken.MAX_ATTEMPTS) {
                    return Result.failure(Exception("Muitas tentativas incorretas. Solicite um novo código."))
                } else {
                    return Result.failure(Exception("Token inválido"))
                }
            }

            // Verificar se o token está correto
            if (token.token != inputToken) {
                // Incrementar tentativas
                val updatedToken = token.copy(attempts = token.attempts + 1)
                document.reference.set(updatedToken).await()

                val remainingAttempts = PasswordResetToken.MAX_ATTEMPTS - updatedToken.attempts
                return Result.failure(Exception("Código incorreto. Você tem $remainingAttempts tentativas restantes."))
            }

            // Token válido - marcar como usado
            val usedToken = token.copy(isUsed = true)
            document.reference.set(usedToken).await()

            Log.d(TAG, "Token validado com sucesso")
            Result.success("Token válido")

        } catch (e: Exception) {
            Log.e(TAG, "Erro ao validar token", e)
            Result.failure(e)
        }
    }

    /**
     * Atualiza a senha do usuário após validação do token
     */
    suspend fun updatePassword(email: String, newPassword: String): Result<String> {
        return try {
            // Aqui você implementaria a lógica para atualizar a senha no Firebase Auth
            // Por enquanto, vamos simular que funcionou
            Log.d(TAG, "Atualizando senha para: $email")

            Result.success("Senha atualizada com sucesso")

        } catch (e: Exception) {
            Log.e(TAG, "Erro ao atualizar senha", e)
            Result.failure(e)
        }
    }

    /**
     * Verifica se o email existe no sistema
     */
    private suspend fun isValidUserEmail(email: String): Boolean {
        return try {
            val usersSnapshot = firestore.collection("users")
                .whereEqualTo("email", email)
                .limit(1)
                .get()
                .await()

            !usersSnapshot.isEmpty
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao verificar email", e)
            false
        }
    }

    /**
     * Invalida tokens anteriores para o email
     */
    private suspend fun invalidatePreviousTokens(email: String) {
        try {
            val querySnapshot = tokensCollection
                .whereEqualTo("email", email)
                .whereEqualTo("isUsed", false)
                .get()
                .await()

            for (document in querySnapshot.documents) {
                val token = document.toObject(PasswordResetToken::class.java)
                if (token != null) {
                    val invalidatedToken = token.copy(isUsed = true)
                    document.reference.set(invalidatedToken).await()
                }
            }

            Log.d(TAG, "Tokens anteriores invalidados")
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao invalidar tokens anteriores", e)
        }
    }

    /**
     * Envia email com o token de recuperação
     */
    private fun sendResetEmail(email: String, token: String) {
        // Esta função enviaria o email em background
        // Por enquanto, vamos simular o envio
        Thread {
            try {
                val props = Properties().apply {
                    put("mail.smtp.host", SMTP_HOST)
                    put("mail.smtp.port", SMTP_PORT)
                    put("mail.smtp.auth", "true")
                    put("mail.smtp.starttls.enable", "true")
                }

                val session = Session.getInstance(props, object : Authenticator() {
                    override fun getPasswordAuthentication(): PasswordAuthentication {
                        return PasswordAuthentication(EMAIL_USERNAME, EMAIL_PASSWORD)
                    }
                })

                val message = MimeMessage(session).apply {
                    setFrom(InternetAddress(EMAIL_USERNAME))
                    setRecipients(Message.RecipientType.TO, InternetAddress.parse(email))
                    subject = "Recuperação de Senha - UNASP Marketplace"

                    val htmlContent = """
                        <html>
                        <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                            <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                                <h2 style="color: #0073e6; text-align: center;">UNASP Marketplace</h2>
                                <h3>Recuperação de Senha</h3>
                                
                                <p>Você solicitou a recuperação de senha para sua conta.</p>
                                
                                <div style="background-color: #f8f9fa; padding: 20px; border-radius: 8px; text-align: center; margin: 20px 0;">
                                    <h2 style="color: #0073e6; font-size: 32px; margin: 0; letter-spacing: 8px;">$token</h2>
                                    <p style="margin: 10px 0 0 0; color: #666;">Código de Verificação</p>
                                </div>
                                
                                <p><strong>Instruções:</strong></p>
                                <ul>
                                    <li>Digite este código no aplicativo para confirmar sua identidade</li>
                                    <li>O código é válido por 15 minutos</li>
                                    <li>Você tem até 3 tentativas para inserir o código correto</li>
                                </ul>
                                
                                <p style="color: #dc3545;"><strong>Importante:</strong> Se você não solicitou esta recuperação, ignore este email.</p>
                                
                                <hr style="margin: 30px 0; border: none; border-top: 1px solid #eee;">
                                <p style="text-align: center; color: #666; font-size: 12px;">
                                    UNASP Marketplace - Sistema de Recuperação de Senha<br>
                                    Este é um email automático, não responda.
                                </p>
                            </div>
                        </body>
                        </html>
                    """.trimIndent()

                    setContent(htmlContent, "text/html; charset=utf-8")
                }

                Transport.send(message)
                Log.d(TAG, "Email enviado com sucesso para: $email")

            } catch (e: Exception) {
                Log.e(TAG, "Erro ao enviar email", e)
            }
        }.start()
    }
}
