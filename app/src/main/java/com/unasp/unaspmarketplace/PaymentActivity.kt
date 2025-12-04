package com.unasp.unaspmarketplace

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import com.unasp.unaspmarketplace.models.Order
import com.unasp.unaspmarketplace.models.OrderItem
import com.unasp.unaspmarketplace.models.OrderStatus
import com.unasp.unaspmarketplace.repository.OrderRepository
import com.unasp.unaspmarketplace.utils.CartManager
import com.unasp.unaspmarketplace.utils.UserUtils
import kotlinx.coroutines.launch

class PaymentActivity : AppCompatActivity() {

    private lateinit var edtCustomerName: TextInputEditText
    private lateinit var edtWhatsappNumber: TextInputEditText
    private lateinit var rgPaymentMethods: RadioGroup
    private lateinit var rbDebit: RadioButton
    private lateinit var rbCredit: RadioButton
    private lateinit var rbPix: RadioButton
    private lateinit var rbCash: RadioButton
    private lateinit var txtTotal: TextView
    private lateinit var btnConfirmPayment: Button
    private lateinit var orderRepository: OrderRepository

    private var totalAmount: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize repository
        orderRepository = OrderRepository()

        setContentView(R.layout.payment_activity)

        // Receber o total do carrinho
        totalAmount = intent.getDoubleExtra("totalAmount", 0.0)

        initViews()
        setupButtons()
        updateTotal()
        setupPaymentSelection()
        loadUserData()
    }

    private fun initViews() {
        edtCustomerName = findViewById(R.id.edtCustomerName)
        edtWhatsappNumber = findViewById(R.id.edtWhatsappNumber)
        rgPaymentMethods = findViewById(R.id.rgPaymentMethods)
        rbDebit = findViewById(R.id.rbDebit)
        rbCredit = findViewById(R.id.rbCredit)
        rbPix = findViewById(R.id.rbPix)
        rbCash = findViewById(R.id.rbCash)
        txtTotal = findViewById(R.id.txtTotal)
        btnConfirmPayment = findViewById(R.id.btnConfirmPayment)

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        btnBack.setOnClickListener { finish() }
    }

    private fun loadUserData() {
        lifecycleScope.launch {
            try {
                val currentUser = UserUtils.getCurrentUser()
                currentUser?.let { user ->
                    // Preencher nome se estiver disponível
                    if (user.name.isNotBlank()) {
                        edtCustomerName.setText(user.name)
                    }

                    // Preencher WhatsApp se estiver disponível
                    if (user.whatsappNumber.isNotBlank()) {
                        edtWhatsappNumber.setText(user.whatsappNumber)
                    }

                    // Atualizar estado do botão após carregar dados
                    updateButtonState()
                }
            } catch (e: Exception) {
                // Se houver erro ao carregar dados do usuário, não fazer nada
                // O usuário pode preencher manualmente
            }
        }
    }

    private fun setupButtons() {
        btnConfirmPayment.setOnClickListener {
            generateOrder()
        }
    }

    private fun setupPaymentSelection() {
        rgPaymentMethods.setOnCheckedChangeListener { _, _ ->
            updateButtonState()
        }

        // Listeners para os campos de texto
        edtCustomerName.setOnFocusChangeListener { _, _ ->
            updateButtonState()
        }

        edtWhatsappNumber.setOnFocusChangeListener { _, _ ->
            updateButtonState()
        }
    }

    private fun updateButtonState() {
        val hasName = edtCustomerName.text?.toString()?.trim()?.isNotEmpty() == true
        val hasWhatsApp = edtWhatsappNumber.text?.toString()?.trim()?.isNotEmpty() == true
        val hasPaymentMethod = rgPaymentMethods.checkedRadioButtonId != -1

        btnConfirmPayment.isEnabled = hasName && hasWhatsApp && hasPaymentMethod
        btnConfirmPayment.alpha = if (hasName && hasWhatsApp && hasPaymentMethod) 1.0f else 0.5f
    }

    private fun updateTotal() {
        txtTotal.text = "Total: R$ %.2f".format(totalAmount)
    }

    private fun generateOrder() {
        val customerName = edtCustomerName.text?.toString()?.trim()
        val whatsappNumber = edtWhatsappNumber.text?.toString()?.trim()
        val selectedPaymentMethod = getSelectedPaymentMethod()

        // Validações
        if (customerName.isNullOrEmpty()) {
            Toast.makeText(this, "Digite seu nome completo", Toast.LENGTH_SHORT).show()
            return
        }

        if (whatsappNumber.isNullOrEmpty()) {
            Toast.makeText(this, "Digite seu número de WhatsApp", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedPaymentMethod.isEmpty()) {
            Toast.makeText(this, "Selecione uma forma de pagamento", Toast.LENGTH_SHORT).show()
            return
        }

        // Obter itens do carrinho
        val cartItems = CartManager.getCartItems()
        if (cartItems.isEmpty()) {
            Toast.makeText(this, "Carrinho vazio!", Toast.LENGTH_SHORT).show()
            return
        }

        btnConfirmPayment.isEnabled = false
        btnConfirmPayment.text = "Processando..."

        lifecycleScope.launch {
            try {
                    android.util.Log.d("PaymentActivity", "Iniciando criação do pedido...")

                val currentUser = UserUtils.getCurrentUser()
                val buyerId = currentUser?.id ?: ""
                val buyerEmail = currentUser?.email ?: ""

                android.util.Log.d("PaymentActivity", "Usuário atual: $buyerId - $buyerEmail")

                if (buyerId.isEmpty()) {
                    runOnUiThread {
                        btnConfirmPayment.isEnabled = true
                        btnConfirmPayment.text = "Gerar Pedido"
                        Toast.makeText(this@PaymentActivity, "❌ Erro: Usuário não logado", Toast.LENGTH_LONG).show()
                    }
                    return@launch
                }

                // Converter itens do carrinho para itens do pedido
                val orderItems = cartItems.map { cartItem ->
                    android.util.Log.d("PaymentActivity", "Item do carrinho: ${cartItem.product.name} - Vendedor: ${cartItem.product.sellerId}")
                    OrderItem(
                        productId = cartItem.product.id,
                        productName = cartItem.product.name,
                        productImage = if (cartItem.product.imageUrls.isNotEmpty()) cartItem.product.imageUrls[0] else "",
                        quantity = cartItem.quantity,
                        unitPrice = cartItem.product.price,
                        totalPrice = cartItem.quantity * cartItem.product.price
                    )
                }

                android.util.Log.d("PaymentActivity", "Convertidos ${orderItems.size} itens do pedido")

                // Verificar se temos vendedor válido
                val sellerId = cartItems[0].product.sellerId
                android.util.Log.d("PaymentActivity", "Vendedor ID: $sellerId")

                if (sellerId.isEmpty()) {
                    runOnUiThread {
                        btnConfirmPayment.isEnabled = true
                        btnConfirmPayment.text = "Gerar Pedido"
                        Toast.makeText(this@PaymentActivity, "❌ Erro: Produto sem vendedor definido", Toast.LENGTH_LONG).show()
                    }
                    return@launch
                }

                // Criar mensagem do WhatsApp
                val whatsAppMessage = createWhatsAppMessage(customerName, orderItems, selectedPaymentMethod)

                // Criar o pedido
                val order = Order(
                    buyerId = buyerId,
                    sellerId = sellerId,
                    sellerName = "", // Será preenchido depois se necessário
                    buyerName = customerName,
                    buyerEmail = buyerEmail,
                    buyerWhatsApp = whatsappNumber,
                    items = orderItems,
                    totalAmount = orderItems.sumOf { it.totalPrice },
                    paymentMethod = selectedPaymentMethod,
                    status = OrderStatus.PENDING.name,
                    whatsAppMessage = whatsAppMessage
                )

                android.util.Log.d("PaymentActivity", "Pedido criado: ${order}")
                android.util.Log.d("PaymentActivity", "Total do pedido: ${order.totalAmount}")

                // Salvar pedido no Firebase
                android.util.Log.d("PaymentActivity", "Salvando pedido no Firebase...")
                val result = orderRepository.createOrder(order)
                android.util.Log.d("PaymentActivity", "Resultado da criação: ${result.isSuccess}")

                runOnUiThread {
                    btnConfirmPayment.isEnabled = true
                    btnConfirmPayment.text = "Gerar Pedido"

                    if (result.isSuccess) {
                        val orderId = result.getOrNull()!!
                        android.util.Log.d("PaymentActivity", "Pedido criado com sucesso! ID: $orderId")
                        Toast.makeText(this@PaymentActivity, "✅ Pedido criado com sucesso!", Toast.LENGTH_SHORT).show()

                        // Limpar carrinho após sucesso
                        CartManager.clearCart()

                        // Ir para tela de preview com o pedido criado
                        goToOrderPreview(order.copy(id = orderId), whatsappNumber)
                    } else {
                        val error = result.exceptionOrNull()
                        android.util.Log.e("PaymentActivity", "Erro ao criar pedido: ${error?.message}", error)
                        Toast.makeText(this@PaymentActivity, "❌ Erro ao criar pedido: ${error?.message}", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("PaymentActivity", "Exceção durante criação do pedido: ${e.message}", e)
                runOnUiThread {
                    btnConfirmPayment.isEnabled = true
                    btnConfirmPayment.text = "Gerar Pedido"
                    Toast.makeText(this@PaymentActivity, "❌ Erro: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun createWhatsAppMessage(customerName: String, orderItems: List<OrderItem>, paymentMethod: String): String {
        val message = StringBuilder()
        message.appendLine("🛒 *NOVO PEDIDO - UNASP MARKETPLACE*")
        message.appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━")
        message.appendLine()
        message.appendLine("👤 *Cliente:* $customerName")
        message.appendLine("📞 *WhatsApp:* ${edtWhatsappNumber.text}")
        message.appendLine()
        message.appendLine("🛍️ *ITENS DO PEDIDO:*")

        var total = 0.0
        orderItems.forEach { item ->
            message.appendLine("• ${item.quantity}x ${item.productName}")
            message.appendLine("   💰 R$ ${String.format("%.2f", item.unitPrice)} cada = R$ ${String.format("%.2f", item.totalPrice)}")
            total += item.totalPrice
        }

        message.appendLine()
        message.appendLine("💳 *Pagamento:* $paymentMethod (na retirada)")
        message.appendLine("💰 *TOTAL:* R$ ${String.format("%.2f", total)}")
        message.appendLine()
        message.appendLine("📍 *Retirada:* UNASP Store")
        message.appendLine("🕒 *Horário:* Segunda à Sexta, 8h às 17h")
        message.appendLine()
        message.appendLine("✅ *Confirme este pedido para prosseguir*")

        return message.toString()
    }

    private fun getSelectedPaymentMethod(): String {
        return when (rgPaymentMethods.checkedRadioButtonId) {
            R.id.rbDebit -> "Cartão de Débito"
            R.id.rbCredit -> "Cartão de Crédito"
            R.id.rbPix -> "PIX"
            R.id.rbCash -> "Dinheiro"
            else -> ""
        }
    }

    private fun showOrderConfirmation(order: Order, customerWhatsApp: String?) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirmar Pedido")

        val whatsappInfo = if (!customerWhatsApp.isNullOrBlank()) {
            "\nWhatsApp: $customerWhatsApp"
        } else ""

        builder.setMessage(
            "Nome: ${order.buyerName}$whatsappInfo\n" +
            "Itens: ${order.items.size} produto(s)\n" +
            "Total: R$ ${String.format("%.2f", order.totalAmount)}\n" +
            "Pagamento: ${order.paymentMethod} (na retirada)\n" +
            "Local: UNASP Store\n\n" +
            "Deseja visualizar o pedido antes de enviar?"
        )

        builder.setPositiveButton("Visualizar") { _, _ ->
            goToOrderPreview(order, customerWhatsApp)
        }

        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.dismiss()
        }

        builder.show()
    }

    private fun goToOrderPreview(order: Order, customerWhatsApp: String?) {
        val intent = Intent(this, OrderPreviewActivity::class.java)
        intent.putExtra("ORDER_ID", order.id)
        intent.putExtra("CUSTOMER_NAME", order.buyerName)
        intent.putExtra("PAYMENT_METHOD", order.paymentMethod)
        intent.putExtra("TOTAL_AMOUNT", order.totalAmount)
        intent.putExtra("WHATSAPP_MESSAGE", order.whatsAppMessage)

        if (!customerWhatsApp.isNullOrBlank()) {
            intent.putExtra("CUSTOMER_WHATSAPP", customerWhatsApp)
        }

        startActivity(intent)
        finish()
    }
}
