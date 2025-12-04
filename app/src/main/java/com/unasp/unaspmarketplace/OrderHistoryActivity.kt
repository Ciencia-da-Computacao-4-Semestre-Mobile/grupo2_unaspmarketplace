package com.unasp.unaspmarketplace

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton

class OrderHistoryActivity : AppCompatActivity() {

    private lateinit var recyclerOrders: RecyclerView
    private lateinit var txtEmptyState: View
    private lateinit var btnStartShopping: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_history)

        setupToolbar()
        initViews()
        setupClickListeners()
        loadOrderHistory()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun initViews() {
        recyclerOrders = findViewById(R.id.recyclerOrders)
        txtEmptyState = findViewById(R.id.txtEmptyState)
        btnStartShopping = findViewById(R.id.btnStartShopping)

        recyclerOrders.layoutManager = LinearLayoutManager(this)
    }

    private fun setupClickListeners() {
        btnStartShopping.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }
    }

    private fun loadOrderHistory() {
        // Por enquanto, mostrar estado vazio
        // TODO: Implementar carregamento real do histórico
        showEmptyState()
    }

    private fun showEmptyState() {
        recyclerOrders.visibility = View.GONE
        txtEmptyState.visibility = View.VISIBLE
    }
}
