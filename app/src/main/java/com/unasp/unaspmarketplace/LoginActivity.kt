package com.unasp.unaspmarketplace

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.unasp.unaspmarketplace.data.model.LoginViewModel

class LoginActivity : AppCompatActivity() {
    private val viewModel = LoginViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_activity)

        findViewById<Button>(R.id.btnLogin).setOnClickListener {
            val email = findViewById<EditText>(R.id.edtEmail).text.toString()
            val password = findViewById<EditText>(R.id.edtSenha).text.toString()

            viewModel.login(email, password)
        }

        viewModel.loginState.observe(this) { success ->
            if (success) {
                Toast.makeText(this, "Login feito!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Falha no login", Toast.LENGTH_SHORT).show()
            }
        }
    }
}