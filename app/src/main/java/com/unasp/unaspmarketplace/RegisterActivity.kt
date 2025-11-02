package com.unasp.unaspmarketplace

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register_activity)

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

             if (nome.isEmpty() || email.isEmpty() || senha.isEmpty() || confirmarSenha.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
            } else {
                // Aqui você pode chamar FirebaseAuth ou seu backend
                Toast.makeText(this, "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show()

                // Volta para a tela de login após cadastro
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }
}