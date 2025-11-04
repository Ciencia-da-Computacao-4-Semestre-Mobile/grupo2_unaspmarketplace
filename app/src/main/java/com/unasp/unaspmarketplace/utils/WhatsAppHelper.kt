package com.unasp.unaspmarketplace.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import android.util.Log

object WhatsAppHelper {

    private const val TAG = "WhatsAppHelper"

    fun sendMessage(context: Context, message: String) {
        val phoneNumber = Constants.UNASP_STORE_PHONE

        try {
            // Método mais simples - tentar abrir diretamente
            val url = "https://wa.me/$phoneNumber?text=${Uri.encode(message)}"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))

            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
                Log.d(TAG, "WhatsApp aberto com sucesso")
            } else {
                // Se não conseguir, copiar mensagem
                copyToClipboard(context, message)
                Toast.makeText(context,
                    "WhatsApp não encontrado. Mensagem copiada!\nEnvie para: $phoneNumber",
                    Toast.LENGTH_LONG).show()
            }

        } catch (e: Exception) {
            Log.e(TAG, "Erro ao abrir WhatsApp", e)
            copyToClipboard(context, message)
            Toast.makeText(context,
                "Erro ao abrir WhatsApp. Mensagem copiada!\nEnvie para: $phoneNumber",
                Toast.LENGTH_LONG).show()
        }
    }

    private fun copyToClipboard(context: Context, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        val clip = android.content.ClipData.newPlainText("Pedido UNASP", text)
        clipboard.setPrimaryClip(clip)
    }
}
