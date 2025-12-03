package com.unasp.unaspmarketplace

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import com.unasp.unaspmarketplace.models.Order
import com.unasp.unaspmarketplace.models.OrderItem
import com.unasp.unaspmarketplace.utils.CartManager
import com.unasp.unaspmarketplace.utils.WhatsAppManager
import com.unasp.unaspmarketplace.utils.UserUtils
import kotlinx.coroutines.launch

class PaymentActivity : AppCompatActivity() {

    private lateinit var txtCustomerNameValue: TextView
    private lateinit var txtWhatsappValue: TextView
    private lateinit var txtPaymentSelected: TextView

    private lateinit var rgPaymentMethods: RadioGroup
    private lateinit var rbDebit: RadioButton
    private lateinit var rbCredit: RadioButton
    private lateinit var rbPix: RadioButton
    private lateinit var rbCash: RadioButton
    private lateinit var txtOrderSummary: TextView
    private lateinit var txtTotal: TextView
    private lateinit var btnConfirmPayment: Button

    private lateinit var btnEditCustomer: Button
    private lateinit var btnEditPayment: Button

    private lateinit var edtPickupLocation: TextInputEditText

    private var totalAmount: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.payment_activity)

        val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.appbar_payment)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        // Receber o total do carrinho
        totalAmount = intent.getDoubleExtra("totalAmount", 0.0)

        initViews()
        setupButtons()
        updateTotal()
        setupPaymentSelection()
        loadUserData()
    }

    private fun initViews() {
        txtCustomerNameValue = findViewById(R.id.txtCustomerNameValue)
        txtWhatsappValue = findViewById(R.id.txtWhatsappValue)
        txtPaymentSelected = findViewById(R.id.txtPaymentSelected)

        rgPaymentMethods = findViewById(R.id.rgPaymentMethods)
        rbDebit = findViewById(R.id.rbDebit)
        rbCredit = findViewById(R.id.rbCredit)
        rbPix = findViewById(R.id.rbPix)
        rbCash = findViewById(R.id.rbCash)

        txtTotal = findViewById(R.id.txtTotal)
        btnConfirmPayment = findViewById(R.id.btnConfirmPayment)
        txtOrderSummary = findViewById(R.id.txtOrderSummary)

        btnEditCustomer = findViewById(R.id.btnEditCustomer)
        btnEditPayment = findViewById(R.id.btnEditPayment)

        edtPickupLocation = findViewById(R.id.edtPickupLocation)
        edtPickupLocation.addTextChangedListener { updateButtonState() } // atualizar estado quando o usuário digitar o local

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
                    if (user.name.isNotBlank()) {  // Preencher nome se estiver disponível
                        txtCustomerNameValue?.text = user.name
                    }

                    if (user.whatsappNumber.isNotBlank()) { // Preencher WhatsApp se estiver disponível
                        txtWhatsappValue?.text = user.whatsappNumber
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
        btnEditCustomer.setOnClickListener {
            showEditCustomerDialog()
        }

        btnEditPayment.setOnClickListener {
            // alterna visibilidade do RadioGroup
            rgPaymentMethods.visibility = if (rgPaymentMethods.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }
    }

    private fun setupPaymentSelection() {
        rgPaymentMethods.setOnCheckedChangeListener { _, checkedId ->
            val method = when (checkedId) {
                R.id.rbDebit -> "Cartão de Débito"
                R.id.rbCredit -> "Cartão de Crédito"
                R.id.rbPix -> "PIX"
                R.id.rbCash -> "Dinheiro"
                else -> ""
            }
            if (method.isNotEmpty()) {
                txtPaymentSelected.text = method
                // opcional: esconder opções depois de escolher
                rgPaymentMethods.visibility = View.GONE
            }
            updateButtonState()
        }
        // inicializa texto do método a partir do radio checked (se houver)
        val initial = when (rgPaymentMethods.checkedRadioButtonId) {
            R.id.rbDebit -> "Cartão de Débito"
            R.id.rbCredit -> "Cartão de Crédito"
            R.id.rbPix -> "PIX"
            R.id.rbCash -> "Dinheiro"
            else -> txtPaymentSelected.text.toString()
        }
        txtPaymentSelected.text = initial
    }

    private fun showEditCustomerDialog() {
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 16, 48, 0)
        }

        val nameInput = EditText(this).apply {
            hint = "Nome completo"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_WORDS
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setText(txtCustomerNameValue?.text ?: "")
        }

        val whatsappInput = EditText(this).apply {
            hint = "WhatsApp"
            inputType = InputType.TYPE_CLASS_PHONE
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                topMargin = 8
            }
            setText(txtWhatsappValue?.text ?: "")
        }

        container.addView(nameInput)
        container.addView(whatsappInput)

        AlertDialog.Builder(this)
            .setTitle("Alterar dados do cliente")
            .setView(container)
            .setPositiveButton("Salvar") { dialog, _ ->
                val newName = nameInput.text?.toString()?.trim().orEmpty()
                val newWhats = whatsappInput.text?.toString()?.trim().orEmpty()
                if (newName.isNotEmpty()) {
                    txtCustomerNameValue?.text = newName
                }
                txtWhatsappValue?.text = newWhats
                updateButtonState()
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun updateButtonState() {
        val hasName = txtCustomerNameValue?.text?.toString()?.trim()?.isNotEmpty() == true
        val hasPaymentMethod = getSelectedPaymentMethod().isNotEmpty()
        btnConfirmPayment.isEnabled = hasName && hasPaymentMethod
        btnConfirmPayment.alpha = if (btnConfirmPayment.isEnabled) 1.0f else 0.5f
    }

    private fun updateTotal() {
        txtTotal.text = "Total: R$ %.2f".format(totalAmount)
    }

    private fun generateOrder() {
        val customerName = txtCustomerNameValue?.text?.toString()?.trim()
        val whatsappNumber = txtWhatsappValue?.text?.toString()?.trim()
        val selectedPaymentMethod = getSelectedPaymentMethod()
        val pickupLocation = edtPickupLocation.text?.toString()?.trim()

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

        if (pickupLocation.isNullOrEmpty()) {
            Toast.makeText(this, "Informe o local de retirada", Toast.LENGTH_SHORT).show()
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
        goToOrderPreview(order, whatsappNumber, pickupLocation)
    }

    private fun getSelectedPaymentMethod(): String {
        return when (rgPaymentMethods.checkedRadioButtonId) {
            R.id.rbDebit -> "Cartão de Débito"
            R.id.rbCredit -> "Cartão de Crédito"
            R.id.rbPix -> "PIX"
            R.id.rbCash -> "Dinheiro"
            else -> {
                // fallback para o texto exibido (por exemplo PIX padrão)
                txtPaymentSelected.text?.toString()?.takeIf { it.isNotBlank() } ?: ""
            }
        }
    }

    private fun goToOrderPreview(order: Order, customerWhatsApp: String?, pickupLocation: String?) {
        val intent = Intent(this, OrderPreviewActivity::class.java)
        intent.putExtra(OrderPreviewActivity.EXTRA_ORDER_ID, order.id)
        intent.putExtra(OrderPreviewActivity.EXTRA_CUSTOMER_NAME, order.customerName)
        intent.putExtra(OrderPreviewActivity.EXTRA_PAYMENT_METHOD, order.paymentMethod)

        if (!customerWhatsApp.isNullOrBlank()) {
            intent.putExtra(OrderPreviewActivity.EXTRA_CUSTOMER_WHATSAPP, customerWhatsApp)
        }

        if (!pickupLocation.isNullOrBlank()) {
            intent.putExtra("EXTRA_PICKUP_LOCATION", pickupLocation)
        }

        startActivity(intent)
        // finish()
    }
}


