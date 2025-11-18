package com.unasp.unaspmarketplace

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.unasp.unaspmarketplace.services.PasswordResetService
import kotlinx.coroutines.launch

class ResetPasswordActivity : AppCompatActivity() {

    private lateinit var tilNewPassword: TextInputLayout
    private lateinit var tilConfirmPassword: TextInputLayout
    private lateinit var edtNewPassword: TextInputEditText
    private lateinit var edtConfirmPassword: TextInputEditText
    private lateinit var btnResetPassword: Button

    private var userEmail: String = ""
    private val resetService = PasswordResetService.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)

        // Receber email do intent
        userEmail = intent.getStringExtra("email") ?: ""

        if (userEmail.isEmpty()) {
            Toast.makeText(this, "Erro: Email não encontrado", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupViews()
        setupListeners()

        // Configurar ActionBar
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Redefinir Senha"
        }

        // Configurar botão de voltar
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })
    }

    private fun setupViews() {
        tilNewPassword = findViewById(R.id.tilNewPassword)
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword)
        edtNewPassword = findViewById(R.id.edtNewPassword)
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword)
        btnResetPassword = findViewById(R.id.btnResetPassword)

        // Inicialmente desabilitar botão
        btnResetPassword.isEnabled = false
    }

    private fun setupListeners() {
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                validatePasswords()
            }
            override fun afterTextChanged(s: Editable?) {}
        }

        edtNewPassword.addTextChangedListener(textWatcher)
        edtConfirmPassword.addTextChangedListener(textWatcher)

        btnResetPassword.setOnClickListener {
            val newPassword = edtNewPassword.text.toString()
            resetPassword(newPassword)
        }
    }

    private fun validatePasswords() {
        val newPassword = edtNewPassword.text.toString()
        val confirmPassword = edtConfirmPassword.text.toString()

        // Limpar erros anteriores
        tilNewPassword.error = null
        tilConfirmPassword.error = null

        var isValid = true

        // Validar senha nova
        if (newPassword.length < 6) {
            tilNewPassword.error = "A senha deve ter pelo menos 6 caracteres"
            isValid = false
        }

        // Validar confirmação de senha
        if (confirmPassword.isNotEmpty() && newPassword != confirmPassword) {
            tilConfirmPassword.error = "As senhas não coincidem"
            isValid = false
        }

        // Habilitar botão apenas se tudo estiver válido
        btnResetPassword.isEnabled = isValid &&
                newPassword.isNotEmpty() &&
                confirmPassword.isNotEmpty() &&
                newPassword == confirmPassword
    }

    private fun resetPassword(newPassword: String) {
        btnResetPassword.isEnabled = false
        btnResetPassword.text = "Redefinindo..."

        lifecycleScope.launch {
            try {
                // Usar Firebase Auth para atualizar a senha
                val auth = FirebaseAuth.getInstance()

                // Primeiro, fazer login temporário para poder alterar a senha
                // Na prática, você precisaria de uma estratégia mais segura aqui
                // Como este é um reset de senha, vamos simular que funcionou

                // Atualizar senha no Firebase Auth
                val user = auth.currentUser
                if (user != null && user.email == userEmail) {
                    user.updatePassword(newPassword)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                // Senha atualizada com sucesso
                                lifecycleScope.launch {
                                    val result = resetService.updatePassword(userEmail, newPassword)
                                    handlePasswordUpdateResult(result)
                                }
                            } else {
                                Toast.makeText(
                                    this@ResetPasswordActivity,
                                    "Erro ao atualizar senha: ${task.exception?.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                                resetButton()
                            }
                        }
                } else {
                    // Usuário não está logado, usar método alternativo
                    // Por segurança, vamos enviar um email de reset do Firebase
                    auth.sendPasswordResetEmail(userEmail)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(
                                    this@ResetPasswordActivity,
                                    "Um link de redefinição foi enviado para seu email. Use-o para definir sua nova senha.",
                                    Toast.LENGTH_LONG
                                ).show()

                                // Voltar para tela de login
                                navigateToLogin()
                            } else {
                                Toast.makeText(
                                    this@ResetPasswordActivity,
                                    "Erro: ${task.exception?.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                                resetButton()
                            }
                        }
                }

            } catch (e: Exception) {
                Toast.makeText(this@ResetPasswordActivity, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
                resetButton()
            }
        }
    }

    private fun handlePasswordUpdateResult(result: Result<String>) {
        if (result.isSuccess) {
            Toast.makeText(this, "Senha redefinida com sucesso!", Toast.LENGTH_SHORT).show()
            navigateToLogin()
        } else {
            val error = result.exceptionOrNull()?.message ?: "Erro ao redefinir senha"
            Toast.makeText(this, error, Toast.LENGTH_LONG).show()
            resetButton()
        }
    }

    private fun resetButton() {
        btnResetPassword.isEnabled = true
        btnResetPassword.text = "Redefinir Senha"
    }

    private fun navigateToLogin() {
        Toast.makeText(this, "Faça login com sua nova senha", Toast.LENGTH_LONG).show()

        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }
}
