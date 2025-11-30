package com.unasp.unaspmarketplace

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

class UnaspMarketplaceApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Aplicar tema sistema como padrão do app
        applySystemTheme()
    }

    private fun applySystemTheme() {
        // Carregar configuração de tema salva ou usar sistema como padrão
        val sharedPreferences = getSharedPreferences("app_settings", MODE_PRIVATE)
        val themeMode = sharedPreferences.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)

        // Aplicar tema escolhido (padrão: sistema)
        AppCompatDelegate.setDefaultNightMode(themeMode)
    }
}
