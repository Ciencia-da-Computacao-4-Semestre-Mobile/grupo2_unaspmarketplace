package com.unasp.unaspmarketplace

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.unasp.unaspmarketplace.repository.UserRepository
import kotlinx.coroutines.launch
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.unasp.unaspmarketplace.auth.FacebookAuthHelper
import com.unasp.unaspmarketplace.auth.GitHubAuthHelper
import com.unasp.unaspmarketplace.auth.GoogleAuthHelper
import com.unasp.unaspmarketplace.utils.UserUtils

class RegisterActivity : AppCompatActivity() {
    private lateinit var userRepository: UserRepository

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account.idToken
            if (idToken != null) {
                GoogleAuthHelper.firebaseAuthWithGoogle(idToken) { success, error ->
                    if (success) {
                        // Garantir que os dados do usuário existam no Firestore
                        lifecycleScope.launch {
                            try {
                                UserUtils.ensureUserDataExists()
                            } catch (e: Exception) {
                                Log.e("ResgiterActivity", "Erro ao garantir dados do usuário", e)
                            }

                            runOnUiThread {
                                Toast.makeText(this@RegisterActivity, "Cadastro com Google realizado com sucesso!", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this@RegisterActivity, HomeActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                        }
                    } else {
                        Toast.makeText(this@RegisterActivity, "Erro no login com Google: $error", Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                Toast.makeText(this, "Erro ao obter token do Google", Toast.LENGTH_SHORT).show()
            }
        } catch (e: ApiException) {
            Log.e("RegisterActivity", "Google sign-in failed", e)
            Toast.makeText(this, "Erro no cadastro com Google: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register_activity)

        userRepository = UserRepository()

        val edtNome = findViewById<EditText>(R.id.edtNome)
        val edtEmail = findViewById<EditText>(R.id.edtEmailSignIn)
        val edtSenha = findViewById<EditText>(R.id.edtSenhaSignIn)
        val edtConfirmarSenha = findViewById<EditText>(R.id.edtConfirmarSenha)
        val btnCadastrar = findViewById<LinearLayout>(R.id.btnCadastrar)

        // 🔹 TextView clicável para voltar ao login
        val login = findViewById<TextView>(R.id.log_in_text)
        login.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish() // fecha a tela de cadastro
        }

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