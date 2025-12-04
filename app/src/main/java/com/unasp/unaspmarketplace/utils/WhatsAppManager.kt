package com.unasp.unaspmarketplace.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import android.util.Log

object WhatsAppManager {

    private const val TAG = "WhatsAppManager"

    fun sendOrderToWhatsApp(context: Context, orderMessage: String) {
        try {
            val phoneNumber = Constants.UNASP_STORE_PHONE
            Log.d(TAG, "Tentando enviar para número: $phoneNumber")

            // Criar URL do WhatsApp
            val whatsappUrl = "https://wa.me/$phoneNumber?text=${Uri.encode(orderMessage)}"
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
            val directIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, orderMessage)
                putExtra("jid", "$phoneNumber@s.whatsapp.net")
                setPackage("com.whatsapp")
                type = "text/plain"
            }

            if (directIntent.resolveActivity(context.packageManager) != null) {
                Log.d(TAG, "Usando intent direto do WhatsApp")
                context.startActivity(directIntent)
                return
            }

            // Método 5: Se não tiver WhatsApp, abrir no navegador
            val intentBrowser = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(whatsappUrl)
            }

            if (intentBrowser.resolveActivity(context.packageManager) != null) {
                Log.d(TAG, "Abrindo no navegador")
                context.startActivity(intentBrowser)
                Toast.makeText(context, "WhatsApp não encontrado. Abrindo no navegador...", Toast.LENGTH_SHORT).show()
            } else {
                Log.w(TAG, "Nenhuma opção funcionou, copiando para clipboard")
                // Fallback: copiar para área de transferência
                copyToClipboard(context, orderMessage)
                showFallbackMessage(context, phoneNumber, orderMessage)
            }

        } catch (e: Exception) {
            Log.e(TAG, "Erro ao abrir WhatsApp", e)
            Toast.makeText(context, "Erro ao abrir WhatsApp: ${e.message}", Toast.LENGTH_LONG).show()
            // Fallback: copiar para área de transferência
            copyToClipboard(context, orderMessage)
            showFallbackMessage(context, Constants.UNASP_STORE_PHONE, orderMessage)
        }
    }

    private fun showFallbackMessage(context: Context, phoneNumber: String, orderMessage: String) {
        Toast.makeText(context,
            "Pedido copiado! Envie manualmente para: $phoneNumber\nOu abra o WhatsApp e cole a mensagem.",
            Toast.LENGTH_LONG).show()
    }

    private fun copyToClipboard(context: Context, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        val clip = android.content.ClipData.newPlainText("Pedido UNASP Marketplace", text)
        clipboard.setPrimaryClip(clip)
    }

    fun isWhatsAppInstalled(context: Context): Boolean {
        val packageManager = context.packageManager
        return try {
            packageManager.getPackageInfo("com.whatsapp", 0)
            true
        } catch (e: Exception) {
            try {
                packageManager.getPackageInfo("com.whatsapp.w4b", 0)
                true
            } catch (e: Exception) {
                false
            }
        }
    }
}

