package com.unasp.unaspmarketplace

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.unasp.unaspmarketplace.models.User
import com.unasp.unaspmarketplace.utils.UserUtils
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {

    private lateinit var etName: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etCurrentPassword: TextInputEditText
    private lateinit var etNewPassword: TextInputEditText
    private lateinit var etConfirmPassword: TextInputEditText
    private lateinit var btnSaveProfile: MaterialButton
    private lateinit var btnChangePassword: MaterialButton
    private lateinit var btnDeleteAccount: MaterialButton

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
        etCurrentPassword = findViewById(R.id.etCurrentPassword)
        etNewPassword = findViewById(R.id.etNewPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnSaveProfile = findViewById(R.id.btnSaveProfile)
        btnChangePassword = findViewById(R.id.btnChangePassword)
        btnDeleteAccount = findViewById(R.id.btnDeleteAccount)
    }

    private fun setupClickListeners() {
        btnSaveProfile.setOnClickListener { saveProfile() }
        btnChangePassword.setOnClickListener { changePassword() }
        btnDeleteAccount.setOnClickListener { showDeleteAccountDialog() }
    }

    private fun loadUserData() {
        lifecycleScope.launch {
            try {
                currentUser = UserUtils.getCurrentUser()
                currentUser?.let { user ->
                    etName.setText(user.name)
                    etEmail.setText(user.email)
                }
            } catch (e: Exception) {
                Toast.makeText(this@ProfileActivity, "Erro ao carregar dados do perfil", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveProfile() {
        val newName = etName.text.toString().trim()

        if (newName.isEmpty()) {
            etName.error = "Nome não pode estar vazio"
            return
        }

        btnSaveProfile.isEnabled = false
        btnSaveProfile.text = "Salvando..."

        lifecycleScope.launch {
            try {
                val updatedUser = currentUser?.copy(name = newName) ?: return@launch
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

        val user = auth.currentUser
        val email = user?.email

        if (user != null && email != null) {
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