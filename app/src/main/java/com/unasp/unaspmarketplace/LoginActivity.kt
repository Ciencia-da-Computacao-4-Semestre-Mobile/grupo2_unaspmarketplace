package com.unasp.unaspmarketplace

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.firebase.FirebaseApp
import com.unasp.unaspmarketplace.auth.GoogleAuthHelper
import com.unasp.unaspmarketplace.data.model.LoginViewModel
import android.widget.TextView

class LoginActivity : AppCompatActivity() {
    private val viewModel = LoginViewModel()

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
                        Toast.makeText(this, "Login com Google realizado com sucesso!", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, HomeActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this, "Erro no login com Google: $error", Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                Toast.makeText(this, "Erro ao obter token do Google", Toast.LENGTH_SHORT).show()
            }
        } catch (e: ApiException) {
            Log.e("LoginActivity", "Google sign-in failed", e)
            Toast.makeText(this, "Erro no login com Google: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            FirebaseApp.initializeApp(this)
            setContentView(R.layout.login_activity)

            setupLoginButtons()
            observeLoginState()

            // 🔹 Novo trecho: texto clicável para cadastro
            val Register = findViewById<TextView>(R.id.sign_in_text)
            Register.setOnClickListener {
                val intent = Intent(this, RegisterActivity::class.java)
                startActivity(intent)
            }

        } catch (e: Exception) {
            Log.e("LoginActivity", "Error in onCreate", e)
            Toast.makeText(this, "Erro ao inicializar a tela: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupLoginButtons() {
        // Email/Password login
        findViewById<LinearLayout>(R.id.btnLogin).setOnClickListener {
            val email = findViewById<EditText>(R.id.edtEmail).text.toString().trim()
            val password = findViewById<EditText>(R.id.edtSenha).text.toString().trim()

            if (validateInput(email, password)) {
                viewModel.login(email, password)
            }
        }

        // Google login using GoogleAuthHelper
        findViewById<LinearLayout>(R.id.btnGoogleLogin).setOnClickListener {
            signInWithGoogle()
        }

        // GitHub login
        findViewById<LinearLayout>(R.id.btnGitHubLogin).setOnClickListener {
            Toast.makeText(this, "Login com GitHub em desenvolvimento", Toast.LENGTH_SHORT).show()
        }
    }

    private fun signInWithGoogle() {
        try {
            val googleSignInClient = GoogleAuthHelper.getClient(this)
            val signInIntent = googleSignInClient.signInIntent
            googleSignInLauncher.launch(signInIntent)
        } catch (e: Exception) {
            Log.e("LoginActivity", "Error starting Google sign-in", e)
            Toast.makeText(this, "Erro ao iniciar login com Google: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        return when {
            email.isEmpty() -> {
                Toast.makeText(this, "Digite o email", Toast.LENGTH_SHORT).show()
                false
            }
            password.isEmpty() -> {
                Toast.makeText(this, "Digite a senha", Toast.LENGTH_SHORT).show()
                false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                Toast.makeText(this, "Email inválido", Toast.LENGTH_SHORT).show()
                false
            }
            else -> true
        }
    }

    private fun observeLoginState() {
        viewModel.loginState.observe(this) { success ->
            if (success) {
                Toast.makeText(this, "Login realizado com sucesso!", Toast.LENGTH_SHORT).show()
                // Navigate to HomeActivity
                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

        viewModel.errorMessage.observe(this) { error ->
            Toast.makeText(this, "Erro: $error", Toast.LENGTH_LONG).show()
        }
    }
}