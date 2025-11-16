package com.unasp.unaspmarketplace

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
import com.unasp.unaspmarketplace.adapters.OrderHistoryAdapter
import com.unasp.unaspmarketplace.models.Order
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class OrderHistoryActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var orderAdapter: OrderHistoryAdapter
    private val ordersList = mutableListOf<Order>()

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_history)

        setupToolbar()
        setupSwipeRefresh()
        setupRecyclerView()
        loadOrderHistory()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Histórico de Pedidos"
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupSwipeRefresh() {
        swipeRefresh = findViewById(R.id.swipeRefresh)
        swipeRefresh.setOnRefreshListener {
            loadOrderHistory()
        }
    }

    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.recyclerOrderHistory)
        orderAdapter = OrderHistoryAdapter(ordersList) { order ->
            // Callback para quando um pedido for clicado
            showOrderDetails(order)
        }

        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@OrderHistoryActivity)
            adapter = orderAdapter
        }
    }

    private fun loadOrderHistory() {
        val userId = auth.currentUser?.uid ?: return

        // Mostrar loading apenas se não for refresh
        if (!swipeRefresh.isRefreshing) {
            swipeRefresh.isRefreshing = true
        }

        lifecycleScope.launch {
            try {
                val ordersSnapshot = firestore.collection("orders")
                    .whereEqualTo("userId", userId)
                    .get()
                    .await()

                ordersList.clear()
                for (document in ordersSnapshot.documents) {
                    val order = document.toObject(Order::class.java)
                    order?.let { ordersList.add(it) }
                }

                // Ordenar localmente por timestamp (mais recente primeiro)
                ordersList.sortByDescending { it.timestamp }

                orderAdapter.notifyDataSetChanged()

                if (ordersList.isEmpty()) {
                    Toast.makeText(this@OrderHistoryActivity, "Nenhum pedido encontrado", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                Toast.makeText(this@OrderHistoryActivity, "Erro ao carregar pedidos: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                swipeRefresh.isRefreshing = false
            }
        }
    }

    private fun showOrderDetails(order: Order) {
        // Aqui você pode abrir uma nova activity ou dialog com detalhes do pedido
        Toast.makeText(this, "Pedido ${order.id} - Total: R$ ${String.format("%.2f", order.getTotalAmount())}", Toast.LENGTH_LONG).show()
    }
}

// Extensão para calcular total do pedido
private fun Order.getTotalAmount(): Double {
    return items.sumOf { it.totalPrice }
}

