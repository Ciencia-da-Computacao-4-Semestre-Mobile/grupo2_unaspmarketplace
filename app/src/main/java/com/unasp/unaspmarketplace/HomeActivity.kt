package com.unasp.unaspmarketplace

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.unasp.unaspmarketplace.models.Category
import com.unasp.unaspmarketplace.models.CategoryAdapter
import com.unasp.unaspmarketplace.models.ProductAdapter
import android.view.View
import android.content.Intent
import androidx.drawerlayout.widget.DrawerLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.google.android.material.navigation.NavigationView
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.core.view.GravityCompat
import com.unasp.unaspmarketplace.models.Product
import com.unasp.unaspmarketplace.repository.ProductRepository
import com.unasp.unaspmarketplace.repository.UserRepository
import com.unasp.unaspmarketplace.utils.CartManager
import com.unasp.unaspmarketplace.utils.CartBadgeManager
import com.unasp.unaspmarketplace.utils.UserUtils
import kotlinx.coroutines.launch
import android.widget.ImageView
import androidx.appcompat.widget.SearchView
import android.widget.TextView
import android.content.SharedPreferences

class HomeActivity : AppCompatActivity(), CartManager.CartUpdateListener {

    // 🔹 Atributos da Activity (estado e referências)
    private lateinit var productRepository: ProductRepository
    private lateinit var productAdapter: ProductAdapter
    private lateinit var recyclerProducts: RecyclerView
    private lateinit var searchView: SearchView
    private var allProducts = mutableListOf<Product>()
    private var filteredProducts = mutableListOf<Product>()
    private lateinit var searchPrefs: SharedPreferences
    private lateinit var categoryAdapter: CategoryAdapter
    private var selectedCategory: String = "Todos"

    // 🔹 Ciclo de vida da Activity
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_activity)

        // Inicialização das views
        searchView = findViewById(R.id.searchView)
        val bannerPromo = findViewById<ImageView>(R.id.bannerPromo)

        // Configuração da barra de pesquisa
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrEmpty()) {
                    // 🔹 Pesquisa ativa → esconder banner + categorias
                    bannerPromo.visibility = View.GONE
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrEmpty()) {
                    // 🔹 Pesquisa limpa → mostrar tudo de novo
                    bannerPromo.visibility = View.VISIBLE
                }
                return true
            }
        })

        // Inicialização de dados e lógica
        ensureUserData()
        productRepository = ProductRepository()
        searchPrefs = getSharedPreferences("search_history", MODE_PRIVATE)

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
            Category("Todos", R.drawable.ic_launcher_foreground), // Categoria especial para mostrar todos
            Category("Roupas", R.drawable.tshirt_logo),
            Category("Eletrônicos", R.drawable.computer_logo),
            Category("Alimentos", R.drawable.apple_logo),
            Category("Livros", R.drawable.book_logo)
        )

        val recyclerCategory = findViewById<RecyclerView>(R.id.recyclerCategorys)
        recyclerCategory.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        // Criar adapter com callback para busca por categoria
        categoryAdapter = CategoryAdapter(categorys) { categoryName ->
            selectedCategory = categoryName

            if (categoryName == "Todos") {
                // Limpar busca e mostrar todos os produtos
                clearSearch()
                Toast.makeText(this, "Mostrando todos os produtos", Toast.LENGTH_SHORT).show()
            } else {
                // Quando uma categoria for clicada, fazer busca por essa categoria
                searchView.setQuery(categoryName, true)

                // Feedback visual para o usuário
                Toast.makeText(this, "Buscando produtos em: $categoryName", Toast.LENGTH_SHORT).show()

                // Limpar foco da SearchView para esconder o teclado
                searchView.clearFocus()
            }
        }

        recyclerCategory.adapter = categoryAdapter
    }

    private fun setupProducts() {
        recyclerProducts = findViewById(R.id.recyclerProducts)
        recyclerProducts.layoutManager = GridLayoutManager(this, 2)

        // Inicializar adapter com lista vazia
        productAdapter = ProductAdapter(filteredProducts)
        recyclerProducts.adapter = productAdapter
    }

    private fun setupSearchView() {
        searchView = findViewById(R.id.searchView)

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrBlank()) {
                    saveSearchToHistory(query)
                }
                searchProducts(query)
                searchView.clearFocus()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                searchProducts(newText)
                return true
            }
        })

        // Configurar aparência da SearchView
        searchView.queryHint = "Buscar produtos, categorias ou preços..."
        searchView.isIconified = false
        searchView.isSubmitButtonEnabled = false
        searchView.clearFocus()

        // Adicionar dica de uso quando o campo estiver vazio
        showSearchSuggestions()
    }

    private fun showSearchSuggestions() {
        // Criar uma TextView temporária para mostrar dicas de busca
        val searchHints = listOf(
            "💡 Dicas de busca:",
            "• Digite o nome do produto",
            "• Busque por categoria (Eletrônicos, Roupas, etc.)",
            "• Use 'até 100' para preços baixos",
            "• Use 'entre 50 e 200' para faixa de preço"
        )

        // Essas dicas podem ser mostradas em um Toast quando o usuário tocar no SearchView
        searchView.setOnSearchClickListener {
            Toast.makeText(this, searchHints.joinToString("\n"), Toast.LENGTH_LONG).show()
        }
    }

    private fun searchProducts(query: String?) {
        if (query.isNullOrBlank()) {
            // Se a busca estiver vazia, mostrar todos os produtos
            updateFilteredProducts(allProducts)
        } else {
            // Verificar se é busca por preço (ex: "menor que 100", "até 50", "entre 10 e 100")
            val priceFiltered = when {
                query.contains("menor que", ignoreCase = true) || query.contains("até", ignoreCase = true) -> {
                    val price = extractPrice(query)
                    if (price != null) allProducts.filter { it.price <= price } else null
                }
                query.contains("maior que", ignoreCase = true) || query.contains("acima", ignoreCase = true) -> {
                    val price = extractPrice(query)
                    if (price != null) allProducts.filter { it.price >= price } else null
                }
                query.contains("entre", ignoreCase = true) -> {
                    val prices = extractPriceRange(query)
                    if (prices != null) {
                        allProducts.filter { it.price >= prices.first && it.price <= prices.second }
                    } else null
                }
                else -> null
            }

            val filtered = if (priceFiltered != null) {
                priceFiltered
            } else {
                // Busca normal por nome, categoria ou descrição
                allProducts.filter { product ->
                    product.name.contains(query, ignoreCase = true) ||
                    product.category.contains(query, ignoreCase = true) ||
                    product.description.contains(query, ignoreCase = true)
                }
            }

            // Ordenar resultados por relevância (nome primeiro, depois categoria, depois descrição)
            val sortedFiltered = filtered.sortedWith { p1, p2 ->
                when {
                    p1.name.contains(query, ignoreCase = true) && !p2.name.contains(query, ignoreCase = true) -> -1
                    !p1.name.contains(query, ignoreCase = true) && p2.name.contains(query, ignoreCase = true) -> 1
                    else -> p1.name.compareTo(p2.name, ignoreCase = true)
                }
            }

            updateFilteredProducts(sortedFiltered)

            // Mostrar toast com resultado da busca
            if (sortedFiltered.isEmpty()) {
                Toast.makeText(this, "Nenhum produto encontrado para \"$query\"", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "${sortedFiltered.size} produto(s) encontrado(s)", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Extrair preço de queries como "menor que 100", "até 50"
    private fun extractPrice(query: String): Double? {
        val regex = Regex("""(\d+(?:\.\d+)?)""")
        val match = regex.find(query)
        return match?.value?.toDoubleOrNull()
    }

    // Extrair faixa de preço de queries como "entre 10 e 100"
    private fun extractPriceRange(query: String): Pair<Double, Double>? {
        val regex = Regex("""(\d+(?:\.\d+)?)\s*(?:e|a)\s*(\d+(?:\.\d+)?)""")
        val match = regex.find(query)
        return if (match != null) {
            val min = match.groupValues[1].toDoubleOrNull()
            val max = match.groupValues[2].toDoubleOrNull()
            if (min != null && max != null) Pair(min, max) else null
        } else null
    }

    private fun updateFilteredProducts(products: List<Product>) {
        filteredProducts.clear()
        filteredProducts.addAll(products)
        productAdapter.notifyDataSetChanged()
    }

    private fun clearSearch() {
        searchView.setQuery("", false)
        searchView.clearFocus()
        updateFilteredProducts(allProducts)

        // Resetar seleção de categoria para "Todos"
        selectedCategory = "Todos"
        categoryAdapter.setSelectedCategory("Todos")
    }

    // Método público para atualizar produtos (útil quando um novo produto é adicionado)
    fun refreshProducts() {
        loadProducts()
    }

    // Salvar busca no histórico
    private fun saveSearchToHistory(query: String) {
        if (query.isBlank()) return

        val history = getSearchHistory().toMutableSet()
        history.add(query)

        // Manter apenas as últimas 10 buscas
        val limitedHistory = history.toList().takeLast(10).toSet()

        searchPrefs.edit()
            .putStringSet("search_history", limitedHistory)
            .apply()
    }

    // Recuperar histórico de busca
    private fun getSearchHistory(): Set<String> {
        return searchPrefs.getStringSet("search_history", emptySet()) ?: emptySet()
    }

    // Limpar histórico de busca
    private fun clearSearchHistory() {
        searchPrefs.edit()
            .remove("search_history")
            .apply()
    }

    // Método para busca rápida por categoria (pode ser chamado programaticamente)
    fun searchByCategory(category: String) {
        searchView.setQuery(category, true)
    }

    // Método para busca rápida por preço
    fun searchByPriceRange(min: Double, max: Double) {
        val query = "entre $min e $max"
        searchView.setQuery(query, true)
    }

    private fun setupNavigation() {
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawerLayout)
        val navigationView = findViewById<NavigationView>(R.id.navigationView)
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)

        // Home marcado por padrão
        bottomNavigation.selectedItemId = R.id.nav_home

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // já está na Home
                    true // mantém marcado
                }
                R.id.nav_menu -> {
                    drawerLayout.openDrawer(GravityCompat.START)
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    false // não deixa marcado
                }
                R.id.nav_cart -> {
                    startActivity(Intent(this, CartActivity::class.java))
                    false // não deixa marcado
                }
                else -> false
            }
        }

        // Listener do Drawer para alternar marcação
        drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}

            override fun onDrawerOpened(drawerView: View) {
                bottomNavigation.selectedItemId = R.id.nav_menu
            }

            override fun onDrawerClosed(drawerView: View) {
                bottomNavigation.selectedItemId = R.id.nav_home
            }

            override fun onDrawerStateChanged(newState: Int) {}
        })

        // Menu lateral
        navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_post_item -> startActivity(Intent(this, PostItemActivity::class.java))
                R.id.nav_posted_items -> startActivity(Intent(this, PostedItemsActivity::class.java))
                R.id.nav_history -> startActivity(Intent(this, SettingsActivity::class.java))
                R.id.nav_logout -> showLogoutConfirmationDialog()
            }
            drawerLayout.closeDrawers()
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
                        // Atualizar lista principal de produtos
                        allProducts.clear()
                        allProducts.addAll(products)

                        // Inicialmente mostrar todos os produtos
                        updateFilteredProducts(allProducts)

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

        // Atualizar lista principal e filtrada
        allProducts.clear()
        allProducts.addAll(sampleProducts)
        updateFilteredProducts(allProducts)
    }

    override fun onResume() {
        super.onResume()
        // Recarregar produtos quando voltar para a tela
        loadProducts()
    }

    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Confirmar Logout")
            .setMessage("Tem certeza que deseja sair da sua conta?")
            .setPositiveButton("Sair") { _, _ ->
                performLogout()
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun performLogout() {
        lifecycleScope.launch {
            try {
                // Limpar carrinho
                CartManager.clearCart()

                // Fazer logout através do repository
                val userRepository = UserRepository()
                userRepository.logout()

                // Mostrar mensagem de sucesso
                Toast.makeText(this@HomeActivity, "Logout realizado com sucesso", Toast.LENGTH_SHORT).show()

                // Redirecionar para LoginActivity
                val intent = Intent(this@HomeActivity, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()

            } catch (e: Exception) {
                Toast.makeText(this@HomeActivity, "Erro ao fazer logout: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
