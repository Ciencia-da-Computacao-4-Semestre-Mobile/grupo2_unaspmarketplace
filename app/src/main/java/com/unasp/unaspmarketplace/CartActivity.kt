package com.unasp.unaspmarketplace

import android.content.Intent
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
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.unasp.unaspmarketplace.utils.CartManager
import com.unasp.unaspmarketplace.utils.CartBadgeManager

class CartActivity : AppCompatActivity(), CartManager.CartUpdateListener {

    private lateinit var recyclerCart: RecyclerView
    private lateinit var txtTotal: TextView
    private lateinit var btnCheckout: Button
    private lateinit var cartAdapter: CartAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.cart_activity)

        initViews()
        setupRecyclerView()
        setupButtons()
        setupBottomNavigation()
        loadCartItems()

        // Registrar listener do carrinho
        CartManager.addListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        CartManager.removeListener(this)
    }

    override fun onResume() {
        super.onResume()
        // Garantir que a UI esteja sincronizada quando voltar para a tela
        updateUI()
        CartBadgeManager.updateBadge(CartManager.getTotalItemCount())
    }

    override fun onCartUpdated(itemCount: Int, totalPrice: Double) {
        updateUI()
        CartBadgeManager.updateBadge(itemCount)
    }

    private fun initViews() {
        txtTotal = findViewById(R.id.txtTotal)
        btnCheckout = findViewById(R.id.btnCheckout)
        recyclerCart = findViewById(R.id.recyclerCart)

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        btnBack.setOnClickListener { finish() }

        val btnClearCart = findViewById<ImageView>(R.id.btnClearCart)
        btnClearCart.setOnClickListener {
            CartManager.clearCart()
            Toast.makeText(this, "Carrinho limpo!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupRecyclerView() {
        cartAdapter = CartAdapter()
        recyclerCart.layoutManager = LinearLayoutManager(this)
        recyclerCart.adapter = cartAdapter
    }

    private fun setupButtons() {
        btnCheckout.setOnClickListener {
            val cartItems = CartManager.getCartItems()
            if (cartItems.isEmpty()) {
                Toast.makeText(this, "Carrinho vazio!", Toast.LENGTH_SHORT).show()
            } else {
                val total = CartManager.getTotalPrice()
                // Navegar para a tela de pagamento
                val intent = Intent(this, PaymentActivity::class.java)
                intent.putExtra("totalAmount", total)
                startActivity(intent)
            }
        }
    }

    private fun loadCartItems() {
        updateUI()
    }

    private fun updateUI() {
        val cartItems = CartManager.getCartItems()
        cartAdapter.updateItems(cartItems)

        val total = CartManager.getTotalPrice()
        txtTotal.text = "Total: R$ %.2f".format(total)

        btnCheckout.isEnabled = cartItems.isNotEmpty()
        btnCheckout.alpha = if (cartItems.isNotEmpty()) 1.0f else 0.5f
    }

    private fun setupBottomNavigation() {
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation_cart)
        bottomNavigation.selectedItemId = R.id.nav_cart

        CartBadgeManager.setupCartBadge(bottomNavigation)
        CartBadgeManager.updateBadge(CartManager.getTotalItemCount())

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_menu -> {
                    val intent = Intent(this, HomeActivity::class.java)
                    intent.putExtra("openMenu", true)
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.nav_home -> {
                    val intent = Intent(this, HomeActivity::class.java)
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.nav_profile -> {
                    val intent = Intent(this, ProfileActivity::class.java)
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.nav_cart -> {
                    Toast.makeText(this, "Você já está no Carrinho", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
    }

    inner class CartAdapter : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {
        private var items = listOf<com.unasp.unaspmarketplace.utils.CartItem>()

        fun updateItems(newItems: List<com.unasp.unaspmarketplace.utils.CartItem>) {
            items = newItems
            notifyDataSetChanged()
        }

        inner class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val txtName: TextView = itemView.findViewById(R.id.txtName)
            val txtPrice: TextView = itemView.findViewById(R.id.txtPrice)
            val txtQuantity: TextView = itemView.findViewById(R.id.txtQuantity)
            val btnIncrease: Button = itemView.findViewById(R.id.btnIncrease)
            val btnDecrease: Button = itemView.findViewById(R.id.btnDecrease)
            val btnRemove: Button = itemView.findViewById(R.id.btnRemove)
            val imgProduct: ImageView = itemView.findViewById(R.id.imgProduct)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.cart_item, parent, false)
            return CartViewHolder(view)
        }

        override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
            val item = items[position]
            val product = item.product

            holder.txtName.text = product.name
            holder.txtPrice.text = "R$ %.2f".format(item.totalPrice)
            holder.txtQuantity.text = item.quantity.toString()
            holder.imgProduct.setImageResource(R.drawable.ic_launcher_background)

            // Botão incrementar (+)
            holder.btnIncrease.isEnabled = item.quantity < product.stock
            holder.btnIncrease.setOnClickListener {
                if (CartManager.updateQuantity(product.id, item.quantity + 1)) {
                    // Atualização bem-sucedida
                } else {
                    Toast.makeText(holder.itemView.context, "Estoque insuficiente!", Toast.LENGTH_SHORT).show()
                }
            }

            // Botão decrementar (-)
            holder.btnDecrease.isEnabled = item.quantity > 1
            holder.btnDecrease.setOnClickListener {
                if (item.quantity > 1) {
                    CartManager.updateQuantity(product.id, item.quantity - 1)
                }
            }

            // Botão remover
            holder.btnRemove.setOnClickListener {
                CartManager.removeFromCart(product.id)
                Toast.makeText(holder.itemView.context, "Item removido do carrinho", Toast.LENGTH_SHORT).show()
            }
        }

        override fun getItemCount(): Int = items.size
    }
}
