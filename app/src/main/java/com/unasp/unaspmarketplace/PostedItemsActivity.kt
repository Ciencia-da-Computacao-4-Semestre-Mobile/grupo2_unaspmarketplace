package com.unasp.unaspmarketplace

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.unasp.unaspmarketplace.adapters.PostedItemsAdapter
import com.unasp.unaspmarketplace.models.Product
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class PostedItemsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var postedItemsAdapter: PostedItemsAdapter
    private val productsList = mutableListOf<Product>()

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_posted_items)

        setupToolbar()
        setupSwipeRefresh()
        setupRecyclerView()
        loadPostedItems()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Meus Itens Postados"
        toolbar.setNavigationOnClickListener { finish() }

        // Menu de teste (temporário)
        toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                android.R.id.home -> {
                    testFirestoreConnection()
                    true
                }
                else -> false
            }
        }
    }

    private fun testFirestoreConnection() {
        lifecycleScope.launch {
            try {
                android.util.Log.d("PostedItems", "Testando conexão com Firestore...")

                // Buscar todos os produtos sem filtro
                val allProducts = firestore.collection("products").get().await()
                android.util.Log.d("PostedItems", "Total de produtos no Firestore: ${allProducts.size()}")

                for (doc in allProducts.documents) {
                    android.util.Log.d("PostedItems", "Produto: ${doc.id} - sellerId: ${doc.get("sellerId")} - nome: ${doc.get("name")}")
                }

                val userId = auth.currentUser?.uid
                android.util.Log.d("PostedItems", "ID do usuário atual: $userId")

                Toast.makeText(this@PostedItemsActivity, "Total produtos: ${allProducts.size()}, Usuário: $userId", Toast.LENGTH_LONG).show()

            } catch (e: Exception) {
                android.util.Log.e("PostedItems", "Erro no teste", e)
                Toast.makeText(this@PostedItemsActivity, "Erro no teste: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setupSwipeRefresh() {
        swipeRefresh = findViewById(R.id.swipeRefresh)
        swipeRefresh.setOnRefreshListener {
            loadPostedItems()
        }
    }

    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.recyclerPostedItems)
        postedItemsAdapter = PostedItemsAdapter(
            productsList,
            onEditClick = { product -> editProduct(product) },
            onToggleVisibilityClick = { product -> toggleProductVisibility(product) },
            onDeleteClick = { product -> confirmDeleteProduct(product) }
        )

        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@PostedItemsActivity)
            adapter = postedItemsAdapter
        }
    }

    private fun loadPostedItems() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "Usuário não logado", Toast.LENGTH_SHORT).show()
            return
        }

        // Mostrar loading apenas se não for refresh
        if (!swipeRefresh.isRefreshing) {
            swipeRefresh.isRefreshing = true
        }

        lifecycleScope.launch {
            try {
                android.util.Log.d("PostedItems", "Buscando produtos para usuário: $userId")

                val productsSnapshot = firestore.collection("products")
                    .whereEqualTo("sellerId", userId)
                    .get()
                    .await()

                android.util.Log.d("PostedItems", "Documentos encontrados: ${productsSnapshot.size()}")

                productsList.clear()
                for (document in productsSnapshot.documents) {
                    android.util.Log.d("PostedItems", "Documento ID: ${document.id}, dados: ${document.data}")
                    val product = document.toObject(Product::class.java)?.copy(id = document.id)
                    product?.let {
                        productsList.add(it)
                        android.util.Log.d("PostedItems", "Produto adicionado: ${it.name}")
                    }
                }

                // Ordenar localmente por data de criação (mais recente primeiro)
                productsList.sortByDescending { it.createdAt }

                postedItemsAdapter.notifyDataSetChanged()

                if (productsList.isEmpty()) {
                    Toast.makeText(this@PostedItemsActivity, "Você ainda não postou nenhum item", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@PostedItemsActivity, "${productsList.size} itens carregados", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                android.util.Log.e("PostedItems", "Erro ao carregar itens", e)
                Toast.makeText(this@PostedItemsActivity, "Erro ao carregar itens: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                swipeRefresh.isRefreshing = false
            }
        }
    }

    private fun editProduct(product: Product) {
        // Abrir tela de edição de produto
        val intent = Intent(this, PostItemActivity::class.java).apply {
            putExtra("productId", product.id)
            putExtra("editMode", true)
        }
        startActivity(intent)
    }

    private fun toggleProductVisibility(product: Product) {
        lifecycleScope.launch {
            try {
                val newActiveStatus = !product.active

                firestore.collection("products")
                    .document(product.id)
                    .update("active", newActiveStatus)
                    .await()

                // Atualizar na lista local
                val index = productsList.indexOfFirst { it.id == product.id }
                if (index != -1) {
                    productsList[index] = product.copy(active = newActiveStatus)
                    postedItemsAdapter.notifyItemChanged(index)
                }

                val status = if (newActiveStatus) "visível" else "oculto"
                Toast.makeText(this@PostedItemsActivity, "Produto agora está $status", Toast.LENGTH_SHORT).show()

            } catch (e: Exception) {
                Toast.makeText(this@PostedItemsActivity, "Erro ao alterar visibilidade: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun confirmDeleteProduct(product: Product) {
        AlertDialog.Builder(this)
            .setTitle("Remover Produto")
            .setMessage("Tem certeza que deseja remover \"${product.name}\"? Esta ação não pode ser desfeita.")
            .setPositiveButton("Remover") { _, _ ->
                deleteProduct(product)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun deleteProduct(product: Product) {
        lifecycleScope.launch {
            try {
                firestore.collection("products")
                    .document(product.id)
                    .delete()
                    .await()

                // Remover da lista local
                val index = productsList.indexOfFirst { it.id == product.id }
                if (index != -1) {
                    productsList.removeAt(index)
                    postedItemsAdapter.notifyItemRemoved(index)
                }

                Toast.makeText(this@PostedItemsActivity, "Produto removido com sucesso", Toast.LENGTH_SHORT).show()

            } catch (e: Exception) {
                Toast.makeText(this@PostedItemsActivity, "Erro ao remover produto: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Recarregar lista quando voltar para a tela (caso tenha editado algum produto)
        loadPostedItems()
    }
}


