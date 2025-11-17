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
    private var txtOrderSummary: TextView? = null
    private lateinit var txtTotal: TextView
    private lateinit var btnConfirmPayment: Button

    private var totalAmount: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        txtOrderSummary = findViewById(R.id.txtOrderSummary) as? TextView

        displayOrderSummary()
    }

    private fun displayOrderSummary() {
        val cartItems = CartManager.getCartItems()
        val summaryText = if (cartItems.isNotEmpty()) {
            cartItems.joinToString(", ") { item -> "x${item.quantity} ${item.product.name}" }
        } else {
            "Nenhum item no carrinho"
        }
        txtOrderSummary?.text = summaryText
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
        val hasPaymentMethod = rgPaymentMethods.checkedRadioButtonId != -1

        btnConfirmPayment.isEnabled = hasName && hasPaymentMethod
        btnConfirmPayment.alpha = if (hasName && hasPaymentMethod) 1.0f else 0.5f
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
        val order = Order(
            id = Order.generateOrderId(),
            userId = UserUtils.getCurrentUserId() ?: "",
            customerName = customerName,
            items = orderItems,
            orderDate = Order.getCurrentDate(),
            paymentMethod = selectedPaymentMethod,
            totalAmount = orderItems.sumOf { it.totalPrice }
        )

        // Mostrar confirmação
        goToOrderPreview(order, whatsappNumber)
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

    private fun goToOrderPreview(order: Order, customerWhatsApp: String?) {
        val intent = Intent(this, OrderPreviewActivity::class.java)
        intent.putExtra(OrderPreviewActivity.EXTRA_ORDER_ID, order.id)
        intent.putExtra(OrderPreviewActivity.EXTRA_CUSTOMER_NAME, order.customerName)
        intent.putExtra(OrderPreviewActivity.EXTRA_PAYMENT_METHOD, order.paymentMethod)

        if (!customerWhatsApp.isNullOrBlank()) {
            intent.putExtra(OrderPreviewActivity.EXTRA_CUSTOMER_WHATSAPP, customerWhatsApp)
        }

        startActivity(intent)
        // finish()
    }
}


