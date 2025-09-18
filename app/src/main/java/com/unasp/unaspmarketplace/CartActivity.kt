package com.unasp.unaspmarketplace

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
    val discountedPrice: BigDecimal
)


class CartActivity : AppCompatActivity() {

    private lateinit var recyclerCart: RecyclerView
    private lateinit var txtTotal: TextView
    lateinit var btnCheckout: Button
    private val cartItems = mutableListOf<CartItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.cart_activity)

        txtTotal = findViewById(R.id.txtTotal) // Valor total dos itens no carrinho

        // Setup da recyclerView para listar os itens
        recyclerCart = findViewById(R.id.recyclerCart)
        recyclerCart.layoutManager = LinearLayoutManager(this)
        recyclerCart.adapter = CartAdapter()

        // Itens exemplo
        cartItems.add(CartItem(
            1,
            "Produto A",
            "Produto exemplo!",
            10.0.toBigDecimal(),
            R.drawable.ic_launcher_background,
            10,
            1.toBigDecimal(),
            50.0,
            5.0.toBigDecimal()))

        cartItems.add(CartItem(
            2,
            "Produto B",
            "Produto exemplo!",
            15.0.toBigDecimal(),
            R.drawable.ic_launcher_background,
            20,
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
            recyclerCart.adapter?.notifyDataSetChanged()
            updateTotal()

        }

        // Botão checkout
        btnCheckout = findViewById(R.id.btnCheckout)
        updateCheckoutButton()
        btnCheckout.setOnClickListener {
            if (cartItems.isEmpty()) {
                Toast.makeText(this, "Carrinho Vazio", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Compra finalizada!", Toast.LENGTH_LONG).show()
            }
        }


    }

    private fun updateCheckoutButton() {
        btnCheckout.isEnabled = cartItems.isNotEmpty()
        btnCheckout.alpha = if (cartItems.isNotEmpty()) 1.0f else 0.5f // Gray out if disabled
    }

    private fun updateTotal() {
        val total = cartItems.sumOf { it.discountedPrice * it.quantity }
        txtTotal.text = "Total: R$ %.2f".format(total)
        updateCheckoutButton()
    }

    inner class CartAdapter : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {
        inner class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val txtName: TextView = itemView.findViewById(R.id.txtName)
            val txtPrice: TextView = itemView.findViewById(R.id.txtPrice)
            val txtQuantity: TextView = itemView.findViewById(R.id.txtQuantity)
            val btnIncrease: Button = itemView.findViewById(R.id.btnIncrease)
            val btnDecrease: Button = itemView.findViewById(R.id.btnDecrease)
            val btnRemove: ImageView = itemView.findViewById(R.id.btnRemove)
            val imgProduct: ImageView = itemView.findViewById(R.id.imgProduct)
        }

        // Cria uma view
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.cart_item, parent, false)
            return CartViewHolder(view)
        }

        override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
            val item = cartItems[position]
            holder.txtName.text = item.name
            holder.txtPrice.text = "R$ %.2f".format(item.discountedPrice)
            holder.txtQuantity.text = item.quantity.toString()
            holder.imgProduct.setImageResource(item.imageResId)

            // Botão incrementar (+)
            holder.btnIncrease.isEnabled = item.quantity < item.stock.toBigDecimal()
            holder.btnIncrease.setOnClickListener {
                if (item.quantity < item.stock.toBigDecimal()) {
                    item.quantity = item.quantity.add(BigDecimal.ONE)
                    notifyItemChanged(position)
                    updateTotal()
                }
            }

            // Botão decrementar (-)
            holder.btnDecrease.isEnabled = item.quantity > BigDecimal.ONE
            holder.btnDecrease.setOnClickListener {
                if (item.quantity > 1.toBigDecimal()) {
                    item.quantity = item.quantity.subtract(BigDecimal.ONE)
                    notifyItemChanged(position)
                    updateTotal()
                }
            }

            // Botão remover
            holder.btnRemove.setOnClickListener {
                cartItems.removeAt(position)
                notifyItemRemoved(position)
                notifyItemRangeChanged(position, cartItems.size)
                updateTotal()
            }
        }

        override fun getItemCount(): Int = cartItems.size

    }
}
