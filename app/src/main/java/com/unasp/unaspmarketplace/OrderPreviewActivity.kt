package com.unasp.unaspmarketplace

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import com.unasp.unaspmarketplace.utils.CartManager

import com.unasp.unaspmarketplace.utils.WhatsAppManager


class OrderPreviewActivity : AppCompatActivity() {

    private lateinit var txtOrderPreview: TextView
    private lateinit var txtCountdown: TextView
    private lateinit var btnSendNow: Button
    private lateinit var btnCancel: Button

    private var countDownTimer: CountDownTimer? = null

    companion object {
        const val COUNTDOWN_SECONDS = 8 // 8 segundos para o usuário ler
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.order_preview_activity)

        val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.appbar_order_preview)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        initViews()
        setupButtons()
        setupBackPressedHandler()
        generateOrder()
        startCountdown()
    }

    private fun initViews() {
        txtOrderPreview = findViewById(R.id.txtOrderPreview)
        txtCountdown = findViewById(R.id.txtCountdown)
        btnSendNow = findViewById(R.id.btnSendNow)
        btnCancel = findViewById(R.id.btnCancel)
    }

    private fun setupButtons() {
        btnSendNow.setOnClickListener {
            cancelCountdown()
            sendOrder()
        }

        btnCancel.setOnClickListener {
            cancelCountdown()
            finish()
        }
    }

    private fun generateOrder() {
        val orderId = intent.getStringExtra("ORDER_ID") ?: return
        val customerName = intent.getStringExtra("CUSTOMER_NAME") ?: return
        val paymentMethod = intent.getStringExtra("PAYMENT_METHOD") ?: return
        val totalAmount = intent.getDoubleExtra("TOTAL_AMOUNT", 0.0)
        val whatsAppMessage = intent.getStringExtra("WHATSAPP_MESSAGE") ?: ""

        // Exibir a mensagem do pedido
        txtOrderPreview.text = whatsAppMessage

        Toast.makeText(this, "✅ Pedido #${orderId.take(8)} criado com sucesso!", Toast.LENGTH_SHORT).show()
    }

    private fun startCountdown() {
        countDownTimer = object : CountDownTimer((COUNTDOWN_SECONDS * 1000).toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsLeft = millisUntilFinished / 1000
                txtCountdown.text = "Redirecionando em $secondsLeft segundos..."
            }

            override fun onFinish() {
                sendOrder()
            }
        }.start()
    }

    private fun cancelCountdown() {
        countDownTimer?.cancel()
        countDownTimer = null
    }

    private fun sendOrder() {
        val whatsAppMessage = intent.getStringExtra("WHATSAPP_MESSAGE") ?: ""

        if (whatsAppMessage.isNotEmpty()) {
            // Enviar para WhatsApp
            WhatsAppManager.sendOrderToWhatsApp(this, whatsAppMessage)

            // Limpar carrinho após enviar para WhatsApp
            CartManager.clearCart()

            // Ir para tela de sucesso
            goToSuccess()
        } else {
            Toast.makeText(this, "Erro: mensagem não encontrada", Toast.LENGTH_SHORT).show()
        }
    }

    private fun goToSuccess() {
        val intent = Intent(this, OrderSuccessActivity::class.java)
                    // Ir para tela de sucesso
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        goToSuccess(order)
                    }, 1000) // 1 segundo de delay

                } catch (e: Exception) {
                    Toast.makeText(
                        this@OrderPreviewActivity,
                        "Erro ao processar pedido: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private suspend fun sendMessagesToSellers(order: Order) {
        // Agrupar itens por vendedor
        val itemsBySeller = mutableMapOf<String, MutableList<OrderItem>>()

        // Buscar dados dos produtos para obter sellerId
        val cartItems = CartManager.getCartItems()
        for (cartItem in cartItems) {
            val sellerId = cartItem.product.sellerId
            if (sellerId.isNotBlank()) {
                if (!itemsBySeller.containsKey(sellerId)) {
                    itemsBySeller[sellerId] = mutableListOf()
                }

                // Encontrar o item correspondente no pedido
                val orderItem = order.items.find { it.productId == cartItem.product.id }
                orderItem?.let {
                    itemsBySeller[sellerId]?.add(it)
                }
            }
        }

        // Para cada vendedor, buscar WhatsApp e enviar mensagem
        for ((sellerId, items) in itemsBySeller) {
            try {
                val sellerWhatsApp = getSellerWhatsApp(sellerId)
                if (sellerWhatsApp.isNotBlank()) {
                    val sellerMessage = formatSellerMessage(order, items, customerWhatsApp)

                    // Enviar mensagem para o vendedor
                    runOnUiThread {
                        WhatsAppHelper.sendMessage(
                            this@OrderPreviewActivity,
                            sellerMessage,
                            sellerWhatsApp
                        )
                    }

                    // Pequeno delay entre mensagens
                    kotlinx.coroutines.delay(1000)
                }
            } catch (e: Exception) {
                // Log do erro mas continue para outros vendedores
                android.util.Log.e("OrderPreview", "Erro ao enviar para vendedor $sellerId", e)
            }
        }
    }

    private suspend fun getSellerWhatsApp(sellerId: String): String {
        return try {
            val userDoc = FirebaseFirestore.getInstance()
                .collection("users")
                .document(sellerId)
                .get()
                .await()

            val user = userDoc.toObject(User::class.java)
            user?.whatsappNumber ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    private fun formatSellerMessage(
        order: Order,
        items: List<OrderItem>,
        customerWhatsApp: String?
    ): String {
        val itemsList = items.joinToString("\n") {
            "• ${it.productName} (Qtd: ${it.quantity}) - R$ ${String.format("%.2f", it.totalPrice)}"
        }
        val subtotal = items.sumOf { it.totalPrice }

        val whatsappInfo = if (!customerWhatsApp.isNullOrBlank()) {
            "\n📱 WhatsApp do Cliente: $customerWhatsApp"
        } else ""

        return """
🛒 NOVO PEDIDO - UNASP MARKETPLACE

📋 ID do Pedido: ${order.id}
👤 Cliente: ${order.customerName}$whatsappInfo
📅 Data: ${order.orderDate}
💳 Pagamento: ${order.paymentMethod} (na retirada)

🛍️ Seus produtos vendidos:
$itemsList

💰 Subtotal dos seus produtos: R$ ${String.format("%.2f", subtotal)}

📍 Local de retirada: Campus UNASP

⚠️ IMPORTANTE: Entre em contato com o cliente para coordenar a entrega!
        """.trimIndent()
    }

    private fun saveUserDataIfNeeded(customerName: String, whatsappNumber: String?) {
        lifecycleScope.launch {
            try {
                val currentUser = UserUtils.getCurrentUser() ?: return@launch

                var shouldUpdate = false
                var updatedUser = currentUser

                // Atualizar nome se estava vazio
                if (currentUser.name.isBlank() && customerName.isNotBlank()) {
                    updatedUser = updatedUser.copy(name = customerName)
                    shouldUpdate = true
                }

                // Atualizar WhatsApp se estava vazio e foi preenchido
                if (currentUser.whatsappNumber.isBlank() && !whatsappNumber.isNullOrBlank()) {
                    updatedUser = updatedUser.copy(whatsappNumber = whatsappNumber)
                    shouldUpdate = true
                }

                if (shouldUpdate) {
                    UserUtils.updateUser(updatedUser)
                }
            } catch (e: Exception) {
                // Se houver erro ao salvar, não interferir no fluxo do pedido
            }
        }
    }

    private fun goToSuccess(order: Order) {
        val intent = Intent(this, OrderSuccessActivity::class.java).apply {
            putExtra("order_id", order.id)
            putExtra("customer_name", order.customerName)
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
        finish()
    }



    override fun onDestroy() {
        super.onDestroy()
        cancelCountdown()
    }


    private fun setupBackPressedHandler() {
        onBackPressedDispatcher.addCallback(this) {
            cancelCountdown()
            finish()
        }
    }
}