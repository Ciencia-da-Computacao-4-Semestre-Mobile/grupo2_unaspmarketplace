package com.unasp.unaspmarketplace.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import android.util.Log

object WhatsAppManager {
    private const val TAG = "WhatsAppManager"
    private const val PHONE_NUMBER = "5515998765432" // Número da UNASP Store

    fun sendOrderToWhatsApp(context: Context, orderMessage: String) {
        Log.d(TAG, "Tentando enviar para número: $PHONE_NUMBER")

        // Criar URL do WhatsApp
        val whatsappUrl = "https://wa.me/$PHONE_NUMBER?text=${Uri.encode(orderMessage)}"
        Log.d(TAG, "URL gerada: $whatsappUrl")

        // Método 1: Tentar abrir WhatsApp sem especificar package (mais genérico)
        val intentGeneric = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(whatsappUrl)
        }

        // Verificar se algum app pode lidar com wa.me
        val packageManager = context.packageManager
        val activities = packageManager.queryIntentActivities(intentGeneric, 0)

        // Procurar por WhatsApp nas atividades disponíveis
        val whatsappActivity = activities.find {
            it.activityInfo.packageName.contains("whatsapp")
        }

        if (whatsappActivity != null) {
            Log.d(TAG, "Encontrou WhatsApp: ${whatsappActivity.activityInfo.packageName}")
            intentGeneric.setPackage(whatsappActivity.activityInfo.packageName)
            context.startActivity(intentGeneric)
            return
        }

        // Método 2: Tentar WhatsApp normal
        val intentNormal = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(whatsappUrl)
            setPackage("com.whatsapp")
        }

        if (intentNormal.resolveActivity(context.packageManager) != null) {
            Log.d(TAG, "Abrindo WhatsApp normal")
            context.startActivity(intentNormal)
            return
        }

        // Método 3: Tentar WhatsApp Business
        val intentBusiness = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(whatsappUrl)
            setPackage("com.whatsapp.w4b")
        }

        if (intentBusiness.resolveActivity(context.packageManager) != null) {
            Log.d(TAG, "Abrindo WhatsApp Business")
            context.startActivity(intentBusiness)
            return
        }

        // Método 4: Tentar direct intent para WhatsApp
        try {
            val directIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, orderMessage)
                putExtra("jid", "$PHONE_NUMBER@s.whatsapp.net")
                type = "text/plain"
                setPackage("com.whatsapp")
            }

            if (directIntent.resolveActivity(context.packageManager) != null) {
                Log.d(TAG, "Usando intent direto do WhatsApp")
                context.startActivity(directIntent)
                return
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao tentar intent direto", e)
        }

        // Método 5: Se não tiver WhatsApp, abrir no navegador
        try {
            val browserIntent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(whatsappUrl)
            }
            context.startActivity(browserIntent)
            copyToClipboard(context, orderMessage)
            Toast.makeText(context, "WhatsApp não instalado. Abrindo no navegador e copiando mensagem.", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao abrir WhatsApp", e)
            copyToClipboard(context, orderMessage)
            Toast.makeText(context, "Erro ao abrir WhatsApp. Mensagem copiada para área de transferência.", Toast.LENGTH_LONG).show()
        }
    }

    private fun copyToClipboard(context: Context, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        val clip = android.content.ClipData.newPlainText("Pedido UNASP Marketplace", text)
        clipboard.setPrimaryClip(clip)
    }
}
