package com.unasp.unaspmarketplace

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.lifecycleScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.unasp.unaspmarketplace.OrderPreviewActivity.Companion.COUNTDOWN_SECONDS
import com.unasp.unaspmarketplace.models.Order
import com.unasp.unaspmarketplace.models.OrderItem
import com.unasp.unaspmarketplace.models.User
import com.unasp.unaspmarketplace.utils.CartManager
import com.unasp.unaspmarketplace.utils.WhatsAppHelper
import com.unasp.unaspmarketplace.utils.UserUtils
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class OrderPreviewActivity : AppCompatActivity() {

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
        generateOrder()
    }

    private fun generateOrder() {
        // Monta o Order com base nos extras e itens do carrinho
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

        // Exibe o bottom sheet com countdown e ações
        CheckoutBottomSheet(
            countdownSeconds = COUNTDOWN_SECONDS,
            onSendNow = { sendOrder() },
            onCancel = { finish() }
        ).show(supportFragmentManager, "CheckoutBottomSheet")
    }

    private fun sendOrder() {
        order?.let { order ->
            // Mostrar feedback para o usuário
            Toast.makeText(this, "Processando pedido...", Toast.LENGTH_SHORT).show()

            // Salvar dados do usuário se necessário
            saveUserDataIfNeeded(order.customerName, customerWhatsApp)

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

    class CheckoutBottomSheet(
        private val countdownSeconds: Int = 8,
        private val onSendNow: () -> Unit,
        private val onCancel: () -> Unit
    ) : BottomSheetDialogFragment() {

        private var countDownTimer: CountDownTimer? = null
        private lateinit var txtCountdown: TextView

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            isCancelable = true // permite cancelar com toque fora / back
        }

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View {
            return inflater.inflate(R.layout.order_preview_activity, container, false)
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)

            txtCountdown = view.findViewById<TextView>(R.id.txtCountdown)
            val btnSendNow = view.findViewById<Button>(R.id.btnSendNow)
            val btnCancel = view.findViewById<Button>(R.id.btnCancel)

            btnSendNow.setOnClickListener {
                onSendNow()
                dismiss()
            }

            btnCancel.setOnClickListener {
                onCancel()
                dismiss()
            }

            dialog?.setCanceledOnTouchOutside(true)
            startCountdown()
        }

        override fun onCancel(dialog: DialogInterface) {
            super.onCancel(dialog)
            onCancel()
        }

        override fun onStart() {
            super.onStart()
            val bottomSheet =
                dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let {
                val behavior = BottomSheetBehavior.from(it)
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.isDraggable = true
                behavior.isHideable = true
            }
        }

        private fun startCountdown() {
            countDownTimer = object : CountDownTimer((countdownSeconds * 1000).toLong(), 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    val secondsLeft = (millisUntilFinished / 1000).toInt()
                    txtCountdown.text = getString(R.string.redirecting_in, secondsLeft)
                }

                override fun onFinish() {
                    onSendNow()
                    dismiss()
                }
            }.start()
        }

        private fun cancelCountdown() {
            countDownTimer?.cancel()
            countDownTimer = null
        }

        override fun onDestroyView() {
            super.onDestroyView()
            cancelCountdown()
        }
    }
}
