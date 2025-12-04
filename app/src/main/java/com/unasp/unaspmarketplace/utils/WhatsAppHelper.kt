package com.unasp.unaspmarketplace.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import android.util.Log

object WhatsAppHelper {

    private const val TAG = "WhatsAppHelper"

    // Método com número padrão (compatibilidade)
    fun sendMessage(context: Context, message: String) {
        sendMessage(context, message, Constants.UNASP_STORE_PHONE)
    }

    // Método com número específico
    fun sendMessage(context: Context, message: String, phoneNumber: String) {
        try {
            // Limpar e formatar o número
            val cleanPhone = cleanPhoneNumber(phoneNumber)

            // Método mais simples - tentar abrir diretamente
            val url = "https://wa.me/$cleanPhone?text=${Uri.encode(message)}"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))

            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
                Log.d(TAG, "WhatsApp aberto com sucesso para: $cleanPhone")
            } else {
                // Se não conseguir, copiar mensagem
                copyToClipboard(context, message)
                Toast.makeText(context,
                    "WhatsApp não encontrado. Mensagem copiada!\nEnvie para: $cleanPhone",
                    Toast.LENGTH_LONG).show()
            }

        } catch (e: Exception) {
            Log.e(TAG, "Erro ao abrir WhatsApp", e)
            copyToClipboard(context, message)
            val cleanPhone = cleanPhoneNumber(phoneNumber)
            Toast.makeText(context,
                "Erro ao abrir WhatsApp. Mensagem copiada!\nEnvie para: $cleanPhone",
                Toast.LENGTH_LONG).show()
        }
    }

    private fun cleanPhoneNumber(phone: String): String {
        // Remove todos os caracteres não numéricos
        val numbersOnly = phone.replace(Regex("[^0-9]"), "")

        // Se não começar com 55 (código do Brasil), adicionar
        return if (numbersOnly.length >= 10 && !numbersOnly.startsWith("55")) {
            "55$numbersOnly"
        } else {
            numbersOnly
        }
    }

    private fun copyToClipboard(context: Context, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        val clip = android.content.ClipData.newPlainText("Pedido UNASP", text)
        clipboard.setPrimaryClip(clip)
    }
}
