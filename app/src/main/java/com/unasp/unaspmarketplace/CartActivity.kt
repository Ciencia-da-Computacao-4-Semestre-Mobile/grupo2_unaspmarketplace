package com.unasp.unaspmarketplace

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.ImageView
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.math.BigDecimal


data class CartItem(
    val id: Long,
    val name: String,
    val description: String,
    val price: BigDecimal,
    val imageResId: Int,
    var stock: Int,
    var quantity: BigDecimal,
    val discountPercentage: Double,
    val discountedPrive: BigDecimal
)


class CartActivity : AppCompatActivity() {

    private lateinit var recyclerCart: RecyclerView
    private lateinit var txtTotal: TextView
    private val cartItems = mutableListOf<CartItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.cart_activity)

        txtTotal = findViewById(R.id.txtTotal) // Valor total dos itens no carrinho

        // Setup da recyclerView para listar os itens
        recyclerCart = findViewById(R.id.recyclerCart)
        recyclerCart.layoutManager = LinearLayoutManager(this)


        // Itens exemplo
        cartItems.add(CartItem(
            1,
            "Produto A",
            "Produto exemplo!",
            10.0.toBigDecimal(),
            R.drawable.ic_launcher_background,
            100,
            1.toBigDecimal(),
            50.0,
            5.0.toBigDecimal()))

        cartItems.add(CartItem(
            1,
            "Produto B",
            "Produto exemplo!",
            10.0.toBigDecimal(),
            R.drawable.ic_launcher_background,
            100,
            1.toBigDecimal(),
            50.0,
            5.0.toBigDecimal()))

        // Botão voltar
        val btnBack = findViewById<ImageView>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish()
        }

        // Botâo esvaziar carrinho
        val btnClearCart = findViewById<ImageView>(R.id.btnClearCart)
        btnClearCart.setOnClickListener {
            cartItems.clear()
            updateTotal()

        }

        // Botão checkout
        val btnCheckout = findViewById<Button>(R.id.btnCheckout)
        btnCheckout.setOnClickListener {
            Toast.makeText(this, "Compra finalizada!", Toast.LENGTH_LONG).show()
        }


    }

    private fun updateTotal() {
        val total = cartItems.sumOf { it.price * it.quantity }
        txtTotal.text = "Total: R$ %.2f".format(total)
    }
}
