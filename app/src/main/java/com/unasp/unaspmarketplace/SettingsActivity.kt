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
import androidx.lifecycle.lifecycleScope
import com.unasp.unaspmarketplace.utils.LoginPreferences
import com.unasp.unaspmarketplace.utils.ProductImageVerifier
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {

    private lateinit var settingProfile: LinearLayout
    private lateinit var settingOrders: LinearLayout
    private lateinit var settingNotifications: LinearLayout
    private lateinit var settingLogin: LinearLayout
    private lateinit var settingVerifyImages: LinearLayout
    private lateinit var settingTheme: LinearLayout
    private lateinit var settingHelp: LinearLayout
    private lateinit var settingAbout: LinearLayout
    private lateinit var switchNotifications: Switch
    private lateinit var txtCurrentTheme: TextView

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var loginPreferences: LoginPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        sharedPreferences = getSharedPreferences("app_settings", MODE_PRIVATE)
        loginPreferences = LoginPreferences(this)

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
        settingLogin = findViewById(R.id.setting_login) // New login settings section
        settingVerifyImages = findViewById(R.id.setting_verify_images) // New image verification section
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

        settingLogin.setOnClickListener {
            showLoginSettingsDialog()
        }

        settingVerifyImages.setOnClickListener {
            showImageVerificationDialog()
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
            • Desenvolvimento: Kosta, Izabella, Rafael, Fofinho, José e Eduardo
            • Instituição: UNASP
            • Ano: 2025
            
            📧 CONTATO:
            marketplace@unasp.edu.br
            
            © 2025 UNASP - Todos os direitos reservados
        """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle("Sobre o App")
            .setMessage(aboutMessage)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showLoginSettingsDialog() {
        val currentRememberMe = loginPreferences.isRememberMeEnabled()
        val hasSavedCredentials = loginPreferences.hasSavedCredentials()

        val message = buildString {
            appendLine("⚙️ CONFIGURAÇÕES DE LOGIN")
            appendLine()
            appendLine("Status atual:")
            appendLine("• Lembrar de mim: ${if (currentRememberMe) "✅ Ativado" else "❌ Desativado"}")
            appendLine("• Credenciais salvas: ${if (hasSavedCredentials) "✅ Sim" else "❌ Não"}")
            appendLine()
            appendLine("O que você deseja fazer?")
        }

        AlertDialog.Builder(this)
            .setTitle("Configurações de Login")
            .setMessage(message)
            .setPositiveButton("Limpar Credenciais") { _, _ ->
                showClearCredentialsConfirmation()
            }
            .setNegativeButton("Ver Detalhes") { _, _ ->
                showLoginDetailsDialog()
            }
            .setNeutralButton("Fechar", null)
            .show()
    }

    private fun showClearCredentialsConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Limpar Credenciais")
            .setMessage("Tem certeza que deseja limpar todas as credenciais salvas? Você precisará digitar email e senha no próximo login.")
            .setPositiveButton("Sim, Limpar") { _, _ ->
                loginPreferences.clearAllPreferences()
                Toast.makeText(this, "Credenciais limpas com sucesso!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showLoginDetailsDialog() {
        val savedEmail = loginPreferences.getSavedEmail() ?: "Nenhum"
        val rememberEnabled = loginPreferences.isRememberMeEnabled()
        val autoLoginEnabled = loginPreferences.isAutoLoginEnabled()
        val hasSavedCredentials = loginPreferences.hasSavedCredentials()

        val details = buildString {
            appendLine("📊 DETALHES DAS CONFIGURAÇÕES")
            appendLine()
            appendLine("Email salvo: $savedEmail")
            appendLine("Lembrar de mim: ${if (rememberEnabled) "Ativado" else "Desativado"}")
            appendLine("Login automático: ${if (autoLoginEnabled) "Ativado" else "Desativado"}")
            appendLine("Credenciais válidas: ${if (hasSavedCredentials) "Sim" else "Não"}")
            appendLine()
            appendLine("ℹ️ INFO:")
            appendLine("• As credenciais expiram em 30 dias")
            appendLine("• Logout completo remove tudo")
            appendLine("• Logout suave mantém credenciais")
        }

        AlertDialog.Builder(this)
            .setTitle("Detalhes de Login")
            .setMessage(details)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showImageVerificationDialog() {
        AlertDialog.Builder(this)
            .setTitle("🔍 Verificação de Imagens")
            .setMessage(
                "Esta ferramenta verifica se as imagens dos seus produtos estão sendo exibidas corretamente.\n\n" +
                "📊 O que será verificado:\n" +
                "• URLs de imagens válidas\n" +
                "• Imagens acessíveis no Firebase\n" +
                "• Produtos sem imagens\n" +
                "• Possíveis problemas\n\n" +
                "🕐 Isso pode levar alguns segundos..."
            )
            .setPositiveButton("🔍 Verificar Agora") { _, _ ->
                performImageVerification()
            }
            .setNegativeButton("❌ Cancelar") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun performImageVerification() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "❌ Usuário não logado", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                runOnUiThread {
                    Toast.makeText(this@SettingsActivity, "🔍 Verificando imagens...", Toast.LENGTH_SHORT).show()
                }

                val result = ProductImageVerifier.verifyUserProductImages(userId)

                runOnUiThread {
                    showVerificationResults(result)
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@SettingsActivity, "❌ Erro na verificação: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun showVerificationResults(result: com.unasp.unaspmarketplace.utils.UserImageVerificationResult) {
        val message = buildString {
            appendLine("📊 RESULTADOS DA VERIFICAÇÃO")
            appendLine("─────────────────────────")
            appendLine("📱 Total de produtos: ${result.totalProducts}")
            appendLine("📸 Com imagens: ${result.productsWithImages}")
            appendLine("❌ Sem imagens: ${result.productsWithoutImages}")
            appendLine("⚠️ Com problemas: ${result.productsWithErrors}")
            appendLine()

            if (result.totalProducts > 0) {
                val percentage = (result.productsWithImages * 100) / result.totalProducts
                appendLine("📈 Percentual com imagens: $percentage%")
                appendLine()

                when {
                    percentage >= 80 -> appendLine("✅ Excelente! A maioria dos produtos tem imagens.")
                    percentage >= 50 -> appendLine("⚠️ Bom, mas pode melhorar. Adicione mais fotos.")
                    else -> appendLine("❌ Muitos produtos sem fotos. Adicione imagens para vender mais!")
                }
            } else {
                appendLine("ℹ️ Você ainda não tem produtos cadastrados.")
            }
        }

        val title = if (result.productsWithErrors > 0) {
            "⚠️ Verificação com Problemas"
        } else if (result.productsWithoutImages > result.productsWithImages) {
            "📷 Adicione Mais Fotos"
        } else {
            "✅ Verificação Concluída"
        }

        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("📝 Ver Detalhes") { _, _ ->
                showDetailedVerificationResults(result)
            }
            .setNegativeButton("✅ OK") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showDetailedVerificationResults(result: com.unasp.unaspmarketplace.utils.UserImageVerificationResult) {
        val message = buildString {
            appendLine("📋 ANÁLISE DETALHADA")
            appendLine("─────────────────")

            var productIndex = 1
            for (productResult in result.detailedResults) {
                when (productResult) {
                    is com.unasp.unaspmarketplace.utils.ImageVerificationResult.Success -> {
                        appendLine("${productIndex}. ✅ ${productResult.totalImages} imagens")
                        if (productResult.invalidImages.isNotEmpty()) {
                            appendLine("   ⚠️ ${productResult.invalidImages.size} inválidas")
                        }
                    }
                    is com.unasp.unaspmarketplace.utils.ImageVerificationResult.NoImages -> {
                        appendLine("${productIndex}. ❌ Sem imagens")
                    }
                    else -> {
                        appendLine("${productIndex}. ⚠️ Erro na verificação")
                    }
                }
                productIndex++
            }

            appendLine()
            appendLine("💡 DICAS PARA MELHORAR:")
            appendLine("• Adicione pelo menos 3 fotos por produto")
            appendLine("• Use fotos de boa qualidade")
            appendLine("• Mostre diferentes ângulos")
            appendLine("• Produtos com fotos vendem 5x mais!")
        }

        AlertDialog.Builder(this)
            .setTitle("📊 Relatório Detalhado")
            .setMessage(message)
            .setPositiveButton("✅ Entendi") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}
