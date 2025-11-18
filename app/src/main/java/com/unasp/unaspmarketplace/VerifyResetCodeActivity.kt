package com.unasp.unaspmarketplace

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.unasp.unaspmarketplace.services.PasswordResetService
import kotlinx.coroutines.launch

class VerifyResetCodeActivity : AppCompatActivity() {

    private lateinit var edtCode: EditText
    private lateinit var btnVerifyCode: Button
    private lateinit var btnResendCode: Button
    private lateinit var txtCountdown: TextView
    private lateinit var txtEmail: TextView

    private var userEmail: String = ""
    private var countDownTimer: CountDownTimer? = null
    private val resetService = PasswordResetService.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify_reset_code)

        // Receber email do intent
        userEmail = intent.getStringExtra("email") ?: ""

        if (userEmail.isEmpty()) {
            Toast.makeText(this, "Erro: Email não encontrado", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupViews()
        setupListeners()
        startCountdown()

        // Configurar ActionBar
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Verificar Código"
        }

        // Configurar botão de voltar
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })
    }

    private fun setupViews() {
        edtCode = findViewById(R.id.edtCode)
        btnVerifyCode = findViewById(R.id.btnVerifyCode)
        btnResendCode = findViewById(R.id.btnResendCode)
        txtCountdown = findViewById(R.id.txtCountdown)
        txtEmail = findViewById(R.id.txtEmail)

        // Mostrar email mascarado
        txtEmail.text = "Código enviado para ${maskEmail(userEmail)}"

        // Inicialmente desabilitar botão de reenviar
        btnResendCode.isEnabled = false
    }

    private fun setupListeners() {
        // Validação em tempo real do código
        edtCode.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                btnVerifyCode.isEnabled = s?.length == 5
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        btnVerifyCode.setOnClickListener {
            val code = edtCode.text.toString()
            if (code.length == 5) {
                verifyCode(code)
            }
        }

        btnResendCode.setOnClickListener {
            resendCode()
        }
    }

    private fun verifyCode(code: String) {
        btnVerifyCode.isEnabled = false
        btnVerifyCode.text = "Verificando..."

        lifecycleScope.launch {
            try {
                val result = resetService.validateToken(userEmail, code)

                if (result.isSuccess) {
                    Toast.makeText(this@VerifyResetCodeActivity, "Código válido!", Toast.LENGTH_SHORT).show()

                    // Navegar para tela de redefinir senha
                    val intent = Intent(this@VerifyResetCodeActivity, ResetPasswordActivity::class.java)
                    intent.putExtra("email", userEmail)
                    startActivity(intent)
                    finish()

                } else {
                    val error = result.exceptionOrNull()?.message ?: "Código inválido"
                    Toast.makeText(this@VerifyResetCodeActivity, error, Toast.LENGTH_LONG).show()
                    edtCode.text.clear()
                }

            } catch (e: Exception) {
                Toast.makeText(this@VerifyResetCodeActivity, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                btnVerifyCode.isEnabled = true
                btnVerifyCode.text = "Verificar Código"
            }
        }
    }

    private fun resendCode() {
        btnResendCode.isEnabled = false
        btnResendCode.text = "Enviando..."

        lifecycleScope.launch {
            try {
                val result = resetService.initiatePasswordReset(userEmail)

                if (result.isSuccess) {
                    Toast.makeText(this@VerifyResetCodeActivity, "Novo código enviado!", Toast.LENGTH_SHORT).show()
                    startCountdown()
                    edtCode.text.clear()
                } else {
                    val error = result.exceptionOrNull()?.message ?: "Erro ao enviar código"
                    Toast.makeText(this@VerifyResetCodeActivity, error, Toast.LENGTH_LONG).show()
                }

            } catch (e: Exception) {
                Toast.makeText(this@VerifyResetCodeActivity, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                btnResendCode.text = "Reenviar Código"
            }
        }
    }

    private fun startCountdown() {
        countDownTimer?.cancel()

        countDownTimer = object : CountDownTimer(15 * 60 * 1000, 1000) { // 15 minutos
            override fun onTick(millisUntilFinished: Long) {
                val minutes = millisUntilFinished / 1000 / 60
                val seconds = (millisUntilFinished / 1000) % 60
                txtCountdown.text = "Código válido por ${String.format("%02d:%02d", minutes, seconds)}"
            }

            override fun onFinish() {
                txtCountdown.text = "Código expirado"
                btnResendCode.isEnabled = true
                edtCode.isEnabled = false
                btnVerifyCode.isEnabled = false
            }
        }

        countDownTimer?.start()
    }

    private fun maskEmail(email: String): String {
        val atIndex = email.indexOf("@")
        if (atIndex <= 2) return email

        val username = email.substring(0, atIndex)
        val domain = email.substring(atIndex)

        val maskedUsername = username.take(2) + "*".repeat(username.length - 2)
        return maskedUsername + domain
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }
}
