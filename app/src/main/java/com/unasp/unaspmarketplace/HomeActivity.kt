package com.unasp.unaspmarketplace

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.unasp.unaspmarketplace.models.Category
import com.unasp.unaspmarketplace.models.CategoryAdapter
import android.content.Intent
import androidx.drawerlayout.widget.DrawerLayout
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.google.android.material.navigation.NavigationView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.unasp.unaspmarketplace.models.Product
import com.unasp.unaspmarketplace.modelos.ProductAdapter
import com.unasp.unaspmarketplace.repository.ProductRepository
import com.unasp.unaspmarketplace.utils.CartManager
import com.unasp.unaspmarketplace.utils.CartBadgeManager
import com.unasp.unaspmarketplace.utils.UserUtils
import kotlinx.coroutines.launch
import androidx.core.view.GravityCompat
import android.widget.Toast

class HomeActivity : AppCompatActivity(), CartManager.CartUpdateListener {
    private lateinit var productRepository: ProductRepository
    private lateinit var productAdapter: ProductAdapter
    private lateinit var recyclerProducts: RecyclerView
    private var suppressHomeToast = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_activity) // use o layout correto do seu XML

        // Sessão/usuário (se necessário para permissões ou personalização)
        ensureUserData()

        // Repositório
        productRepository = ProductRepository()

        // UI: RecyclerViews + Adapters
        setupCategories()
        setupProducts()

        // Navegação (drawer, bottom nav, etc.)
        setupNavigation()

        // Carregar produtos (coroutines com lifecycleScope)
        loadProducts()

        // Listener do carrinho
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
        productAdapter = ProductAdapter(mutableListOf())
        recyclerProducts.adapter = productAdapter
        recyclerProducts.setHasFixedSize(true)
    }


    private var userClickedHome = false

    private fun setupNavigation() {
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawerLayout)
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        val navigationView = findViewById<NavigationView>(R.id.navigationView)

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_menu -> {
                    drawerLayout.openDrawer(GravityCompat.START)
                    true
                }
                R.id.nav_home -> {
                    if (userClickedHome) {
                        Toast.makeText(this, "Você já está na Home", Toast.LENGTH_SHORT).show()
                    }
                    userClickedHome = false
                    true
                }
                R.id.nav_cart -> {
                    startActivity(Intent(this, CartActivity::class.java))
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }

        drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerClosed(drawerView: View) {
                // seleção programática → não dispara Toast
                userClickedHome = false
                bottomNavigation.selectedItemId = R.id.nav_home
            }
            override fun onDrawerOpened(drawerView: View) {}
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}
            override fun onDrawerStateChanged(newState: Int) {}
        })

        navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                //R. id.nav_post_item -> startActivity(Intent(this, PostItemActivity::class.java))

                //R.id.nav_profile -> startActivity(Intent(this, ProfileActivity::class.java))

                R.id.nav_orders -> Toast.makeText(this, "Meus pedidos em breve", Toast.LENGTH_SHORT).show()

                R.id.nav_posted_items -> {
                    val intent = Intent(this, PostedItemsActivity::class.java)
                    startActivity(intent)
                }

                R.id.nav_history -> Toast.makeText(this, "Histórico em breve", Toast.LENGTH_SHORT).show()

                R.id.nav_settings -> Toast.makeText(this, "Configurações em breve", Toast.LENGTH_SHORT).show()

                R.id.nav_logout -> {
                    val prefs = getSharedPreferences("user_session", MODE_PRIVATE)
                    prefs.edit().clear().apply()
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
            }
            drawerLayout.closeDrawers()
            bottomNavigation.selectedItemId = R.id.nav_home
            true
        }
            drawerLayout.closeDrawers()
            // seleção programática → não dispara Toast
            userClickedHome = false
            bottomNavigation.selectedItemId = R.id.nav_home
            true
    }

        // clique inicial na Home → não dispara Toast
        //userClickedHome = false
        //bottomNavigation.selectedItemId = R.id.nav_home

        // Para capturar cliques manuais na Home
        //bottomNavigation.setOnItemReselectedListener { item ->
        //    if (item.itemId == R.id.nav_home) {
        //        Toast.makeText(this, "Você já está na Home", Toast.LENGTH_SHORT).show()
        //   }
        //}
    //}


    private fun loadProducts() {
        lifecycleScope.launch {
            try {
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
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNavigation.selectedItemId = R.id.nav_home
    }
}
