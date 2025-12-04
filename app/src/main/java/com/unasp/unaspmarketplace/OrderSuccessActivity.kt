package com.unasp.unaspmarketplace

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class OrderSuccessActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_success)

        setupButtons()
        setupBackPress()
    }

    private fun setupButtons() {
        val btnViewHistory = findViewById<MaterialButton>(R.id.btnViewHistory)
        val btnContinueShopping = findViewById<MaterialButton>(R.id.btnContinueShopping)

        btnViewHistory.setOnClickListener {
            val intent = Intent(this, OrderHistoryActivity::class.java)
            startActivity(intent)
            finish()
        }

        btnContinueShopping.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }
    }
}
