package com.unasp.unaspmarketplace

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import android.widget.ImageView
import android.widget.Toast
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.storage.FirebaseStorage
import com.unasp.unaspmarketplace.adapters.ProductImageAdapter
import com.unasp.unaspmarketplace.models.Product
import com.unasp.unaspmarketplace.repository.ProductRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*

class PostItemActivity : AppCompatActivity() {
    private lateinit var productRepository: ProductRepository
    private lateinit var edtName: TextInputEditText
    private lateinit var edtDescription: TextInputEditText
    private lateinit var edtPrice: TextInputEditText
    private lateinit var edtStock: TextInputEditText
    private lateinit var spinnerCategory: AutoCompleteTextView
    private lateinit var btnSave: MaterialButton
    private lateinit var btnCancel: MaterialButton
    private lateinit var btnAddImage: MaterialButton
    private lateinit var btnRemoveImage: MaterialButton
    private lateinit var recyclerImages: RecyclerView

    private lateinit var imageAdapter: ProductImageAdapter
    private val storage = FirebaseStorage.getInstance()
    private val selectedImages = mutableListOf<Uri>()

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                imageAdapter.addImage(uri)
                btnRemoveImage.isEnabled = imageAdapter.getImages().isNotEmpty()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.post_item_activity)

        // Inicializar repositório
        productRepository = ProductRepository()

        // Inicializar views
        initViews()
        setupImageRecycler()
        setupCategorySpinner()
        setupButtons()
        setupBottomNavigation()
    }

    private fun initViews() {
        edtName = findViewById(R.id.edtName)
        edtDescription = findViewById(R.id.edtDescription)
        edtPrice = findViewById(R.id.edtPrice)
        edtStock = findViewById(R.id.edtStock)
        spinnerCategory = findViewById(R.id.spinnerCategory)
        btnSave = findViewById(R.id.btnSave)
        btnCancel = findViewById(R.id.btnCancel)
        btnAddImage = findViewById(R.id.btnAddImage)
        btnRemoveImage = findViewById(R.id.btnRemoveImage)
        recyclerImages = findViewById(R.id.recyclerImages)

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        btnBack.setOnClickListener { finish() }

        // Inicialmente desabilitar botão de remover
        btnRemoveImage.isEnabled = false
    }

    private fun setupImageRecycler() {
        imageAdapter = ProductImageAdapter(selectedImages) { position ->
            imageAdapter.removeImage(position)
            btnRemoveImage.isEnabled = imageAdapter.getImages().isNotEmpty()
        }

        recyclerImages.apply {
            adapter = imageAdapter
            layoutManager = LinearLayoutManager(this@PostItemActivity, LinearLayoutManager.HORIZONTAL, false)
        }
    }

    private fun setupCategorySpinner() {
        val categories = arrayOf(
            "Roupas",
            "Eletrônicos",
            "Alimentos",
            "Livros",
            "Casa e Jardim",
            "Esportes",
            "Beleza",
            "Automóveis",
            "Outros"
        )

        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categories)
        spinnerCategory.setAdapter(adapter)
    }

    private fun setupButtons() {
        btnSave.setOnClickListener {
            saveProduct()
        }

        btnCancel.setOnClickListener {
            finish()
        }

        btnAddImage.setOnClickListener {
            showImagePickerDialog()
        }

        btnRemoveImage.setOnClickListener {
            if (imageAdapter.getImages().isNotEmpty()) {
                showRemoveImageDialog()
            }
        }
    }

    private fun showImagePickerDialog() {
        val options = arrayOf("Galeria", "Câmera")
        AlertDialog.Builder(this)
            .setTitle("Selecionar Imagem")
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

    private fun showRemoveImageDialog() {
        val images = imageAdapter.getImages()
        if (images.isEmpty()) return

        val imageNames = images.mapIndexed { index, _ -> "Imagem ${index + 1}" }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("Remover Imagem")
            .setItems(imageNames) { _, which ->
                imageAdapter.removeImage(which)
                btnRemoveImage.isEnabled = imageAdapter.getImages().isNotEmpty()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun saveProduct() {
        // Validar campos
        val name = edtName.text.toString().trim()
        val description = edtDescription.text.toString().trim()
        val priceText = edtPrice.text.toString().trim()
        val stockText = edtStock.text.toString().trim()
        val category = spinnerCategory.text.toString().trim()

        if (name.isEmpty()) {
            edtName.error = "Nome é obrigatório"
            edtName.requestFocus()
            return
        }

        if (description.isEmpty()) {
            edtDescription.error = "Descrição é obrigatória"
            edtDescription.requestFocus()
            return
        }

        if (priceText.isEmpty()) {
            edtPrice.error = "Preço é obrigatório"
            edtPrice.requestFocus()
            return
        }

        if (stockText.isEmpty()) {
            edtStock.error = "Estoque é obrigatório"
            edtStock.requestFocus()
            return
        }

        if (category.isEmpty()) {
            spinnerCategory.error = "Categoria é obrigatória"
            spinnerCategory.requestFocus()
            return
        }

        val price = priceText.toDoubleOrNull()
        if (price == null || price <= 0) {
            edtPrice.error = "Preço deve ser um valor válido"
            edtPrice.requestFocus()
            return
        }

        val stock = stockText.toIntOrNull()
        if (stock == null || stock < 0) {
            edtStock.error = "Estoque deve ser um número válido"
            edtStock.requestFocus()
            return
        }

        // Salvar no Firebase
        btnSave.isEnabled = false
        btnSave.text = "Salvando..."

        lifecycleScope.launch {
            try {
                // Upload das imagens primeiro
                val imageUrls = uploadImages()

                // Criar produto
                val product = Product(
                    name = name,
                    description = description,
                    price = price,
                    stock = stock,
                    category = category,
                    imageUrls = imageUrls,
                    active = true
                )

                val result = productRepository.saveProduct(product)

                runOnUiThread {
                    btnSave.isEnabled = true
                    btnSave.text = "💾 Salvar Produto"

                    if (result.isSuccess) {
                        Toast.makeText(this@PostItemActivity, "Produto salvo com sucesso!", Toast.LENGTH_SHORT).show()

                        // Voltar para a tela anterior
                        val intent = Intent(this@PostItemActivity, HomeActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                        startActivity(intent)
                        finish()
                    } else {
                        val error = result.exceptionOrNull()
                        Toast.makeText(this@PostItemActivity, "Erro ao salvar: ${error?.message}", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    btnSave.isEnabled = true
                    btnSave.text = "💾 Salvar Produto"
                    Toast.makeText(this@PostItemActivity, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private suspend fun uploadImages(): List<String> {
        val imageUrls = mutableListOf<String>()
        val images = imageAdapter.getImages()

        for ((index, imageUri) in images.withIndex()) {
            try {
                val fileName = "products/${UUID.randomUUID()}_${System.currentTimeMillis()}.jpg"
                val storageRef = storage.reference.child(fileName)

                val uploadTask = storageRef.putFile(imageUri)
                uploadTask.await()

                val downloadUrl = storageRef.downloadUrl.await()
                imageUrls.add(downloadUrl.toString())

                // Atualizar progresso na UI
                runOnUiThread {
                    btnSave.text = "Uploading... ${index + 1}/${images.size}"
                }
            } catch (e: Exception) {
                // Log do erro mas continua com as outras imagens
                e.printStackTrace()
            }
        }

        return imageUrls
    }

    private fun setupBottomNavigation() {
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation_post)
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, HomeActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_cart -> {
                    val intent = Intent(this, CartActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }
}