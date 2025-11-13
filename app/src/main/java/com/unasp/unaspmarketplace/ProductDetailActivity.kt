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
import com.unasp.unaspmarketplace.adapters.ProductDetailImageAdapter
import com.unasp.unaspmarketplace.models.Product
import com.unasp.unaspmarketplace.utils.CartManager
import com.unasp.unaspmarketplace.utils.CartBadgeManager

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
        txtSellerName.text = "Vendedor: ${product.sellerId}"

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

    private fun setupButtons() {
        btnAddToCart.setOnClickListener {
            if (product.stock >= currentQuantity) {
                addToCart()
            } else {
                Toast.makeText(this, "Estoque insuficiente!", Toast.LENGTH_SHORT).show()
            }
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
                /*R.id.nav_notifications -> {
                    Toast.makeText(this, "Notificações em breve", Toast.LENGTH_SHORT).show()
                    true
                }*/
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
}
