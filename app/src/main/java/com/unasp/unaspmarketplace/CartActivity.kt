package com.unasp.unaspmarketplace

import android.content.Intent
import android.graphics.Canvas
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import android.widget.ImageView
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.unasp.unaspmarketplace.utils.CartManager
import com.unasp.unaspmarketplace.utils.CartBadgeManager
import kotlin.compareTo
import kotlin.div
import kotlin.or
import kotlin.text.compareTo
import kotlin.text.get
import kotlin.text.toFloat
import kotlin.times

class CartActivity : AppCompatActivity(), CartManager.CartUpdateListener {

    private lateinit var recyclerCart: RecyclerView
    private lateinit var txtTotal: TextView
    private lateinit var btnCheckout: Button
    private lateinit var cartAdapter: CartAdapter
    private lateinit var toolbar: MaterialToolbar
    private lateinit var emptyCartView: LinearLayout
    private lateinit var cartFooter: LinearLayout
    private lateinit var btnStartShopping: Button


    // Listener para ações do carrinho (remoção de itens, limpeza do carrinho)
    private val cartActionsListener = object : CartManager.CartActionListener {
        override fun onItemsRemoved(removed: List<com.unasp.unaspmarketplace.utils.CartItem>) {
            val first = removed.firstOrNull() ?: return
            Snackbar.make(findViewById(android.R.id.content),
                "${first.product.name} removido",
                Snackbar.LENGTH_LONG
            ).setAction("Desfazer") {
                removed.forEach { CartManager.addToCart(it.product, it.quantity) }
            }.show()
        }
        override fun onCartCleared(removed: List<com.unasp.unaspmarketplace.utils.CartItem>) {
            if (removed.isEmpty()) return
            Snackbar.make(findViewById(android.R.id.content),
                "Carrinho limpo",
                Snackbar.LENGTH_LONG
            ).setAction("Desfazer") {
                removed.forEach { CartManager.addToCart(it.product, it.quantity) }
            }.show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.cart_activity)

        initViews()
        setupToolbar()
        setupRecyclerView()
        setupButtons()
        setupBottomNavigation()
        loadCartItems()
    }

    override fun onStart() {
        super.onStart()
        CartManager.addListener(this)
        CartManager.addActionListener(cartActionsListener)
    }

    override fun onStop() {
        super.onStop()
        CartManager.removeListener(this)
        CartManager.removeActionListener(cartActionsListener)
    }

    override fun onResume() {
        super.onResume()
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
        emptyCartView = findViewById(R.id.emptyCartView)
        cartFooter = findViewById(R.id.cart_footer)
        btnStartShopping = findViewById(R.id.btnStartShopping)
    }

    private fun setupToolbar() {
        toolbar = findViewById(R.id.appbar_cart)
        toolbar.subtitle = ""
        toolbar.setNavigationOnClickListener { finish() }

        toolbar.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.btnClearCart) {
                MaterialAlertDialogBuilder(this)
                    .setTitle("Limpar carrinho")
                    .setMessage("Tem certeza que deseja remover todos os itens?")
                    .setPositiveButton("Limpar") { _, _ -> CartManager.clearCart() }
                    .setNegativeButton("Cancelar", null)
                    .show()
                true
            } else false
        }
    }

    private fun setupRecyclerView() {
        cartAdapter = CartAdapter()
        recyclerCart.layoutManager = LinearLayoutManager(this)
        recyclerCart.adapter = cartAdapter
        setupItemTouchHelper()
    }

    //Função para configurar o swipe to delete
    private fun setupItemTouchHelper() {
        val callback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder) = false

            override fun onSwiped(vh: RecyclerView.ViewHolder, dir: Int) {
                val position = vh.adapterPosition
                val items = CartManager.getCartItems()
                if (position !in items.indices) return
                val removed = items[position]
                CartManager.removeFromCart(removed.product.id)
            }

            override fun onChildDraw(
                c: Canvas,
                rv: RecyclerView,
                vh: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                state: Int,
                active: Boolean
            ) {
                if (state != ItemTouchHelper.ACTION_STATE_SWIPE) {
                    super.onChildDraw(c, rv, vh, dX, dY, state, active)
                    return
                }
                val itemView = vh.itemView
                val fg = itemView.findViewById<View>(R.id.view_foreground)
                val bg = itemView.findViewById<View>(R.id.view_background)
                val iconLeft = itemView.findViewById<ImageView>(R.id.icon_delete_left)
                val iconRight = itemView.findViewById<ImageView>(R.id.icon_delete_right)

                fg.translationX = dX
                bg.visibility = if (dX == 0f) View.INVISIBLE else View.VISIBLE

                val isRightSwipe = dX > 0
                val activeIcon = if (isRightSwipe) iconLeft else iconRight
                val inactiveIcon = if (isRightSwipe) iconRight else iconLeft

                // Reset ícone oposto
                inactiveIcon.visibility = View.INVISIBLE
                inactiveIcon.alpha = 0f
                inactiveIcon.scaleX = 0f
                inactiveIcon.scaleY = 0f
                inactiveIcon.translationX = 0f

                val width = itemView.width.toFloat().coerceAtLeast(1f)
                val absDx = kotlin.math.abs(dX)
                val growthEnd = 0.25f * width

                val growT = (absDx / growthEnd).coerceIn(0f, 1f)
                activeIcon.visibility = View.VISIBLE
                activeIcon.alpha = 1f
                activeIcon.scaleX = growT
                activeIcon.scaleY = growT

                val slideT = ((absDx - growthEnd) / (width / 2f)).coerceIn(0f, 1f)
                val centerX = width / 2f
                val currentHalfW = (activeIcon.width * activeIcon.scaleX) / 2f
                val baseCenterX = activeIcon.left.toFloat() + currentHalfW
                val neededTx = centerX - baseCenterX
                activeIcon.translationX = neededTx * slideT
            }

            override fun clearView(rv: RecyclerView, vh: RecyclerView.ViewHolder) {
                super.clearView(rv, vh)
                val itemView = vh.itemView
                itemView.findViewById<View>(R.id.view_foreground).translationX = 0f
                itemView.findViewById<View>(R.id.view_background).visibility = View.INVISIBLE
                fun reset(iv: ImageView) {
                    iv.visibility = View.INVISIBLE
                    iv.alpha = 0f
                    iv.scaleX = 0f
                    iv.scaleY = 0f
                    iv.translationX = 0f
                }
                reset(itemView.findViewById(R.id.icon_delete_left))
                reset(itemView.findViewById(R.id.icon_delete_right))
            }
        }
        ItemTouchHelper(callback).attachToRecyclerView(recyclerCart)
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

        btnStartShopping.setOnClickListener {
            finish()
        }
    }

    private fun setEmptyState(isEmpty: Boolean) {
        emptyCartView.visibility = if (isEmpty) View.VISIBLE else View.GONE
        recyclerCart.visibility = if (isEmpty) View.GONE else View.VISIBLE
        cartFooter.visibility = if (isEmpty) View.GONE else View.VISIBLE

        btnCheckout.isEnabled = !isEmpty
        btnCheckout.alpha = if (!isEmpty) 1.0f else 0.5f
    }

    private fun loadCartItems() {
        updateUI()
    }

    private fun updateUI() {
        val cartItems = CartManager.getCartItems()
        cartAdapter.updateItems(cartItems)

        setEmptyState(cartItems.isEmpty())

        val total = CartManager.getTotalPrice()
        txtTotal.text = "Total: R$ %.2f".format(total)

        btnCheckout.isEnabled = cartItems.isNotEmpty()
        btnCheckout.alpha = if (cartItems.isNotEmpty()) 1.0f else 0.5f

        toolbar.subtitle = "${CartManager.getTotalItemCount()} itens" // Atualizar subtítulo com número de itens
        val clearItem = toolbar.menu.findItem(R.id.btnClearCart)
        clearItem?.isVisible = cartItems.isNotEmpty() // Mostrar botão limpar apenas se houver itens
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
                R.id.nav_notifications -> {
                    Toast.makeText(this, "Notificações em breve", Toast.LENGTH_SHORT).show()
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
            holder.btnDecrease.setOnClickListener {
                if (item.quantity > 1) {
                    CartManager.updateQuantity(product.id, item.quantity - 1)
                } else {
                    CartManager.removeFromCart(product.id)
                }
            }

            // Botão remover
            holder.btnRemove.setOnClickListener {
                CartManager.removeFromCart(product.id)
            }
        }

        override fun getItemCount(): Int = items.size
    }
}
