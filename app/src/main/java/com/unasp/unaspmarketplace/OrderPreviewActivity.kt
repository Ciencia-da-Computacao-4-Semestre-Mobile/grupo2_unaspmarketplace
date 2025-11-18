package com.unasp.unaspmarketplace

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.unasp.unaspmarketplace.models.Order
import com.unasp.unaspmarketplace.models.OrderItem
import com.unasp.unaspmarketplace.utils.CartManager
import com.unasp.unaspmarketplace.utils.WhatsAppHelper
import com.unasp.unaspmarketplace.utils.UserUtils
import kotlinx.coroutines.launch

class OrderPreviewActivity : AppCompatActivity() {

    private lateinit var txtOrderPreview: TextView
    private lateinit var txtCountdown: TextView
    private lateinit var btnSendNow: Button
    private lateinit var btnTestWhatsApp: Button
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

        initViews()
        setupButtons()
        generateOrder()
        startCountdown()
    }

    private fun initViews() {
        txtOrderPreview = findViewById(R.id.txtOrderPreview)
        txtCountdown = findViewById(R.id.txtCountdown)
        btnSendNow = findViewById(R.id.btnSendNow)
        btnTestWhatsApp = findViewById(R.id.btnTestWhatsApp)
        btnCancel = findViewById(R.id.btnCancel)
    }

    private fun setupButtons() {
        btnSendNow.setOnClickListener {
            cancelCountdown()
            sendOrder()
        }

        btnTestWhatsApp.setOnClickListener {
            testWhatsApp()
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
            userId = UserUtils.getCurrentUserId() ?: "",
            customerName = customerName,
            items = orderItems,
            orderDate = Order.getCurrentDate(),
            paymentMethod = paymentMethod,
            totalAmount = orderItems.sumOf { it.totalPrice }
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
        val totalAmount = order.items.sumOf { it.totalPrice }

        val whatsappInfo = if (!customerWhatsApp.isNullOrBlank()) {
            "\n📱 WhatsApp do Cliente: $customerWhatsApp"
        } else ""

        return """
🛒 NOVO PEDIDO - UNASP MARKETPLACE

📋 ID do Pedido: ${order.id}
👤 Nome: ${order.customerName}$whatsappInfo
📍 Local de Retirada: ${order.pickupLocation}
📅 Data da Compra: ${order.orderDate}
💳 Forma de Pagamento: ${order.paymentMethod} (na retirada)

🛍️ Itens Comprados:
$itemsList

💰 Total: R$ ${String.format("%.2f", totalAmount)}

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
            Toast.makeText(this, "Abrindo WhatsApp...", Toast.LENGTH_SHORT).show()

            // Salvar dados do usuário se necessário
            saveUserDataIfNeeded(order.customerName, customerWhatsApp)

            // Limpar o carrinho
            CartManager.clearCart()

            // Enviar via WhatsApp
            val orderMessage = formatOrderMessage(order, customerWhatsApp)
            WhatsAppHelper.sendMessage(this, orderMessage)

            // Dar um pequeno delay antes de ir para tela de sucesso
            // para permitir que o WhatsApp abra
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                goToSuccess(order)
            }, 1500) // 1.5 segundos de delay
        }
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
        val intent = Intent(this, OrderSuccessActivity::class.java)
        intent.putExtra("order_id", order.id)
        intent.putExtra("customer_name", order.customerName)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    private fun testWhatsApp() {
        val testMessage = "🧪 TESTE - UNASP MARKETPLACE\n\nEste é um teste de conectividade.\nSe você recebeu esta mensagem, a integração está funcionando!"

        Toast.makeText(this, "Enviando mensagem de teste...", Toast.LENGTH_SHORT).show()
        WhatsAppHelper.sendMessage(this, testMessage)
    }

    override fun onDestroy() {
        super.onDestroy()
        cancelCountdown()
    }

    override fun onBackPressed() {
        cancelCountdown()
        super.onBackPressed()
    }
}
