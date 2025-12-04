package com.unasp.unaspmarketplace.utils

object Constants {
    // Configurações da UNASP Store
    const val UNASP_STORE_PHONE = "5511990261518" // Número da UNASP Store
    const val UNASP_STORE_NAME = "UNASP Store"

    // Mensagens padrão
    const val WHATSAPP_NOT_FOUND = "WhatsApp não está instalado. Será aberto no navegador."
    const val ORDER_SUCCESS = "Pedido enviado com sucesso!"

    // Formatos
    const val DATE_FORMAT = "dd/MM/yyyy HH:mm"
    const val CURRENCY_FORMAT = "R$ %.2f"

    // Debug
    fun getWhatsAppUrl(message: String): String {
        return "https://wa.me/$UNASP_STORE_PHONE?text=${android.net.Uri.encode(message)}"
    }
}
