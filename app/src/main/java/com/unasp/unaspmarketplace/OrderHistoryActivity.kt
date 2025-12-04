package com.unasp.unaspmarketplace

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.unasp.unaspmarketplace.adapters.OrderHistoryAdapter
import com.unasp.unaspmarketplace.models.Order
import com.unasp.unaspmarketplace.repository.OrderRepository
import kotlinx.coroutines.launch

class OrderHistoryActivity : AppCompatActivity() {

    private lateinit var recyclerOrders: RecyclerView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var txtEmptyState: View
    private lateinit var btnStartShopping: MaterialButton

    private lateinit var orderRepository: OrderRepository
    private lateinit var orderAdapter: OrderHistoryAdapter
    private val ordersList = mutableListOf<Order>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_history)

        setupToolbar()
        initViews()
        setupRecyclerView()
        setupClickListeners()
        loadOrderHistory()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Histórico de Pedidos"
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun initViews() {
        recyclerOrders = findViewById(R.id.recyclerOrders)
        swipeRefresh = findViewById(R.id.swipeRefresh)
        txtEmptyState = findViewById(R.id.txtEmptyState)
        btnStartShopping = findViewById(R.id.btnStartShopping)

        orderRepository = OrderRepository()

        swipeRefresh.setOnRefreshListener {
            loadOrderHistory()
        }
    }

    private fun setupRecyclerView() {
        orderAdapter = OrderHistoryAdapter(ordersList) { order ->
            // Quando clicar em um pedido, mostrar detalhes
            showOrderDetails(order)
        }

        recyclerOrders.apply {
            layoutManager = LinearLayoutManager(this@OrderHistoryActivity)
            adapter = orderAdapter
        }
    }

    private fun setupClickListeners() {
        btnStartShopping.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }
    }

    private fun loadOrderHistory() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "Usuário não logado", Toast.LENGTH_SHORT).show()
            showEmptyState()
            return
        }

        if (!swipeRefresh.isRefreshing) {
            swipeRefresh.isRefreshing = true
        }

        lifecycleScope.launch {
            try {
                val result = orderRepository.getBuyerOrders(userId)

                if (result.isSuccess) {
                    val orders = result.getOrNull() ?: emptyList()
                    ordersList.clear()
                    ordersList.addAll(orders)

                    runOnUiThread {
                        if (orders.isEmpty()) {
                            showEmptyState()
                        } else {
                            showOrdersState()
                            orderAdapter.notifyDataSetChanged()
                        }
                    }
                } else {
                    val error = result.exceptionOrNull()
                    runOnUiThread {
                        Toast.makeText(this@OrderHistoryActivity,
                            "Erro ao carregar pedidos: ${error?.message}",
                            Toast.LENGTH_LONG).show()
                        showEmptyState()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@OrderHistoryActivity,
                        "Erro: ${e.message}",
                        Toast.LENGTH_LONG).show()
                    showEmptyState()
                }
            } finally {
                runOnUiThread {
                    swipeRefresh.isRefreshing = false
                }
            }
        }
    }

    private fun showEmptyState() {
        recyclerOrders.visibility = View.GONE
        txtEmptyState.visibility = View.VISIBLE
    }

    private fun showOrdersState() {
        recyclerOrders.visibility = View.VISIBLE
        txtEmptyState.visibility = View.GONE
    }

    private fun showOrderDetails(order: Order) {
        val intent = Intent(this, OrderDetailsActivity::class.java)
        intent.putExtra("ORDER_ID", order.id)
        startActivity(intent)
    }
}

