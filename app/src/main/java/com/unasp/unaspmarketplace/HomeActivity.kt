package com.unasp.unaspmarketplace

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.unasp.unaspmarketplace.modelos.Category
import com.unasp.unaspmarketplace.modelos.CategoryAdapter
import com.unasp.unaspmarketplace.modelos.ProductAdapter
import android.content.Intent
import androidx.drawerlayout.widget.DrawerLayout
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.google.android.material.navigation.NavigationView
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.core.view.GravityCompat
import com.unasp.unaspmarketplace.models.Product
import com.unasp.unaspmarketplace.repository.ProductRepository
import com.unasp.unaspmarketplace.utils.CartManager
import com.unasp.unaspmarketplace.utils.CartBadgeManager
import com.unasp.unaspmarketplace.utils.UserUtils
import kotlinx.coroutines.launch

class HomeActivity : AppCompatActivity(), CartManager.CartUpdateListener {
    private lateinit var productRepository: ProductRepository
    private lateinit var productAdapter: ProductAdapter
    private lateinit var recyclerProducts: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_activity)

        // Verificar e garantir que os dados do usuário existam
        ensureUserData()

        // Inicializar repositório
        productRepository = ProductRepository()

        setupCategories()
        setupProducts()
        setupNavigation()

        // Carregar produtos
        loadProducts()

        // Registrar listener do carrinho
        CartManager.addListener(this)
    }

    private fun ensureUserData() {
        lifecycleScope.launch {
            try {
                UserUtils.ensureUserDataExists()
            } catch (e: Exception) {
                // Log error but don't crash the app
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        CartManager.removeListener(this)
    }

    override fun onCartUpdated(itemCount: Int, totalPrice: Double) {
        CartBadgeManager.updateBadge(itemCount)
    }

    private fun setupCategories() {
        val categorys = listOf(
            Category("Roupas", R.drawable.tshirt_logo),
            Category("Eletrônicos", R.drawable.computer_logo),
            Category("Alimentos", R.drawable.apple_logo),
            Category("Livros", R.drawable.book_logo)
        )

        val recyclerCategory = findViewById<RecyclerView>(R.id.recyclerCategorys)
        recyclerCategory.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerCategory.adapter = CategoryAdapter(categorys)
    }

    private fun setupProducts() {
        recyclerProducts = findViewById(R.id.recyclerProducts)
        recyclerProducts.layoutManager = GridLayoutManager(this, 2)

        // Inicializar adapter com lista vazia
        productAdapter = ProductAdapter(mutableListOf())
        recyclerProducts.adapter = productAdapter
    }

    private fun setupNavigation() {
        // Configuração do menu lateral
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawerLayout)
        val navigationView = findViewById<NavigationView>(R.id.navigationView)

        // Verificar se deve abrir o menu automaticamente
        if (intent.getBooleanExtra("openMenu", false)) {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        // Configuração da hotbar inferior
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)

        // Configurar badge do carrinho
        CartBadgeManager.setupCartBadge(bottomNavigation)
        CartBadgeManager.updateBadge(CartManager.getTotalItemCount())

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_menu -> {
                    // Abre o menu lateral
                    drawerLayout.openDrawer(GravityCompat.START)
                    true
                }
                R.id.nav_home -> {
                    // Já estamos na home, não precisa fazer nada
                    Toast.makeText(this, "Você já está na Home", Toast.LENGTH_SHORT).show()
                    true
                }
                /*R.id.nav_notifications -> {
                    // Implementar navegação para notificações
                    Toast.makeText(this, "Notificações em breve", Toast.LENGTH_SHORT).show()
                    true
                }*/
                R.id.nav_cart -> {
                    // Navegar para o carrinho
                    val intent = Intent(this, CartActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
        bottomNavigation.selectedItemId = R.id.nav_home
        // Configuração do menu lateral
        navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_post_item -> {
                    val intent = Intent(this, PostItemActivity::class.java)
                    startActivity(intent)
                }
                R.id.nav_profile -> {
                    val intent = Intent(this, ProfileActivity::class.java)
                    startActivity(intent)
                }
            }
            drawerLayout.closeDrawers() // fecha o menu depois do clique
            true
        }
    }

    private fun loadProducts() {
        lifecycleScope.launch {
            try {
                // Primeiro vamos tentar criar um produto de teste se não houver nenhum
                createTestProductIfNeeded()

                val result = productRepository.getActiveProducts()

                if (result.isSuccess) {
                    val products = result.getOrNull() ?: emptyList()
                    runOnUiThread {
                        productAdapter.updateProducts(products)

                        if (products.isEmpty()) {
                            Toast.makeText(this@HomeActivity, "Nenhum produto encontrado. Que tal publicar o primeiro?", Toast.LENGTH_LONG).show()
                            // Tentar carregar produtos de exemplo
                            loadSampleProducts()
                        } else {
                            Toast.makeText(this@HomeActivity, "Carregados ${products.size} produtos!", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    val error = result.exceptionOrNull()
                    val errorMessage = error?.message ?: "Erro desconhecido"
                    runOnUiThread {
                        Toast.makeText(this@HomeActivity, "Erro Firebase: $errorMessage", Toast.LENGTH_LONG).show()

                        // Carregar produtos de exemplo como fallback
                        loadSampleProducts()
                        Toast.makeText(this@HomeActivity, "Carregando produtos de exemplo...", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@HomeActivity, "Erro de conexão: ${e.message}", Toast.LENGTH_LONG).show()
                    loadSampleProducts()
                    Toast.makeText(this@HomeActivity, "Carregando produtos de exemplo...", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private suspend fun createTestProductIfNeeded() {
        try {
            // Verificar se já existem produtos
            val existingProducts = productRepository.getActiveProducts()
            if (existingProducts.isSuccess && existingProducts.getOrNull()?.isEmpty() == true) {
                // Criar um produto de teste
                val testProduct = Product(
                    name = "Produto Teste",
                    description = "Este é um produto de teste criado automaticamente",
                    price = 99.99,
                    category = "Eletrônicos",
                    stock = 10,
                    imageUrls = emptyList(),
                    active = true
                )

                val saveResult = productRepository.saveProduct(testProduct)
                if (saveResult.isSuccess) {
                    android.util.Log.d("HomeActivity", "Produto de teste criado com sucesso")
                } else {
                    android.util.Log.e("HomeActivity", "Erro ao criar produto de teste: ${saveResult.exceptionOrNull()?.message}")
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("HomeActivity", "Erro ao verificar/criar produto de teste: ${e.message}")
        }
    }

    private fun loadSampleProducts() {
        // Produtos de exemplo caso não consiga carregar do Firebase
        val sampleProducts = listOf(
            Product(
                name = "Notebook Dell",
                description = "Notebook Dell Inspiron",
                price = 3500.0,
                category = "Eletrônicos",
                stock = 5,
                active = true
            ),
            Product(
                name = "Camiseta Azul",
                description = "Camiseta Nike azul",
                price = 79.9,
                category = "Roupas",
                stock = 10,
                active = true
            ),
            Product(
                name = "Livro Kotlin",
                description = "Livro sobre programação Kotlin",
                price = 120.0,
                category = "Livros",
                stock = 3,
                active = true
            )
        )

        productAdapter.updateProducts(sampleProducts)
    }

    override fun onResume() {
        super.onResume()
        // Recarregar produtos quando voltar para a tela
        loadProducts()
    }
}
