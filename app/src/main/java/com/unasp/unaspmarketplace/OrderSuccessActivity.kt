package com.unasp.unaspmarketplace

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar

class OrderSuccessActivity : AppCompatActivity() {

    private lateinit var txtOrderId: TextView
    private lateinit var txtCustomerName: TextView
    private lateinit var btnBackToHome: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.order_success_activity)

        val toolbar = findViewById<MaterialToolbar>(R.id.appbar_success)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { navigateToHome() }

        initViews()
        setupData()
        setupButtons()
        setupBackPress()
    }

    private fun initViews() {
        txtOrderId = findViewById(R.id.txtOrderId)
        txtCustomerName = findViewById(R.id.txtCustomerName)
        btnBackToHome = findViewById(R.id.btnBackToHome)
    }

    private fun setupData() {
        val orderId = intent.getStringExtra("order_id") ?: ""
        val customerName = intent.getStringExtra("customer_name") ?: ""
        txtOrderId.text = "Pedido: #$orderId"
        txtCustomerName.text = "Cliente: $customerName"
    }

    private fun setupButtons() {
        btnBackToHome.setOnClickListener { navigateToHome() }
    }

    private fun setupBackPress() {
        onBackPressedDispatcher.addCallback(this) { navigateToHome() }
    }

    private fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
        finish()
    }
}