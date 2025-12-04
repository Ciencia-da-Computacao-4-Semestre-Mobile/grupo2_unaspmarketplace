package com.unasp.unaspmarketplace

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.firestore.FirebaseFirestore
import com.unasp.unaspmarketplace.models.Order
import com.unasp.unaspmarketplace.models.OrderItem
import com.unasp.unaspmarketplace.models.User
import com.unasp.unaspmarketplace.utils.CartManager
import com.unasp.unaspmarketplace.utils.WhatsAppHelper
import com.unasp.unaspmarketplace.utils.UserUtils
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.or

class OrderPreviewActivity : AppCompatActivity() {

    private lateinit var txtOrderPreview: TextView
    private lateinit var txtCountdown: TextView
    private lateinit var btnSendNow: Button
    private lateinit var btnCancel: Button

    private var countDownTimer: CountDownTimer? = null
    private var order: Order? = null
    private var customerWhatsApp: String? = null

    companion object {
        const val EXTRA_ORDER_ID = "order_id"
        const val EXTRA_CUSTOMER_NAME = "customer_name"
        const val EXTRA_CUSTOMER_WHATSAPP = "customer_whatsapp"
        const val EXTRA_PAYMENT_METHOD = "payment_method"
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
        val orderId = intent.getStringExtra(EXTRA_ORDER_ID) ?: return
        val customerName = intent.getStringExtra(EXTRA_CUSTOMER_NAME) ?: return
        val paymentMethod = intent.getStringExtra(EXTRA_PAYMENT_METHOD) ?: return
        customerWhatsApp = intent.getStringExtra(EXTRA_CUSTOMER_WHATSAPP)

        // Obter itens do carrinho
        val cartItems = CartManager.getCartItems()
        if (cartItems.isEmpty()) {
            finish()
            return
        }

        // Converter itens do carrinho para itens do pedido
        val orderItems = cartItems.map { cartItem ->
            OrderItem(
                productId = cartItem.product.id,
                productName = cartItem.product.name,
                quantity = cartItem.quantity,
                unitPrice = cartItem.product.price
            )
        }

        // Criar o pedido
        order = Order(
            id = orderId,
            buyerId = UserUtils.getCurrentUserId() ?: "",
            sellerId = "", // Multi-seller, will be handled per item
            sellerName = "",
            buyerName = customerName,
            buyerEmail = "",
            buyerWhatsApp = customerWhatsApp ?: "",
            items = orderItems,
            totalAmount = orderItems.sumOf { it.totalPrice },
            paymentMethod = paymentMethod,
            status = "PENDING",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        // Mostrar preview do pedido
        displayOrderPreview()
    }

    private fun displayOrderPreview() {
        order?.let { order ->
            val orderMessage = formatOrderMessage(order, customerWhatsApp)
            txtOrderPreview.text = orderMessage
        }
    }

    private fun formatOrderMessage(order: Order, customerWhatsApp: String?): String {
        val itemsList = order.items.joinToString("\n") {
            "• ${it.productName} (Qtd: ${it.quantity}) - R$ ${String.format("%.2f", it.totalPrice)}"
        }

        val whatsappInfo = if (!customerWhatsApp.isNullOrBlank()) {
            "\n📱 WhatsApp do Cliente: $customerWhatsApp"
        } else ""

        return """
🛒 NOVO PEDIDO - UNASP MARKETPLACE

📋 ID do Pedido: ${order.id}
👤 Nome: ${order.buyerName}$whatsappInfo
📅 Data da Compra: ${java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date(order.createdAt))}
💳 Forma de Pagamento: ${order.paymentMethod} (na retirada)

🛍️ Itens Comprados:
$itemsList

💰 Total: R$ ${String.format("%.2f", order.totalAmount)}

Por favor, confirme o recebimento deste pedido.
    """.trimIndent()
    }


    private fun startCountdown() {
        countDownTimer = object : CountDownTimer((COUNTDOWN_SECONDS * 1000).toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsLeft = millisUntilFinished / 1000
                txtCountdown.text = getString(R.string.redirecting_in, secondsLeft.toInt())
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
        order?.let { order ->
            // Mostrar feedback para o usuário
            Toast.makeText(this, "Processando pedido...", Toast.LENGTH_SHORT).show()

            // Salvar dados do usuário se necessário
            saveUserDataIfNeeded(order.buyerName, customerWhatsApp)

            // Buscar WhatsApps dos vendedores e enviar mensagens
            lifecycleScope.launch {
                try {
                    sendMessagesToSellers(order)

                    // Limpar o carrinho apenas após sucesso
                    CartManager.clearCart()

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
👤 Cliente: ${order.buyerName}$whatsappInfo
📅 Data: ${java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date(order.createdAt))}
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
            putExtra("customer_name", order.buyerName)
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