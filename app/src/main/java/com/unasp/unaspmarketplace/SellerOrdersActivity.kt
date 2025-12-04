package com.unasp.unaspmarketplace

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.button.MaterialButton
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.unasp.unaspmarketplace.adapters.SellerOrdersAdapter
import com.unasp.unaspmarketplace.models.Order
import com.unasp.unaspmarketplace.models.OrderStatus
import com.unasp.unaspmarketplace.repository.OrderRepository
import kotlinx.coroutines.launch

/**
 * Activity para vendedores gerenciarem seus pedidos recebidos
 */
class SellerOrdersActivity : AppCompatActivity() {

    private lateinit var tabLayout: TabLayout
    private lateinit var recyclerOrders: RecyclerView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var emptyStateView: View
    private lateinit var btnGoToProducts: MaterialButton

    private lateinit var orderRepository: OrderRepository
    private lateinit var sellerOrdersAdapter: SellerOrdersAdapter
    private val allOrders = mutableListOf<Order>()
    private val filteredOrders = mutableListOf<Order>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seller_orders)

        setupToolbar()
        initViews()
        setupRecyclerView()
        setupTabs()
        loadSellerOrders()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Pedidos Recebidos"
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun initViews() {
        tabLayout = findViewById(R.id.tabLayout)
        recyclerOrders = findViewById(R.id.recyclerOrders)
        swipeRefresh = findViewById(R.id.swipeRefresh)
        emptyStateView = findViewById(R.id.emptyStateView)
        btnGoToProducts = findViewById(R.id.btnGoToProducts)

        orderRepository = OrderRepository()

        swipeRefresh.setOnRefreshListener {
            loadSellerOrders()
        }

        btnGoToProducts.setOnClickListener {
            finish() // Volta para a tela anterior
        }
    }

    private fun setupRecyclerView() {
        sellerOrdersAdapter = SellerOrdersAdapter(
            filteredOrders,
            onCompleteOrder = { order -> showCompleteOrderDialog(order) },
            onViewDetails = { order -> showOrderDetails(order) }
        )

        recyclerOrders.apply {
            layoutManager = LinearLayoutManager(this@SellerOrdersActivity)
            adapter = sellerOrdersAdapter
        }
    }

    private fun setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("Todos"))
        tabLayout.addTab(tabLayout.newTab().setText("Pendentes"))
        tabLayout.addTab(tabLayout.newTab().setText("Confirmados"))
        tabLayout.addTab(tabLayout.newTab().setText("Concluídos"))

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                filterOrdersByTab(tab.position)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    private fun loadSellerOrders() {
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
                val result = orderRepository.getSellerOrders(userId)

                if (result.isSuccess) {
                    val orders = result.getOrNull() ?: emptyList()
                    allOrders.clear()
                    allOrders.addAll(orders)

                    runOnUiThread {
                        filterOrdersByTab(tabLayout.selectedTabPosition)
                    }
                } else {
                    val error = result.exceptionOrNull()
                    runOnUiThread {
                        Toast.makeText(this@SellerOrdersActivity,
                            "Erro ao carregar pedidos: ${error?.message}",
                            Toast.LENGTH_LONG).show()
                        showEmptyState()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@SellerOrdersActivity,
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

    private fun filterOrdersByTab(tabPosition: Int) {
        filteredOrders.clear()

        when (tabPosition) {
            0 -> filteredOrders.addAll(allOrders) // Todos
            1 -> filteredOrders.addAll(allOrders.filter { it.getOrderStatus() == OrderStatus.PENDING }) // Pendentes
            2 -> filteredOrders.addAll(allOrders.filter {
                val status = it.getOrderStatus()
                status == OrderStatus.CONFIRMED || status == OrderStatus.PREPARING || status == OrderStatus.READY
            }) // Confirmados
            3 -> filteredOrders.addAll(allOrders.filter { it.getOrderStatus() == OrderStatus.COMPLETED }) // Concluídos
        }

        if (filteredOrders.isEmpty()) {
            showEmptyState()
        } else {
            showOrdersState()
            sellerOrdersAdapter.notifyDataSetChanged()
        }
    }

    private fun showCompleteOrderDialog(order: Order) {
        AlertDialog.Builder(this)
            .setTitle("Concluir Pedido")
            .setMessage(
                "Confirma a conclusão do pedido #${order.id.take(8)}?\n\n" +
                "Cliente: ${order.buyerName}\n" +
                "Total: R$ ${String.format("%.2f", order.totalAmount)}\n\n" +
                "Esta ação marcará o pedido como concluído."
            )
            .setPositiveButton("✅ Concluir") { _, _ ->
                completeOrder(order)
            }
            .setNegativeButton("❌ Cancelar", null)
            .show()
    }

    private fun completeOrder(order: Order) {
        lifecycleScope.launch {
            try {
                val result = orderRepository.updateOrderStatus(order.id, OrderStatus.COMPLETED)

                runOnUiThread {
                    if (result.isSuccess) {
                        Toast.makeText(this@SellerOrdersActivity,
                            "✅ Pedido concluído com sucesso!",
                            Toast.LENGTH_SHORT).show()
                        loadSellerOrders() // Recarregar lista
                    } else {
                        val error = result.exceptionOrNull()
                        Toast.makeText(this@SellerOrdersActivity,
                            "❌ Erro ao concluir pedido: ${error?.message}",
                            Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@SellerOrdersActivity,
                        "❌ Erro: ${e.message}",
                        Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun showOrderDetails(order: Order) {
        val details = buildString {
            appendLine("📋 DETALHES DO PEDIDO")
            appendLine("━━━━━━━━━━━━━━━━━━━━━━")
            appendLine()
            appendLine("🆔 ID: #${order.id.take(8)}")
            appendLine("👤 Cliente: ${order.buyerName}")
            appendLine("📧 Email: ${order.buyerEmail}")
            appendLine("📱 WhatsApp: ${order.buyerWhatsApp}")
            appendLine("📅 Data: ${java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date(order.createdAt))}")
            appendLine("💳 Pagamento: ${order.paymentMethod}")
            appendLine("📊 Status: ${order.getOrderStatus().displayName}")
            appendLine()
            appendLine("🛍️ ITENS:")
            order.items.forEach { item ->
                appendLine("• ${item.quantity}x ${item.productName}")
                appendLine("   R$ ${String.format("%.2f", item.unitPrice)} cada = R$ ${String.format("%.2f", item.totalPrice)}")
            }
            appendLine()
            appendLine("💰 TOTAL: R$ ${String.format("%.2f", order.totalAmount)}")
        }

        AlertDialog.Builder(this)
            .setTitle("Detalhes do Pedido")
            .setMessage(details)
            .setPositiveButton("✅ OK", null)
            .show()
    }

    private fun showEmptyState() {
        recyclerOrders.visibility = View.GONE
        emptyStateView.visibility = View.VISIBLE
    }

    private fun showOrdersState() {
        recyclerOrders.visibility = View.VISIBLE
        emptyStateView.visibility = View.GONE
    }
}
