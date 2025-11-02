package com.unasp.unaspmarketplace

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.unasp.unaspmarketplace.repository.UserRepository
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {
    private lateinit var userRepository: UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register_activity)

        userRepository = UserRepository()

        val edtNome = findViewById<EditText>(R.id.edtNome)
        val edtEmail = findViewById<EditText>(R.id.edtEmailSignIn)
        val edtSenha = findViewById<EditText>(R.id.edtSenhaSignIn)
        val edtConfirmarSenha = findViewById<EditText>(R.id.edtConfirmarSenha)
        val btnCadastrar = findViewById<LinearLayout>(R.id.btnCadastrar)

        btnCadastrar.setOnClickListener {
            val nome = edtNome.text.toString().trim()
            val email = edtEmail.text.toString().trim()
            val senha = edtSenha.text.toString().trim()
            val confirmarSenha = edtConfirmarSenha.text.toString().trim()

            if (validateInputs(nome, email, senha, confirmarSenha)) {
                registerUser(nome, email, senha)
            }
        }
    }

    private fun validateInputs(nome: String, email: String, senha: String, confirmarSenha: String): Boolean {
        when {
            nome.isEmpty() || email.isEmpty() || senha.isEmpty() || confirmarSenha.isEmpty() -> {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
                return false
            }
            senha != confirmarSenha -> {
                Toast.makeText(this, "As senhas não coincidem", Toast.LENGTH_SHORT).show()
                return false
            }
            senha.length < 6 -> {
                Toast.makeText(this, "A senha deve ter pelo menos 6 caracteres", Toast.LENGTH_SHORT).show()
                return false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                Toast.makeText(this, "Email inválido", Toast.LENGTH_SHORT).show()
                return false
            }
            else -> return true
        }
    }

    private fun registerUser(nome: String, email: String, senha: String) {
        lifecycleScope.launch {
            try {
                val result = userRepository.registerUser(nome, email, senha)

                if (result.isSuccess) {
                    Toast.makeText(this@RegisterActivity, "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show()

                    // Volta para a tela de login após cadastro
                    val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    val error = result.exceptionOrNull()?.message ?: "Erro desconhecido"
                    Toast.makeText(this@RegisterActivity, "Erro no cadastro: $error", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@RegisterActivity, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}