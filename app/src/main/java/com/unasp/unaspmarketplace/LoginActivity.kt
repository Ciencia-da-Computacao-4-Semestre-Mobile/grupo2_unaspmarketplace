package com.unasp.unaspmarketplace

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.CheckBox
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.firebase.FirebaseApp

import com.google.firebase.auth.FirebaseAuth
import com.unasp.unaspmarketplace.auth.FacebookAuthHelper

import com.unasp.unaspmarketplace.auth.GoogleAuthHelper
import com.unasp.unaspmarketplace.auth.GitHubAuthHelper
import com.unasp.unaspmarketplace.services.PasswordResetService
import com.unasp.unaspmarketplace.utils.UserUtils
import com.unasp.unaspmarketplace.data.model.LoginViewModel
import kotlinx.coroutines.launch


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

            // 🔹 Funcionalidade "Esqueci minha senha" - Sistema com token de 5 dígitos
            findViewById<TextView>(R.id.login_forgot_password).setOnClickListener {
                showForgotPasswordDialog()
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


    private fun signInWithFacebook() {
        try {
            FacebookAuthHelper.signInWithFacebook(this, facebookCallbackManager) { success, error ->
                if (success) {
                    // Login bem-sucedido
                    lifecycleScope.launch {
                        try {
                            UserUtils.ensureUserDataExists()
                        } catch (e: Exception) {
                            Log.e("LoginActivity", "Erro ao garantir dados do usuário", e)
                        }

                        runOnUiThread {
                            Toast.makeText(this@LoginActivity, "Login com Facebook realizado com sucesso!", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    }
                } else {
                    // Verificar se é falha silenciosa (como no GitHub)
                    if (error == "SILENT_FAIL") {
                        Toast.makeText(this@LoginActivity, "Não foi possível fazer login com Facebook. Tente outro método.", Toast.LENGTH_LONG).show()
                    } else {
                        // Outros erros (cancelamento, rede, etc.)
                        if (error?.contains("cancelado", ignoreCase = true) != true) {
                            Toast.makeText(this@LoginActivity, error ?: "Erro no login com Facebook", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("LoginActivity", "Error starting Facebook sign-in", e)
            Toast.makeText(this, "Erro ao iniciar login com Facebook: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun signInWithGitHub() {
        try {
            GitHubAuthHelper.signInWithGitHub(this) { success: Boolean, error: String? ->
                if (success) {
                    // Login bem-sucedido
                    lifecycleScope.launch {
                        try {
                            UserUtils.ensureUserDataExists()
                        } catch (e: Exception) {
                            Log.e("LoginActivity", "Erro ao garantir dados do usuário", e)
                        }

                        runOnUiThread {
                            Toast.makeText(this@LoginActivity, "Login com GitHub realizado com sucesso!", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    }
                } else {
                    // Verificar se é falha silenciosa
                    if (error == "SILENT_FAIL") {
                        // Falha silenciosa - mostrar mensagem genérica
                        Toast.makeText(this@LoginActivity, "Não foi possível fazer login com GitHub. Tente outro método.", Toast.LENGTH_LONG).show()
                    } else {
                        // Outros erros (cancelamento, rede, etc.)
                        if (error?.contains("cancelado", ignoreCase = true) != true) {
                            Toast.makeText(this@LoginActivity, error ?: "Erro no login com GitHub", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("LoginActivity", "Error starting GitHub sign-in", e)
            Toast.makeText(this, "Erro ao iniciar login com GitHub: ${e.message}", Toast.LENGTH_LONG).show()
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
        viewModel.loginState.observe(this) { success: Boolean ->
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

        viewModel.errorMessage.observe(this) { error: String ->
            Toast.makeText(this, "Erro: $error", Toast.LENGTH_LONG).show()
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Tratar callback do Facebook
        facebookCallbackManager.onActivityResult(requestCode, resultCode, data)
    }

    /**
     * Mostra diálogo para solicitar recuperação de senha
     */
    private fun showForgotPasswordDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Recuperar Senha")
        builder.setMessage("Digite seu email para receber um código de verificação:")

        // Campo de email
        val emailInput = EditText(this)
        emailInput.hint = "Digite seu email"
        emailInput.inputType = android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS

        // Pré-preencher com email atual se houver
        val currentEmail = findViewById<EditText>(R.id.edtEmail).text.toString()
        if (currentEmail.isNotEmpty()) {
            emailInput.setText(currentEmail)
        }

        builder.setView(emailInput)

        builder.setPositiveButton("Enviar Código") { dialog, _ ->
            val email = emailInput.text.toString().trim()

            if (email.isEmpty()) {
                Toast.makeText(this, "Digite um email válido", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Email inválido", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }

            // Solicitar código de recuperação
            requestPasswordReset(email)
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }

    /**
     * Solicita código de recuperação de senha
     */
    private fun requestPasswordReset(email: String) {
        val resetService = PasswordResetService.getInstance()

        lifecycleScope.launch {
            try {
                Toast.makeText(this@LoginActivity, "Enviando código...", Toast.LENGTH_SHORT).show()

                val result = resetService.initiatePasswordReset(email)

                if (result.isSuccess) {
                    Toast.makeText(
                        this@LoginActivity,
                        "Código enviado para $email! Verifique sua caixa de entrada.",
                        Toast.LENGTH_LONG
                    ).show()

                    // Navegar para tela de verificação de código
                    val intent = Intent(this@LoginActivity, VerifyResetCodeActivity::class.java)
                    intent.putExtra("email", email)
                    startActivity(intent)

                } else {
                    val error = result.exceptionOrNull()?.message ?: "Erro ao enviar código"
                    Toast.makeText(this@LoginActivity, error, Toast.LENGTH_LONG).show()
                }

            } catch (e: Exception) {
                Toast.makeText(this@LoginActivity, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

}