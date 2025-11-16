package com.unasp.unaspmarketplace

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.firebase.FirebaseApp
import com.unasp.unaspmarketplace.auth.GoogleAuthHelper
import com.unasp.unaspmarketplace.data.model.LoginViewModel
import com.unasp.unaspmarketplace.utils.UserUtils
import android.widget.TextView
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth
import android.widget.CheckBox


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
                        // Garantir que os dados do usuário existam no Firestore
                        lifecycleScope.launch {
                            try {
                                UserUtils.ensureUserDataExists()
                            } catch (e: Exception) {
                                Log.e("LoginActivity", "Erro ao garantir dados do usuário", e)
                            }

                            runOnUiThread {
                                Toast.makeText(this@LoginActivity, "Login com Google realizado com sucesso!", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                        }
                    } else {
                        Toast.makeText(this@LoginActivity, "Erro no login com Google: $error", Toast.LENGTH_LONG).show()
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
            // Inicializar Firebase
            FirebaseApp.initializeApp(this)

            setContentView(R.layout.login_activity)

            setupLoginButtons()
            observeLoginState()

            // 🔹 Novo trecho: texto clicável para cadastro
            val register = findViewById<TextView>(R.id.sign_in_text)
            register.setOnClickListener {
                val intent = Intent(this, RegisterActivity::class.java)
                startActivity(intent)
            }

            // 🔹 Texto clicável para "Esqueci minha senha"
            val forgotPassword = findViewById<TextView>(R.id.login_forgot_password)
            forgotPassword.setOnClickListener {
                val emailField = findViewById<EditText>(R.id.edtEmail)
                val email = emailField.text.toString()

                if (email.isEmpty()) {
                    Toast.makeText(this, "Digite seu e-mail para recuperar a senha", Toast.LENGTH_SHORT).show()
                } else {
                    FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(this, "E-mail de recuperação enviado!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this, "Erro: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                }
            }

            val emailField = findViewById<EditText>(R.id.edtEmail)
            val passwordField = findViewById<EditText>(R.id.edtSenha)
            val loginButton = findViewById<com.google.android.material.button.MaterialButton>(R.id.btnLogin)
            val checkBoxTerms = findViewById<CheckBox>(R.id.login_remember_me)

            loginButton.setOnClickListener {
                val email = emailField.text.toString()
                val password = passwordField.text.toString()

                if (!checkBoxTerms.isChecked) {
                    Toast.makeText(this, "Você precisa aceitar os termos para continuar", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Aqui você chama a função de login já implementada no projeto
                if (email.isNotEmpty() && password.isNotEmpty()) {
                    // Exemplo: chamar FirebaseAuth ou lógica existente
                    viewModel.login(email, password)
                } else {
                    Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
                }
            }



            } catch (e: Exception) {
                    Log.e("LoginActivity", "Error in onCreate", e)
                    Toast.makeText(this, "Erro ao inicializar a tela: ${e.message}", Toast.LENGTH_LONG).show()
                }
    }

    private fun setupLoginButtons() {
        // Email/Password login
        findViewById<com.google.android.material.button.MaterialButton>(R.id.btnLogin).setOnClickListener {
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
                // Garantir que os dados do usuário existam no Firestore
                lifecycleScope.launch {
                    try {
                        UserUtils.ensureUserDataExists()
                    } catch (e: Exception) {
                        Log.e("LoginActivity", "Erro ao garantir dados do usuário", e)
                    }

                    runOnUiThread {
                        Toast.makeText(this@LoginActivity, "Login realizado com sucesso!", Toast.LENGTH_SHORT).show()
                        // Navigate to HomeActivity
                        val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                }
            }
        }

        viewModel.errorMessage.observe(this) { error ->
            Toast.makeText(this, "Erro: $error", Toast.LENGTH_LONG).show()
        }
    }
}