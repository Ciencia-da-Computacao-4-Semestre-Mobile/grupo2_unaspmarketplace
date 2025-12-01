package com.unasp.unaspmarketplace

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.unasp.unaspmarketplace.models.Order
import com.unasp.unaspmarketplace.repository.OrderRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class OrderDetailsActivity : AppCompatActivity() {

    private lateinit var txtOrderDetails: TextView
    private lateinit var btnClose: MaterialButton
    private lateinit var orderRepository: OrderRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_details)

        setupToolbar()
        initViews()
        loadOrderDetails()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Detalhes do Pedido"
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun initViews() {
        txtOrderDetails = findViewById(R.id.txtOrderDetails)
        btnClose = findViewById(R.id.btnClose)
        orderRepository = OrderRepository()

        btnClose.setOnClickListener { finish() }
    }

    private fun loadOrderDetails() {
        val orderId = intent.getStringExtra("ORDER_ID")
        if (orderId == null) {
            Toast.makeText(this, "ID do pedido não encontrado", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        lifecycleScope.launch {
            try {
                val result = orderRepository.getOrderById(orderId)

                if (result.isSuccess) {
                    val order = result.getOrNull()
                    if (order != null) {
                        runOnUiThread {
                            displayOrderDetails(order)
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(this@OrderDetailsActivity, "Pedido não encontrado", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    }
                } else {
                    val error = result.exceptionOrNull()
                    runOnUiThread {
                        Toast.makeText(this@OrderDetailsActivity, "Erro: ${error?.message}", Toast.LENGTH_LONG).show()
                        finish()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@OrderDetailsActivity, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
                    finish()
                }
            }
        }
    }

    private fun displayOrderDetails(order: Order) {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val createdDate = Date(order.createdAt)
        val updatedDate = Date(order.updatedAt)

        val details = buildString {
            appendLine("🛒 DETALHES DO PEDIDO")
            appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━")
            appendLine()
            appendLine("🆔 ID: #${order.id}")
            appendLine("📅 Criado: ${dateFormat.format(createdDate)}")
            appendLine("🔄 Atualizado: ${dateFormat.format(updatedDate)}")
            appendLine("📊 Status: ${order.status.displayName}")
            appendLine()
            appendLine("👤 INFORMAÇÕES DO CLIENTE")
            appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━")
            appendLine("Nome: ${order.buyerName}")
            appendLine("Email: ${order.buyerEmail}")
            appendLine("WhatsApp: ${order.buyerWhatsApp}")
            appendLine()
            appendLine("💳 PAGAMENTO")
            appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━")
            appendLine("Método: ${order.paymentMethod}")
            appendLine()
            appendLine("🛍️ ITENS DO PEDIDO")
            appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━")

            order.items.forEachIndexed { index, item ->
                appendLine("${index + 1}. ${item.productName}")
                appendLine("   Quantidade: ${item.quantity}")
                appendLine("   Preço unitário: R$ ${String.format("%.2f", item.unitPrice)}")
                appendLine("   Subtotal: R$ ${String.format("%.2f", item.totalPrice)}")
                appendLine()
            }

            appendLine("💰 RESUMO FINANCEIRO")
            appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━")
            appendLine("Total dos itens: R$ ${String.format("%.2f", order.items.sumOf { it.totalPrice })}")
            appendLine("TOTAL GERAL: R$ ${String.format("%.2f", order.totalAmount)}")
            appendLine()

            if (order.notes.isNotEmpty()) {
                appendLine("📝 OBSERVAÇÕES")
                appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━")
                appendLine(order.notes)
                appendLine()
            }

            appendLine("📍 INFORMAÇÕES DE RETIRADA")
            appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━")
            appendLine("Local: UNASP Store")
            appendLine("Horário: Segunda à Sexta, 8h às 17h")
            appendLine("Pagamento: ${order.paymentMethod} (na retirada)")
        }

        txtOrderDetails.text = details
    }
}
