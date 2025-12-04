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

import com.unasp.unaspmarketplace.utils.WhatsAppManager


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
        val intent = Intent(this, OrderSuccessActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
        finish()
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
