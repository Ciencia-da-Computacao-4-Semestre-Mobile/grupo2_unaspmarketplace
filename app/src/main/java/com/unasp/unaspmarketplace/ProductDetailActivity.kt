package com.unasp.unaspmarketplace

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FirebaseFirestore
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.unasp.unaspmarketplace.adapters.ProductDetailImageAdapter
import com.unasp.unaspmarketplace.models.Product
import com.unasp.unaspmarketplace.models.User
import com.unasp.unaspmarketplace.utils.CartManager
import com.unasp.unaspmarketplace.utils.CartBadgeManager
import com.unasp.unaspmarketplace.utils.WhatsAppHelper
import com.unasp.unaspmarketplace.utils.UserUtils

class ProductDetailActivity : AppCompatActivity(), CartManager.CartUpdateListener {
    private lateinit var product: Product
    private var currentQuantity = 1

    private lateinit var txtProductName: TextView
    private lateinit var txtProductCategory: TextView
    private lateinit var txtProductPrice: TextView
    private lateinit var txtProductStock: TextView
    private lateinit var txtProductDescription: TextView
    private lateinit var txtSellerName: TextView
    private lateinit var imgProductDetail: ImageView
    private lateinit var recyclerProductImages: RecyclerView
    private lateinit var txtQuantity: TextView
    private lateinit var btnDecrease: MaterialButton
    private lateinit var btnIncrease: MaterialButton
    private lateinit var btnAddToCart: MaterialButton
    private lateinit var btnContactSeller: MaterialButton

    private lateinit var imageAdapter: ProductDetailImageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_detail)

        initViews()
        getProductFromIntent()
        setupQuantityControls()
        setupButtons()
        setupBottomNavigation()
        displayProductInfo()

        // Registrar listener do carrinho
        CartManager.addListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Remover listener para evitar memory leaks
        CartManager.removeListener(this)
    }

    override fun onCartUpdated(itemCount: Int, totalPrice: Double) {
        // Atualizar badge do carrinho
        CartBadgeManager.updateBadge(itemCount)
    }

    private fun initViews() {
        txtProductName = findViewById(R.id.txtProductName)
        txtProductCategory = findViewById(R.id.txtProductCategory)
        txtProductPrice = findViewById(R.id.txtProductPrice)
        txtProductStock = findViewById(R.id.txtProductStock)
        txtProductDescription = findViewById(R.id.txtProductDescription)
        txtSellerName = findViewById(R.id.txtSellerName)
        imgProductDetail = findViewById(R.id.imgProductDetail)
        recyclerProductImages = findViewById(R.id.recyclerProductImages)
        txtQuantity = findViewById(R.id.txtQuantity)
        btnDecrease = findViewById(R.id.btnDecrease)
        btnIncrease = findViewById(R.id.btnIncrease)
        btnAddToCart = findViewById(R.id.btnAddToCart)
        btnContactSeller = findViewById(R.id.btnContactSeller)

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        btnBack.setOnClickListener { finish() }

        // Configurar RecyclerView de imagens
        setupImageRecycler()
    }

    private fun setupImageRecycler() {
        imageAdapter = ProductDetailImageAdapter { imageUrl ->
            // Quando uma imagem é clicada, mostrar na ImageView principal
            Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)
                .centerCrop()
                .into(imgProductDetail)
        }

        recyclerProductImages.apply {
            adapter = imageAdapter
            layoutManager = LinearLayoutManager(this@ProductDetailActivity, LinearLayoutManager.HORIZONTAL, false)
        }
    }

    private fun getProductFromIntent() {
        // Receber dados do produto via Intent
        val imageUrls = intent.getStringArrayExtra("productImageUrls")?.toList() ?: emptyList()

        product = Product(
            id = intent.getStringExtra("productId") ?: "",
            name = intent.getStringExtra("productName") ?: "",
            description = intent.getStringExtra("productDescription") ?: "",
            price = intent.getDoubleExtra("productPrice", 0.0),
            stock = intent.getIntExtra("productStock", 0),
            category = intent.getStringExtra("productCategory") ?: "",
            sellerId = intent.getStringExtra("productSellerId") ?: "",
            imageUrls = imageUrls,
            active = intent.getBooleanExtra("productActive", true)
        )
    }

    private fun displayProductInfo() {
        txtProductName.text = product.name
        txtProductCategory.text = product.category
        txtProductPrice.text = "R$ %.2f".format(product.price)
        txtProductStock.text = "${product.stock} unidades"
        txtProductDescription.text = product.description

        // Buscar nome real do vendedor
        loadSellerName()

        // Exibir imagens do produto
        if (product.imageUrls.isNotEmpty()) {
            // Mostrar primeira imagem na ImageView principal
            Glide.with(this)
                .load(product.imageUrls.first())
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)
                .centerCrop()
                .into(imgProductDetail)

            // Atualizar adapter com todas as imagens
            imageAdapter.updateImages(product.imageUrls)
        } else {
            // Usar imagem padrão se não houver imagens
            imgProductDetail.setImageResource(R.drawable.ic_launcher_background)
            imageAdapter.updateImages(emptyList())
        }

        // Atualizar botões baseado no estoque
        updateQuantityControls()
        updateAddToCartButton()
        updateContactSellerButton()
    }

    private fun setupQuantityControls() {
        btnDecrease.setOnClickListener {
            if (currentQuantity > 1) {
                currentQuantity--
                updateQuantityDisplay()
                updateQuantityControls()
            }
        }

        btnIncrease.setOnClickListener {
            if (currentQuantity < product.stock) {
                currentQuantity++
                updateQuantityDisplay()
                updateQuantityControls()
            }
        }

        updateQuantityDisplay()
    }

    private fun updateQuantityDisplay() {
        txtQuantity.text = currentQuantity.toString()
    }

    private fun updateQuantityControls() {
        btnDecrease.isEnabled = currentQuantity > 1
        btnIncrease.isEnabled = currentQuantity < product.stock

        // Visual feedback
        btnDecrease.alpha = if (currentQuantity > 1) 1.0f else 0.5f
        btnIncrease.alpha = if (currentQuantity < product.stock) 1.0f else 0.5f
    }

    private fun updateAddToCartButton() {
        btnAddToCart.isEnabled = product.stock > 0
        if (product.stock > 0) {
            btnAddToCart.text = "Adicionar ao Carrinho"
            btnAddToCart.alpha = 1.0f
        } else {
            btnAddToCart.text = "Produto Esgotado"
            btnAddToCart.alpha = 0.6f
        }
    }

    private fun updateContactSellerButton() {
        lifecycleScope.launch {
            try {
                val currentUserId = UserUtils.getCurrentUserId()

                if (currentUserId == product.sellerId) {
                    // É o próprio produto do usuário
                    runOnUiThread {
                        btnContactSeller.text = "📦 Seu Produto"
                        btnContactSeller.isEnabled = false
                        btnContactSeller.alpha = 0.6f
                    }
                } else {
                    // Produto de outro usuário
                    runOnUiThread {
                        btnContactSeller.text = "💬 Falar com o Vendedor"
                        btnContactSeller.isEnabled = true
                        btnContactSeller.alpha = 1.0f
                    }
                }
            } catch (e: Exception) {
                // Em caso de erro, manter o botão ativo
                runOnUiThread {
                    btnContactSeller.isEnabled = true
                    btnContactSeller.alpha = 1.0f
                }
            }
        }
    }

    private fun setupButtons() {
        btnAddToCart.setOnClickListener {
            if (product.stock >= currentQuantity) {
                addToCart()
            } else {
                Toast.makeText(this, "Estoque insuficiente!", Toast.LENGTH_SHORT).show()
            }
        }

        btnContactSeller.setOnClickListener {
            contactSeller()
        }
    }

    private fun addToCart() {
        val success = CartManager.addToCart(product, currentQuantity)

        if (success) {
            val totalItems = CartManager.getTotalItemCount()
            Toast.makeText(
                this,
                "Adicionado ao carrinho!\n${product.name}\nQuantidade: $currentQuantity",
                Toast.LENGTH_LONG
            ).show()

            // Forçar atualização do badge
            CartBadgeManager.updateBadge(totalItems)
        } else {
            Toast.makeText(
                this,
                "Erro: Estoque insuficiente!\nDisponível: ${product.stock} unidades",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun setupBottomNavigation() {
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation_product)

        // Configurar badge do carrinho
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
                    val intent = Intent(this, CartActivity::class.java)
                    startActivity(intent)
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    /**
     * Busca o nome real do vendedor pelo sellerId
     */
    private fun loadSellerName() {
        if (product.sellerId.isEmpty()) {
            txtSellerName.text = "Vendedor: Não informado"
            return
        }

        lifecycleScope.launch {
            try {
                val firestore = FirebaseFirestore.getInstance()
                val userDoc = firestore.collection("users")
                    .document(product.sellerId)
                    .get()
                    .await()

                if (userDoc.exists()) {
                    val user = userDoc.toObject(User::class.java)
                    val sellerName = user?.name ?: "Nome não disponível"
                    runOnUiThread {
                        txtSellerName.text = "Vendedor: $sellerName"
                    }
                } else {
                    runOnUiThread {
                        txtSellerName.text = "Vendedor: Usuário não encontrado"
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("ProductDetail", "Erro ao buscar vendedor", e)
                runOnUiThread {
                    txtSellerName.text = "Vendedor: Erro ao carregar"
                }
            }
        }
    }

    private fun contactSeller() {
        lifecycleScope.launch {
            try {
                // Buscar dados do vendedor
                val sellerDoc = FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(product.sellerId)
                    .get()
                    .await()

                val seller = sellerDoc.toObject(User::class.java)

                if (seller != null && seller.whatsappNumber.isNotBlank()) {
                    // Buscar dados do comprador atual
                    val currentUser = UserUtils.getCurrentUser()
                    val buyerName = currentUser?.name ?: "Comprador"

                    // Criar mensagem personalizada
                    val message = formatContactMessage(seller.name, buyerName)

                    runOnUiThread {
                        Toast.makeText(this@ProductDetailActivity, "Abrindo WhatsApp...", Toast.LENGTH_SHORT).show()
                        WhatsAppHelper.sendMessage(this@ProductDetailActivity, message, seller.whatsappNumber)
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(
                            this@ProductDetailActivity,
                            "Vendedor não possui WhatsApp cadastrado. Adicione o produto ao carrinho e finalize a compra.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("ProductDetail", "Erro ao contatar vendedor", e)
                runOnUiThread {
                    Toast.makeText(
                        this@ProductDetailActivity,
                        "Erro ao buscar dados do vendedor: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun formatContactMessage(sellerName: String, buyerName: String): String {
        return """
👋 Olá, $sellerName!

Eu sou $buyerName e tenho interesse no seu produto:

📦 *${product.name}*
💰 R$ ${String.format("%.2f", product.price)}
📂 Categoria: ${product.category}

Gostaria de saber mais detalhes sobre o produto. Você pode me ajudar?

_Mensagem enviada através do UNASP Marketplace_
        """.trimIndent()
    }
}
