package com.unasp.unaspmarketplace

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.ImageView
import android.widget.Toast
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.unasp.unaspmarketplace.models.Product
import com.unasp.unaspmarketplace.repository.ProductRepository
import kotlinx.coroutines.launch


class PostItemActivity : AppCompatActivity() {
    private lateinit var productRepository: ProductRepository
    private lateinit var edtName: TextInputEditText
    private lateinit var edtDescription: TextInputEditText
    private lateinit var edtPrice: TextInputEditText
    private lateinit var edtStock: TextInputEditText
    private lateinit var spinnerCategory: AutoCompleteTextView
    private lateinit var btnSave: MaterialButton
    private lateinit var btnCancel: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.post_item_activity)

        // Inicializar repositório
        productRepository = ProductRepository()

        // Inicializar views
        initViews()
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

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        btnBack.setOnClickListener { finish() }
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

        // Criar produto
        val product = Product(
            name = name,
            description = description,
            price = price,
            stock = stock,
            category = category,
            imageUrls = emptyList(), // Por enquanto sem imagens
            active = true
        )

        // Salvar no Firebase
        btnSave.isEnabled = false
        btnSave.text = "Salvando..."

        lifecycleScope.launch {
            val result = productRepository.saveProduct(product)

            runOnUiThread {
                btnSave.isEnabled = true
                btnSave.text = "💾 Salvar Produto"

                if (result.isSuccess) {
                    Toast.makeText(this@PostItemActivity, "Produto salvo com sucesso!", Toast.LENGTH_LONG).show()

                    // Voltar para home
                    val intent = Intent(this@PostItemActivity, HomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(intent)
                    finish()
                } else {
                    val error = result.exceptionOrNull()?.message ?: "Erro desconhecido"
                    Toast.makeText(this@PostItemActivity, "Erro ao salvar: $error", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun setupBottomNavigation() {
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation_post)
        bottomNavigation.selectedItemId = R.id.nav_menu // Destacar menu já que viemos dele
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_menu -> {
                    val intent = Intent(this, HomeActivity::class.java)
                    intent.putExtra("openMenu", true)
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.nav_home -> {
                    val intent = Intent(this, HomeActivity::class.java)
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.nav_notifications -> {
                    Toast.makeText(this, "Notificações em breve", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_cart -> {
                    val intent = Intent(this, CartActivity::class.java)
                    startActivity(intent)
                    finish()
                    true
                }
                else -> false
            }
        }
    }
}