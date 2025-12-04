package com.unasp.unaspmarketplace

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.unasp.unaspmarketplace.models.User
import com.unasp.unaspmarketplace.repository.UserRepository
import com.unasp.unaspmarketplace.utils.AccountLinkingHelper
import com.unasp.unaspmarketplace.utils.CartManager
import com.unasp.unaspmarketplace.utils.UserUtils
import com.unasp.unaspmarketplace.utils.LogoutManager
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {

    private lateinit var etName: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etWhatsappNumber: TextInputEditText
    private lateinit var etCurrentPassword: TextInputEditText
    private lateinit var etNewPassword: TextInputEditText
    private lateinit var etConfirmPassword: TextInputEditText
    private lateinit var btnSaveProfile: MaterialButton
    private lateinit var btnChangePassword: MaterialButton
    private lateinit var btnLogout: MaterialButton
    private lateinit var btnDeleteAccount: MaterialButton

    private lateinit var txtPasswordInfo: TextView

    private val auth = FirebaseAuth.getInstance()
    private var currentUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        setupToolbar()
        initViews()
        setupClickListeners()
        loadUserData()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun initViews() {
        etName = findViewById(R.id.etName)
        etEmail = findViewById(R.id.etEmail)
        etWhatsappNumber = findViewById(R.id.etNumber) // Reutilizando o campo de número como WhatsApp
        etCurrentPassword = findViewById(R.id.etCurrentPassword)
        etNewPassword = findViewById(R.id.etNewPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnSaveProfile = findViewById(R.id.btnSaveProfile)
        btnChangePassword = findViewById(R.id.btnChangePassword)
        btnLogout = findViewById(R.id.btnLogout)
        btnDeleteAccount = findViewById(R.id.btnDeleteAccount)
        txtPasswordInfo = findViewById<TextView>(R.id.txtPasswordInfo)
    }

    private fun setupClickListeners() {
        btnSaveProfile.setOnClickListener { saveProfile() }
        btnChangePassword.setOnClickListener { changePassword() }
        btnLogout.setOnClickListener { showLogoutOptionsDialog() }
        btnDeleteAccount.setOnClickListener { showDeleteAccountDialog() }

        // Debug: mostrar informações da conta ao clicar no email (modo desenvolvedor)
        etEmail.setOnLongClickListener {
            showAccountDebugInfo()
            true
        }
    }

    private fun showAccountDebugInfo() {
        val user = auth.currentUser
        if (user != null) {
            val debugInfo = """
Informações da Conta (Debug):

Email: ${user.email}
UID: ${user.uid}

${AccountLinkingHelper.getProviderInfo(user)}

Google Account: ${AccountLinkingHelper.isGoogleAccount(user)}
Has Password: ${AccountLinkingHelper.hasPasswordProvider(user)}
Can Login with Password: ${AccountLinkingHelper.canLoginWithPassword(user)}
            """.trimIndent()

            AlertDialog.Builder(this)
                .setTitle("Debug - Info da Conta")
                .setMessage(debugInfo)
                .setPositiveButton("OK", null)
                .show()
        }
    }

    private fun loadUserData() {
        lifecycleScope.launch {
            try {
                currentUser = UserUtils.getCurrentUser()
                currentUser?.let { user ->
                    etName.setText(user.name)
                    etEmail.setText(user.email)
                    etWhatsappNumber.setText(user.whatsappNumber)
                }

                // Atualizar seção de senha baseado no tipo de conta
                updatePasswordSectionBasedOnProvider()

            } catch (e: Exception) {
                Toast.makeText(this@ProfileActivity, "Erro ao carregar dados do perfil", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updatePasswordSectionBasedOnProvider() {
        val user = auth.currentUser
        if (user != null) {
            val isGoogleAccount = AccountLinkingHelper.isGoogleAccount(user)
            val hasPasswordProvider = AccountLinkingHelper.hasPasswordProvider(user)

            if (isGoogleAccount && !hasPasswordProvider) {
                // Conta Google sem senha - mostrar opção para vincular
                showGoogleAccountPasswordLinking()
            } else if (isGoogleAccount && hasPasswordProvider) {
                // Conta Google com senha vinculada
                showLinkedAccountPasswordSection()
            } else {
                // Conta normal com senha
                showNormalPasswordSection()
            }
        }
    }

    private fun showGoogleAccountPasswordLinking() {
        // Atualizar textos para mostrar que é vinculação de senha
        etCurrentPassword.hint = "Deixe em branco (conta Google)"
        etCurrentPassword.isEnabled = false
        etNewPassword.hint = "Nova senha para vincular"
        etConfirmPassword.hint = "Confirme a nova senha"
        btnChangePassword.text = "Vincular Senha"

        // Mostrar informação explicativa
        txtPasswordInfo.visibility = View.VISIBLE
        txtPasswordInfo.text = "💡 Vincule uma senha à sua conta Google para poder fazer login também com email/senha."
    }

    private fun showLinkedAccountPasswordSection() {
        // Conta Google com senha vinculada - funciona normalmente
        etCurrentPassword.hint = "Senha atual"
        etCurrentPassword.isEnabled = true
        etNewPassword.hint = "Nova senha"
        etConfirmPassword.hint = "Confirme a nova senha"
        btnChangePassword.text = "Alterar Senha"

        // Mostrar informação sobre conta vinculada
        txtPasswordInfo.visibility = View.VISIBLE
        txtPasswordInfo.text = "✅ Sua conta Google tem senha vinculada. Você pode fazer login com Google ou email/senha."
    }

    private fun showNormalPasswordSection() {
        // Conta normal - manter como está
        etCurrentPassword.hint = "Senha atual"
        etCurrentPassword.isEnabled = true
        etNewPassword.hint = "Nova senha"
        etConfirmPassword.hint = "Confirme a nova senha"
        btnChangePassword.text = "Alterar Senha"

        // Ocultar informação (não é necessária para contas normais)
        txtPasswordInfo.visibility = View.GONE
    }

    private fun updatePasswordSectionForLinkedAccount() {
        // Atualizar para modo normal após vincular senha
        showLinkedAccountPasswordSection()
    }

    private fun saveProfile() {
        val newName = etName.text.toString().trim()
        val newWhatsapp = etWhatsappNumber.text.toString().trim()

        if (newName.isEmpty()) {
            etName.error = "Nome não pode estar vazio"
            return
        }

        // Validar formato do WhatsApp apenas se foi preenchido
        if (newWhatsapp.isNotEmpty() && newWhatsapp.length < 10) {
            etWhatsappNumber.error = "Digite um número de WhatsApp válido"
            return
        }

        // Validar formato do WhatsApp apenas se foi preenchido
        if (newWhatsapp.isNotEmpty() && newWhatsapp.length < 10) {
            etWhatsappNumber.error = "Digite um número de WhatsApp válido"
            return
        }

        btnSaveProfile.isEnabled = false
        btnSaveProfile.text = "Salvando..."

        lifecycleScope.launch {
            try {
                val updatedUser = currentUser?.copy(
                    name = newName,
                    whatsappNumber = newWhatsapp
                ) ?: return@launch

                val success = UserUtils.updateUser(updatedUser)

                btnSaveProfile.isEnabled = true
                btnSaveProfile.text = "Salvar Alterações"

                if (success) {
                    currentUser = updatedUser
                    Toast.makeText(this@ProfileActivity, "Perfil atualizado com sucesso!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@ProfileActivity, "Erro ao atualizar perfil", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                btnSaveProfile.isEnabled = true
                btnSaveProfile.text = "Salvar Alterações"
                Toast.makeText(this@ProfileActivity, "Erro: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun changePassword() {
        val currentPassword = etCurrentPassword.text.toString().trim()
        val newPassword = etNewPassword.text.toString().trim()
        val confirmPassword = etConfirmPassword.text.toString().trim()

        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(this, "Erro: usuário não logado", Toast.LENGTH_SHORT).show()
            return
        }

        // Verificar se é conta Google sem senha
        if (AccountLinkingHelper.isGoogleAccount(user) && !AccountLinkingHelper.hasPasswordProvider(user)) {
            // Conta Google sem senha vinculada - permitir criar senha
            handleGoogleAccountPasswordCreation(newPassword, confirmPassword)
        } else {
            // Conta com senha - processo normal de alteração
            handleNormalPasswordChange(currentPassword, newPassword, confirmPassword)
        }
    }

    private fun handleGoogleAccountPasswordCreation(newPassword: String, confirmPassword: String) {
        if (newPassword.isEmpty()) {
            etNewPassword.error = "Digite uma senha para vincular à sua conta Google"
            return
        }
        if (newPassword.length < 6) {
            etNewPassword.error = "A senha deve ter pelo menos 6 caracteres"
            return
        }
        if (newPassword != confirmPassword) {
            etConfirmPassword.error = "As senhas não coincidem"
            return
        }

        btnChangePassword.isEnabled = false
        btnChangePassword.text = "Vinculando Senha..."

        val user = auth.currentUser!!
        val email = user.email!!

        AccountLinkingHelper.linkPasswordToGoogleAccount(email, newPassword) { success, message ->
            runOnUiThread {
                btnChangePassword.isEnabled = true
                btnChangePassword.text = "Alterar Senha"

                if (success) {
                    etCurrentPassword.text?.clear()
                    etNewPassword.text?.clear()
                    etConfirmPassword.text?.clear()

                    Toast.makeText(this, message, Toast.LENGTH_LONG).show()

                    // Atualizar a interface para mostrar que agora tem senha
                    updatePasswordSectionForLinkedAccount()

                    // Opcionalmente, testar o login com a nova senha
                    showTestPasswordDialog(email, newPassword)
                } else {
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun showTestPasswordDialog(email: String, password: String) {
        AlertDialog.Builder(this)
            .setTitle("Testar Login com Senha")
            .setMessage("Deseja testar se o login com email/senha está funcionando?")
            .setPositiveButton("Sim, Testar") { _, _ ->
                testPasswordLogin(email, password)
            }
            .setNegativeButton("Não") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun testPasswordLogin(email: String, password: String) {
        Toast.makeText(this, "Testando login com senha...", Toast.LENGTH_SHORT).show()

        AccountLinkingHelper.testLoginWithNewPassword(email, password) { success, message ->
            runOnUiThread {
                AlertDialog.Builder(this)
                    .setTitle(if (success) "✅ Teste Bem-sucedido" else "❌ Teste Falhado")
                    .setMessage(message)
                    .setPositiveButton("OK") { dialog, _ ->
                        dialog.dismiss()
                        if (success) {
                            Toast.makeText(this, "Agora você pode fazer login com email/senha!", Toast.LENGTH_LONG).show()
                        }
                    }
                    .show()
            }
        }
    }

    private fun handleNormalPasswordChange(currentPassword: String, newPassword: String, confirmPassword: String) {
        if (currentPassword.isEmpty()) {
            etCurrentPassword.error = "Digite a senha atual"
            return
        }
        if (newPassword.isEmpty()) {
            etNewPassword.error = "Digite a nova senha"
            return
        }
        if (newPassword.length < 6) {
            etNewPassword.error = "A senha deve ter pelo menos 6 caracteres"
            return
        }
        if (newPassword != confirmPassword) {
            etConfirmPassword.error = "As senhas não coincidem"
            return
        }

        btnChangePassword.isEnabled = false
        btnChangePassword.text = "Alterando..."

        val user = auth.currentUser!!
        val email = user.email!!
        val credential = EmailAuthProvider.getCredential(email, currentPassword)

        user.reauthenticate(credential)
            .addOnCompleteListener { reauthTask ->
                if (reauthTask.isSuccessful) {
                    user.updatePassword(newPassword)
                        .addOnCompleteListener { updateTask ->
                            btnChangePassword.isEnabled = true
                            btnChangePassword.text = "Alterar Senha"

                            if (updateTask.isSuccessful) {
                                etCurrentPassword.text?.clear()
                                etNewPassword.text?.clear()
                                etConfirmPassword.text?.clear()
                                Toast.makeText(this, "Senha alterada com sucesso!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this, "Erro ao alterar senha: ${updateTask.exception?.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    btnChangePassword.isEnabled = true
                    btnChangePassword.text = "Alterar Senha"
                    Toast.makeText(this, "Senha atual incorreta", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Confirmar Logout")
            .setMessage("Tem certeza que deseja sair da sua conta?")
            .setPositiveButton("Sair") { _, _ ->
                performCompleteLogout()
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun performCompleteLogout() {
        lifecycleScope.launch {
            try {
                CartManager.clearCart()
                LogoutManager.performCompleteLogout(this@ProfileActivity)

                Toast.makeText(this@ProfileActivity, "Logout completo realizado com sucesso", Toast.LENGTH_SHORT).show()

                val intent = Intent(this@ProfileActivity, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@ProfileActivity, "Erro ao fazer logout: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun performSoftLogout() {
        lifecycleScope.launch {
            try {
                CartManager.clearCart()
                LogoutManager.performSoftLogout(this@ProfileActivity)

                Toast.makeText(
                    this@ProfileActivity,
                    "Logout realizado. Suas credenciais foram mantidas para próximo login.",
                    Toast.LENGTH_LONG
                ).show()

                val intent = Intent(this@ProfileActivity, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@ProfileActivity, "Erro ao fazer logout: ${e.message}", Toast.LENGTH_SHORT).show()
            }

        }
    }

    private fun showDeleteAccountDialog() {
        AlertDialog.Builder(this)
            .setTitle("Excluir Conta")
            .setMessage("Tem certeza que deseja excluir sua conta? Esta ação não pode ser desfeita.")
            .setPositiveButton("Excluir") { _, _ -> deleteAccount() }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun deleteAccount() {
        val user = auth.currentUser
        user?.delete()
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Conta excluída com sucesso", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Erro ao excluir conta: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}