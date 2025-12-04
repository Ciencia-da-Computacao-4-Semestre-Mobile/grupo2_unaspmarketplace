package com.unasp.unaspmarketplace

import android.content.Intent
import android.os.Looper
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.unasp.unaspmarketplace.utils.CartManager
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class ProductDetailActivityTest {

    @Before
    fun setup() {
        CartManager.clearCart()
    }

    @After
    fun tearDown() {
        CartManager.clearCart()
    }

    // ========== Testes de displayProductInfo() ==========

    @Test
    fun displayProductInfo_setsProductName() {
        val intent = createTestIntent(name = "Notebook Dell")

        val controller = Robolectric.buildActivity(ProductDetailActivity::class.java, intent).setup()
        val activity = controller.get()

        val txtName = activity.findViewById<TextView>(R.id.txtProductName)
        assertEquals("Notebook Dell", txtName.text.toString())
    }

    @Test
    fun displayProductInfo_setsProductCategory() {
        val intent = createTestIntent(category = "Eletrônicos")

        val controller = Robolectric.buildActivity(ProductDetailActivity::class.java, intent).setup()
        val activity = controller.get()

        val txtCategory = activity.findViewById<TextView>(R.id.txtProductCategory)
        assertEquals("Eletrônicos", txtCategory.text.toString())
    }

    @Test
    fun displayProductInfo_setsFormattedPrice() {
        val intent = createTestIntent(price = 1999.99)

        val controller = Robolectric.buildActivity(ProductDetailActivity::class.java, intent).setup()
        val activity = controller.get()

        val txtPrice = activity.findViewById<TextView>(R.id.txtProductPrice)
        assertEquals("R$ 1999.99", txtPrice.text.toString())
    }

    @Test
    fun displayProductInfo_setsStockWithUnits() {
        val intent = createTestIntent(stock = 15)

        val controller = Robolectric.buildActivity(ProductDetailActivity::class.java, intent).setup()
        val activity = controller.get()

        val txtStock = activity.findViewById<TextView>(R.id.txtProductStock)
        assertEquals("15 unidades", txtStock.text.toString())
    }

    @Test
    fun displayProductInfo_setsDescription() {
        val intent = createTestIntent(description = "Produto em ótimo estado, usado apenas 2 vezes")

        val controller = Robolectric.buildActivity(ProductDetailActivity::class.java, intent).setup()
        val activity = controller.get()

        val txtDescription = activity.findViewById<TextView>(R.id.txtProductDescription)
        assertEquals("Produto em ótimo estado, usado apenas 2 vezes", txtDescription.text.toString())
    }

    @Test
    fun displayProductInfo_withMultipleImages_populatesRecycler() {
        val imageUrls = arrayOf("url1.jpg", "url2.jpg", "url3.jpg")
        val intent = createTestIntent(imageUrls = imageUrls)

        val controller = Robolectric.buildActivity(ProductDetailActivity::class.java, intent).setup()
        val activity = controller.get()
        shadowOf(Looper.getMainLooper()).idle()

        val recycler = activity.findViewById<RecyclerView>(R.id.recyclerProductImages)
        assertNotNull(recycler.adapter)
        assertEquals(3, recycler.adapter?.itemCount)
    }

    @Test
    fun displayProductInfo_withEmptyImages_recyclerHasNoItems() {
        val intent = createTestIntent(imageUrls = emptyArray())

        val controller = Robolectric.buildActivity(ProductDetailActivity::class.java, intent).setup()
        val activity = controller.get()
        shadowOf(Looper.getMainLooper()).idle()

        val recycler = activity.findViewById<RecyclerView>(R.id.recyclerProductImages)
        assertEquals(0, recycler.adapter?.itemCount ?: 0)
    }

    // ========== Testes de Controle de Quantidade ==========

    @Test
    fun quantityControls_initialQuantityIsOne() {
        val intent = createTestIntent(stock = 10)

        val controller = Robolectric.buildActivity(ProductDetailActivity::class.java, intent).setup()
        val activity = controller.get()

        val txtQuantity = activity.findViewById<TextView>(R.id.txtQuantity)
        assertEquals("1", txtQuantity.text.toString())
    }

    @Test
    fun quantityControls_decreaseButton_disabledAtMinimum() {
        val intent = createTestIntent(stock = 10)

        val controller = Robolectric.buildActivity(ProductDetailActivity::class.java, intent).setup()
        val activity = controller.get()

        val btnDecrease = activity.findViewById<MaterialButton>(R.id.btnDecrease)
        assertFalse("Botão diminuir deve estar desabilitado em quantidade 1", btnDecrease.isEnabled)
    }

    @Test
    fun quantityControls_increaseButton_enabledWhenStockAvailable() {
        val intent = createTestIntent(stock = 5)

        val controller = Robolectric.buildActivity(ProductDetailActivity::class.java, intent).setup()
        val activity = controller.get()

        val btnIncrease = activity.findViewById<MaterialButton>(R.id.btnIncrease)
        assertTrue("Botão aumentar deve estar habilitado quando há estoque", btnIncrease.isEnabled)
    }

    @Test
    fun quantityControls_increaseButton_disabledAtMaxStock() {
        val intent = createTestIntent(stock = 1)

        val controller = Robolectric.buildActivity(ProductDetailActivity::class.java, intent).setup()
        val activity = controller.get()

        val btnIncrease = activity.findViewById<MaterialButton>(R.id.btnIncrease)
        assertFalse("Botão aumentar deve estar desabilitado quando quantidade = estoque", btnIncrease.isEnabled)
    }

    @Test
    fun quantityControls_clickIncrease_updatesQuantity() {
        val intent = createTestIntent(stock = 10)

        val controller = Robolectric.buildActivity(ProductDetailActivity::class.java, intent).setup()
        val activity = controller.get()

        val btnIncrease = activity.findViewById<MaterialButton>(R.id.btnIncrease)
        val txtQuantity = activity.findViewById<TextView>(R.id.txtQuantity)

        btnIncrease.performClick()
        shadowOf(Looper.getMainLooper()).idle()

        assertEquals("2", txtQuantity.text.toString())
    }

    @Test
    fun quantityControls_clickDecrease_updatesQuantity() {
        val intent = createTestIntent(stock = 10)

        val controller = Robolectric.buildActivity(ProductDetailActivity::class.java, intent).setup()
        val activity = controller.get()

        val btnIncrease = activity.findViewById<MaterialButton>(R.id.btnIncrease)
        val btnDecrease = activity.findViewById<MaterialButton>(R.id.btnDecrease)
        val txtQuantity = activity.findViewById<TextView>(R.id.txtQuantity)

        // Aumentar para 2
        btnIncrease.performClick()
        shadowOf(Looper.getMainLooper()).idle()

        // Diminuir para 1
        btnDecrease.performClick()
        shadowOf(Looper.getMainLooper()).idle()

        assertEquals("1", txtQuantity.text.toString())
    }

    @Test
    fun quantityControls_cannotDecreaseBelow1() {
        val intent = createTestIntent(stock = 10)

        val controller = Robolectric.buildActivity(ProductDetailActivity::class.java, intent).setup()
        val activity = controller.get()

        val btnDecrease = activity.findViewById<MaterialButton>(R.id.btnDecrease)
        val txtQuantity = activity.findViewById<TextView>(R.id.txtQuantity)

        // Tentar diminuir quando já está em 1
        btnDecrease.performClick()
        shadowOf(Looper.getMainLooper()).idle()

        assertEquals("1", txtQuantity.text.toString())
    }

    @Test
    fun quantityControls_cannotIncreaseAboveStock() {
        val intent = createTestIntent(stock = 2)

        val controller = Robolectric.buildActivity(ProductDetailActivity::class.java, intent).setup()
        val activity = controller.get()

        val btnIncrease = activity.findViewById<MaterialButton>(R.id.btnIncrease)
        val txtQuantity = activity.findViewById<TextView>(R.id.txtQuantity)

        // Aumentar para 2
        btnIncrease.performClick()
        shadowOf(Looper.getMainLooper()).idle()

        // Tentar aumentar para 3 (não deve funcionar)
        btnIncrease.performClick()
        shadowOf(Looper.getMainLooper()).idle()

        assertEquals("2", txtQuantity.text.toString())
    }

    // ========== Testes de Adicionar ao Carrinho ==========

    @Test
    fun addToCart_withStockZero_buttonDisabled() {
        val intent = createTestIntent(stock = 0)

        val controller = Robolectric.buildActivity(ProductDetailActivity::class.java, intent).setup()
        val activity = controller.get()

        val btnAddToCart = activity.findViewById<MaterialButton>(R.id.btnAddToCart)
        assertFalse(btnAddToCart.isEnabled)
        assertEquals("Produto Esgotado", btnAddToCart.text.toString())
    }

    @Test
    fun addToCart_withPositiveStock_buttonEnabled() {
        val intent = createTestIntent(stock = 5)

        val controller = Robolectric.buildActivity(ProductDetailActivity::class.java, intent).setup()
        val activity = controller.get()

        val btnAddToCart = activity.findViewById<MaterialButton>(R.id.btnAddToCart)
        assertTrue(btnAddToCart.isEnabled)
        assertEquals("Adicionar ao Carrinho", btnAddToCart.text.toString())
    }

    @Test
    fun addToCart_clickButton_addsToCart() {
        val intent = createTestIntent(stock = 10, name = "Produto Teste")

        val controller = Robolectric.buildActivity(ProductDetailActivity::class.java, intent).setup()
        val activity = controller.get()

        val btnAddToCart = activity.findViewById<MaterialButton>(R.id.btnAddToCart)
        btnAddToCart.performClick()
        shadowOf(Looper.getMainLooper()).idle()

        assertEquals(1, CartManager.getTotalItemCount())
    }

    @Test
    fun addToCart_multipleClicks_increasesQuantity() {
        val intent = createTestIntent(stock = 10)

        val controller = Robolectric.buildActivity(ProductDetailActivity::class.java, intent).setup()
        val activity = controller.get()

        val btnAddToCart = activity.findViewById<MaterialButton>(R.id.btnAddToCart)

        // Adicionar 3 vezes
        repeat(3) {
            btnAddToCart.performClick()
            shadowOf(Looper.getMainLooper()).idle()
        }

        assertEquals(3, CartManager.getTotalItemCount())
    }

    // ========== Testes de Interface ==========

    @Test
    fun allRequiredViews_existInLayout() {
        val intent = createTestIntent()

        val controller = Robolectric.buildActivity(ProductDetailActivity::class.java, intent).setup()
        val activity = controller.get()

        assertNotNull(activity.findViewById<TextView>(R.id.txtProductName))
        assertNotNull(activity.findViewById<TextView>(R.id.txtProductCategory))
        assertNotNull(activity.findViewById<TextView>(R.id.txtProductPrice))
        assertNotNull(activity.findViewById<TextView>(R.id.txtProductStock))
        assertNotNull(activity.findViewById<TextView>(R.id.txtProductDescription))
        assertNotNull(activity.findViewById<TextView>(R.id.txtSellerName))
        assertNotNull(activity.findViewById<ImageView>(R.id.imgProductDetail))
        assertNotNull(activity.findViewById<RecyclerView>(R.id.recyclerProductImages))
        assertNotNull(activity.findViewById<MaterialButton>(R.id.btnAddToCart))
        assertNotNull(activity.findViewById<MaterialButton>(R.id.btnContactSeller))
    }

    @Test
    fun btnBack_finishesActivity() {
        val intent = createTestIntent()

        val controller = Robolectric.buildActivity(ProductDetailActivity::class.java, intent).setup()
        val activity = controller.get()

        val btnBack = activity.findViewById<ImageView>(R.id.btnBack)
        btnBack.performClick()
        shadowOf(Looper.getMainLooper()).idle()

        assertTrue(activity.isFinishing)
    }

    @Test
    fun contactSellerButton_existsAndIsClickable() {
        val intent = createTestIntent()

        val controller = Robolectric.buildActivity(ProductDetailActivity::class.java, intent).setup()
        val activity = controller.get()

        val btnContact = activity.findViewById<MaterialButton>(R.id.btnContactSeller)
        assertNotNull(btnContact)
        assertTrue(btnContact.isClickable)
    }

    // ========== Função Helper ==========

    private fun createTestIntent(
        id: String = "test-id",
        name: String = "Produto Teste",
        description: String = "Descrição teste",
        price: Double = 50.0,
        stock: Int = 10,
        category: String = "Outros",
        sellerId: String = "seller-id",
        imageUrls: Array<String> = emptyArray(),
        active: Boolean = true
    ): Intent {
        return Intent().apply {
            putExtra("productId", id)
            putExtra("productName", name)
            putExtra("productDescription", description)
            putExtra("productPrice", price)
            putExtra("productStock", stock)
            putExtra("productCategory", category)
            putExtra("productSellerId", sellerId)
            putExtra("productImageUrls", imageUrls)
            putExtra("productActive", active)
        }
    }
}
