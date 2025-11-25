package com.unasp.unaspmarketplace

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate

class SettingsActivity : AppCompatActivity() {

    private lateinit var settingProfile: LinearLayout
    private lateinit var settingOrders: LinearLayout
    private lateinit var settingNotifications: LinearLayout
    private lateinit var settingTheme: LinearLayout
    private lateinit var settingHelp: LinearLayout
    private lateinit var settingAbout: LinearLayout
    private lateinit var switchNotifications: Switch
    private lateinit var txtCurrentTheme: TextView

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        sharedPreferences = getSharedPreferences("app_settings", MODE_PRIVATE)

        setupToolbar()
        initViews()
        setupClickListeners()
        loadSettings()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun initViews() {
        settingProfile = findViewById(R.id.setting_profile)
        settingOrders = findViewById(R.id.setting_orders)
        settingNotifications = findViewById(R.id.setting_notifications)
        settingTheme = findViewById(R.id.setting_theme)
        settingHelp = findViewById(R.id.setting_help)
        settingAbout = findViewById(R.id.setting_about)
        switchNotifications = findViewById(R.id.switch_notifications)
        txtCurrentTheme = findViewById(R.id.txt_current_theme)
    }

    private fun setupClickListeners() {
        settingProfile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        settingOrders.setOnClickListener {
            val intent = Intent(this, OrderHistoryActivity::class.java)
            startActivity(intent)
        }

        settingNotifications.setOnClickListener {
            switchNotifications.isChecked = !switchNotifications.isChecked
        }

        switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            saveNotificationSettings(isChecked)
            val message = if (isChecked) {
                "Notificações ativadas"
            } else {
                "Notificações desativadas"
            }
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }

        settingTheme.setOnClickListener {
            showThemeDialog()
        }

        settingHelp.setOnClickListener {
            showHelpDialog()
        }

        settingAbout.setOnClickListener {
            showAboutDialog()
        }
    }

    private fun loadSettings() {
        // Carregar configurações de notificação
        val notificationsEnabled = sharedPreferences.getBoolean("notifications_enabled", true)
        switchNotifications.isChecked = notificationsEnabled

        // Carregar tema atual - PADRÃO É SISTEMA
        val currentTheme = sharedPreferences.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        updateThemeText(currentTheme)

        // Aplicar o tema automaticamente (garante que o tema sistema seja aplicado)
        AppCompatDelegate.setDefaultNightMode(currentTheme)
    }

    private fun saveNotificationSettings(enabled: Boolean) {
        sharedPreferences.edit()
            .putBoolean("notifications_enabled", enabled)
            .apply()
    }

    private fun showThemeDialog() {
        val themes = arrayOf("Sistema", "Claro", "Escuro")
        val currentTheme = sharedPreferences.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)

        val selectedTheme = when (currentTheme) {
            AppCompatDelegate.MODE_NIGHT_NO -> 1
            AppCompatDelegate.MODE_NIGHT_YES -> 2
            else -> 0
        }

        AlertDialog.Builder(this)
            .setTitle("Escolher Tema")
            .setSingleChoiceItems(themes, selectedTheme) { dialog, which ->
                val newThemeMode = when (which) {
                    1 -> AppCompatDelegate.MODE_NIGHT_NO
                    2 -> AppCompatDelegate.MODE_NIGHT_YES
                    else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                }

                // Salvar preferência
                sharedPreferences.edit()
                    .putInt("theme_mode", newThemeMode)
                    .apply()

                // Aplicar tema
                AppCompatDelegate.setDefaultNightMode(newThemeMode)

                // Atualizar texto
                updateThemeText(newThemeMode)

                dialog.dismiss()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun updateThemeText(themeMode: Int) {
        txtCurrentTheme.text = when (themeMode) {
            AppCompatDelegate.MODE_NIGHT_NO -> "Claro"
            AppCompatDelegate.MODE_NIGHT_YES -> "Escuro"
            else -> "Sistema"
        }
    }

    private fun showHelpDialog() {
        val helpMessage = """
            Como usar o UNASP Marketplace:
            
            📱 NAVEGAÇÃO:
            • Use o menu inferior para navegar
            • Toque no menu para mais opções
            
            🛒 COMPRAS:
            • Busque produtos na tela inicial
            • Adicione ao carrinho
            • Finalize pelo WhatsApp
            
            💰 VENDAS:
            • Use "Publicar Item" no menu
            • Adicione fotos e descrição
            • Gerencie em "Meus Itens"
            
            🔧 SUPORTE:
            • Entre em contato via WhatsApp
            • Email: suporte@unasp.edu.br
        """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle("Central de Ajuda")
            .setMessage(helpMessage)
            .setPositiveButton("Entendi", null)
            .setNeutralButton("Contatar Suporte") { _, _ ->
                Toast.makeText(this, "Redirecionando para suporte...", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun showAboutDialog() {
        val aboutMessage = """
            📱 UNASP Marketplace
            Versão 1.0.0
            
            🎓 Desenvolvido para a comunidade UNASP
            
            📋 FUNCIONALIDADES:
            • Compra e venda entre estudantes
            • Integração com WhatsApp
            • Autenticação segura
            • Interface moderna
            
            👥 EQUIPE:
            • Desenvolvimento: Grupo 2
            • Instituição: UNASP
            • Ano: 2024
            
            📧 CONTATO:
            marketplace@unasp.edu.br
            
            © 2024 UNASP - Todos os direitos reservados
        """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle("Sobre o App")
            .setMessage(aboutMessage)
            .setPositiveButton("OK", null)
            .show()
    }
}
