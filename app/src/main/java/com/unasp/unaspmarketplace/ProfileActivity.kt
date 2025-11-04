package com.unasp.unaspmarketplace

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.unasp.unaspmarketplace.models.User
import com.unasp.unaspmarketplace.utils.UserUtils
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*

class ProfileActivity : AppCompatActivity() {

    private lateinit var ivProfileImage: ImageView
    private lateinit var etName: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etCurrentPassword: TextInputEditText
    private lateinit var etNewPassword: TextInputEditText
    private lateinit var etConfirmPassword: TextInputEditText
    private lateinit var btnChangePhoto: MaterialButton
    private lateinit var btnSaveProfile: MaterialButton
    private lateinit var btnChangePassword: MaterialButton
    private lateinit var btnDeleteAccount: MaterialButton

    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private var currentUser: User? = null
    private var selectedImageUri: Uri? = null

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            selectedImageUri = result.data?.data
            selectedImageUri?.let { uri ->
                // Mostrar a imagem selecionada
                Glide.with(this)
                    .load(uri)
                    .circleCrop()
                    .into(ivProfileImage)
            }
        }
    }

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
        ivProfileImage = findViewById(R.id.ivProfileImage)
        etName = findViewById(R.id.etName)
        etEmail = findViewById(R.id.etEmail)
        etCurrentPassword = findViewById(R.id.etCurrentPassword)
        etNewPassword = findViewById(R.id.etNewPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnChangePhoto = findViewById(R.id.btnChangePhoto)
        btnSaveProfile = findViewById(R.id.btnSaveProfile)
        btnChangePassword = findViewById(R.id.btnChangePassword)
        btnDeleteAccount = findViewById(R.id.btnDeleteAccount)
    }

    private fun setupClickListeners() {
        btnChangePhoto.setOnClickListener {
            showImagePickerDialog()
        }

        btnSaveProfile.setOnClickListener {
            saveProfile()
        }

        btnChangePassword.setOnClickListener {
            changePassword()
        }

        btnDeleteAccount.setOnClickListener {
            showDeleteAccountDialog()
        }
    }

    private fun loadUserData() {
        lifecycleScope.launch {
            try {
                currentUser = UserUtils.getCurrentUser()
                currentUser?.let { user ->
                    runOnUiThread {
                        etName.setText(user.name)
                        etEmail.setText(user.email)

                        // Carregar foto de perfil
                        if (user.profileImageUrl.isNotEmpty()) {
                            Glide.with(this@ProfileActivity)
                                .load(user.profileImageUrl)
                                .circleCrop()
                                .placeholder(R.drawable.ic_person_placeholder)
                                .into(ivProfileImage)
                        }
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@ProfileActivity, "Erro ao carregar dados do perfil", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showImagePickerDialog() {
        val options = arrayOf("Galeria", "Câmera")
        AlertDialog.Builder(this)
            .setTitle("Selecionar Foto")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> openGallery()
                    1 -> openCamera()
                }
            }
            .show()
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        imagePickerLauncher.launch(intent)
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(packageManager) != null) {
            imagePickerLauncher.launch(intent)
        } else {
            Toast.makeText(this, "Câmera não disponível", Toast.LENGTH_SHORT).show()
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
                var imageUrl = currentUser?.profileImageUrl ?: ""

                // Upload da nova imagem se foi selecionada
                selectedImageUri?.let { uri ->
                    imageUrl = uploadProfileImage(uri) ?: imageUrl
                }

                // Atualizar dados do usuário
                val updatedUser = currentUser?.copy(
                    name = newName,
                    profileImageUrl = imageUrl
                ) ?: return@launch

                val success = UserUtils.updateUser(updatedUser)

                runOnUiThread {
                    btnSaveProfile.isEnabled = true
                    btnSaveProfile.text = "Salvar Alterações"

                    if (success) {
                        currentUser = updatedUser
                        selectedImageUri = null
                        Toast.makeText(this@ProfileActivity, "Perfil atualizado com sucesso!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@ProfileActivity, "Erro ao atualizar perfil", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    btnSaveProfile.isEnabled = true
                    btnSaveProfile.text = "Salvar Alterações"
                    Toast.makeText(this@ProfileActivity, "Erro: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private suspend fun uploadProfileImage(uri: Uri): String? {
        return try {
            val userId = auth.currentUser?.uid ?: return null
            val fileName = "profile_images/${userId}_${UUID.randomUUID()}.jpg"
            val storageRef = storage.reference.child(fileName)

            val uploadTask = storageRef.putFile(uri)
            uploadTask.await()

            val downloadUrl = storageRef.downloadUrl.await()
            downloadUrl.toString()
        } catch (e: Exception) {
            null
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
                                    // Limpar campos
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
            .setPositiveButton("Excluir") { _, _ ->
                deleteAccount()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun deleteAccount() {
        val user = auth.currentUser
        user?.delete()
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Conta excluída com sucesso", Toast.LENGTH_SHORT).show()

                    // Redirecionar para tela de login
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
