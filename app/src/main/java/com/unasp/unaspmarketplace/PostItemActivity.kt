package com.unasp.unaspmarketplace

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.ImageView


class PostItemActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.post_item_activity)

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish()
        }
    }
}