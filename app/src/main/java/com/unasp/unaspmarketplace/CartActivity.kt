package com.unasp.unaspmarketplace

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.ImageView


class CartActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.cart_activity)

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish()
        }
    }
}