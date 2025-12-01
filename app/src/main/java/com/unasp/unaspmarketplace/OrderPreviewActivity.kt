package com.unasp.unaspmarketplace

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
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
        val intent = Intent(this, OrderSuccessActivity::class.java)
        startActivity(intent)
        finish()
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
