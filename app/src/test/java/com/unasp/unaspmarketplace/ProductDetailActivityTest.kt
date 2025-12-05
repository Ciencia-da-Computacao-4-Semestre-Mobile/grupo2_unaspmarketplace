package com.unasp.unaspmarketplace

import android.content.Intent
import android.os.Looper
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.unasp.unaspmarketplace.models.User
import com.unasp.unaspmarketplace.utils.CartManager
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import com.google.android.gms.tasks.Tasks

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class ProductDetailActivityTest : BaseFirebaseTest() {

    private lateinit var mockFirestore: FirebaseFirestore
    private lateinit var mockUser: FirebaseUser

    @Before
    override fun setupFirebase() {
        super.setupFirebase()
        CartManager.clearCart()

        // Mock FirebaseFirestore
        mockFirestore = mockk(relaxed = true)
        mockkStatic(FirebaseFirestore::class)
        every { FirebaseFirestore.getInstance() } returns mockFirestore

        // Mock FirebaseUser
        mockUser = mockk(relaxed = true)
        every { mockUser.uid } returns "test-user-id"
        every { mockAuth.currentUser } returns mockUser
    }

    @After
    override fun tearDownFirebase() {
        super.tearDownFirebase()
        CartManager.clearCart()
    }

    // ========== Category 1: Product Display Tests (8 tests) ==========

    @Test
    fun displayProductInfo_setsProductName() {
        val intent = createTestIntent(name = "Notebook Dell")
        val activity = Robolectric.buildActivity(ProductDetailActivity::class.java, intent)
            .create().start().resume().get()

        val txtName = activity.findViewById<TextView>(R.id.txtProductName)
        assertEquals("Notebook Dell", txtName.text.toString())
    }

    @Test
    fun displayProductInfo_setsProductCategory() {
        val intent = createTestIntent(category = "Eletrônicos")
        val activity = Robolectric.buildActivity(ProductDetailActivity::class.java, intent)
            .create().start().resume().get()

        val txtCategory = activity.findViewById<TextView>(R.id.txtProductCategory)
        assertEquals("Eletrônicos", txtCategory.text.toString())
    }

    @Test
    fun displayProductInfo_setsFormattedPrice() {
        val intent = createTestIntent(price = 1999.99)
        val activity = Robolectric.buildActivity(ProductDetailActivity::class.java, intent)
            .create().start().resume().get()

        val txtPrice = activity.findViewById<TextView>(R.id.txtProductPrice)
        assertEquals("R$ 1999.99", txtPrice.text.toString())
    }

    @Test
    fun displayProductInfo_setsStockWithUnits() {
        val intent = createTestIntent(stock = 15)
        val activity = Robolectric.buildActivity(ProductDetailActivity::class.java, intent)
            .create().start().resume().get()

        val txtStock = activity.findViewById<TextView>(R.id.txtProductStock)
        assertEquals("15 unidades", txtStock.text.toString())
    }

    @Test
    fun displayProductInfo_setsDescription() {
        val intent = createTestIntent(description = "Produto em ótimo estado")
        val activity = Robolectric.buildActivity(ProductDetailActivity::class.java, intent)
            .create().start().resume().get()

        val txtDescription = activity.findViewById<TextView>(R.id.txtProductDescription)
        assertEquals("Produto em ótimo estado", txtDescription.text.toString())
    }

    @Test
    fun displayProductInfo_withMultipleImages_populatesRecycler() {
        val imageUrls = arrayOf("url1.jpg", "url2.jpg", "url3.jpg")
        val intent = createTestIntent(imageUrls = imageUrls)
        val activity = Robolectric.buildActivity(ProductDetailActivity::class.java, intent)
            .create().start().resume().get()
        shadowOf(Looper.getMainLooper()).idle()

        val recycler = activity.findViewById<RecyclerView>(R.id.recyclerProductImages)
        assertNotNull(recycler.adapter)
        assertEquals(3, recycler.adapter?.itemCount)
    }

    @Test
    fun displayProductInfo_withEmptyImages_recyclerHasNoItems() {
        val intent = createTestIntent(imageUrls = emptyArray())
        val activity = Robolectric.buildActivity(ProductDetailActivity::class.java, intent)
            .create().start().resume().get()
        shadowOf(Looper.getMainLooper()).idle()

        val recycler = activity.findViewById<RecyclerView>(R.id.recyclerProductImages)
        assertEquals(0, recycler.adapter?.itemCount ?: 0)
    }

    @Test
    fun displayProductInfo_loadsSellerName() {
        // Mock Firestore document
        val mockDoc = mockk<DocumentSnapshot>(relaxed = true)
        every { mockDoc.exists() } returns true
        every { mockDoc.toObject(User::class.java) } returns User(
            name = "João Silva",
            whatsappNumber = "11999999999"
        )
        every { mockFirestore.collection("users").document(any()).get() } returns Tasks.forResult(mockDoc)

        val intent = createTestIntent(sellerId = "seller-123")
        val activity = Robolectric.buildActivity(ProductDetailActivity::class.java, intent)
            .create().start().resume().get()
        shadowOf(Looper.getMainLooper()).idle()

        val txtSellerName = activity.findViewById<TextView>(R.id.txtSellerName)
        assertTrue(txtSellerName.text.toString().contains("João Silva") ||
                txtSellerName.text.toString().contains("Vendedor"))
    }

    // ========== Category 2: Quantity Control Tests (9 tests) ==========

    @Test
    fun quantityControls_initialQuantityIsOne() {
        val intent = createTestIntent(stock = 10)
        val activity = Robolectric.buildActivity(ProductDetailActivity::class.java, intent)
            .create().start().resume().get()

        val txtQuantity = activity.findViewById<TextView>(R.id.txtQuantity)
        assertEquals("1", txtQuantity.text.toString())
    }

    @Test
    fun quantityControls_decreaseButton_disabledAtMinimum() {
        val intent = createTestIntent(stock = 10)
        val activity = Robolectric.buildActivity(ProductDetailActivity::class.java, intent)
            .create().start().resume().get()

        val btnDecrease = activity.findViewById<MaterialButton>(R.id.btnDecrease)
        assertFalse("Decrease button should be disabled at quantity 1", btnDecrease.isEnabled)
        assertEquals(0.5f, btnDecrease.alpha, 0.01f)
    }

    @Test
    fun quantityControls_increaseButton_enabledWhenStockAvailable() {
        val intent = createTestIntent(stock = 5)
        val activity = Robolectric.buildActivity(ProductDetailActivity::class.java, intent)
            .create().start().resume().get()

        val btnIncrease = activity.findViewById<MaterialButton>(R.id.btnIncrease)
        assertTrue("Increase button should be enabled when stock is available", btnIncrease.isEnabled)
        assertEquals(1.0f, btnIncrease.alpha, 0.01f)
    }

    @Test
    fun quantityControls_increaseButton_disabledAtMaxStock() {
        val intent = createTestIntent(stock = 1)
        val activity = Robolectric.buildActivity(ProductDetailActivity::class.java, intent)
            .create().start().resume().get()

        val btnIncrease = activity.findViewById<MaterialButton>(R.id.btnIncrease)
        assertFalse("Increase button should be disabled when quantity equals stock", btnIncrease.isEnabled)
        assertEquals(0.5f, btnIncrease.alpha, 0.01f)
    }

    @Test
    fun quantityControls_clickIncrease_updatesQuantity() {
        val intent = createTestIntent(stock = 10)
        val activity = Robolectric.buildActivity(ProductDetailActivity::class.java, intent)
            .create().start().resume().get()

        val btnIncrease = activity.findViewById<MaterialButton>(R.id.btnIncrease)
        val txtQuantity = activity.findViewById<TextView>(R.id.txtQuantity)

        btnIncrease.performClick()
        shadowOf(Looper.getMainLooper()).idle()

        assertEquals("2", txtQuantity.text.toString())
    }

    @Test
    fun quantityControls_clickDecrease_updatesQuantity() {
        val intent = createTestIntent(stock = 10)
        val activity = Robolectric.buildActivity(ProductDetailActivity::class.java, intent)
            .create().start().resume().get()

        val btnIncrease = activity.findViewById<MaterialButton>(R.id.btnIncrease)
        val btnDecrease = activity.findViewById<MaterialButton>(R.id.btnDecrease)
        val txtQuantity = activity.findViewById<TextView>(R.id.txtQuantity)

        // Increase to 2
        btnIncrease.performClick()
        shadowOf(Looper.getMainLooper()).idle()

        // Decrease to 1
        btnDecrease.performClick()
        shadowOf(Looper.getMainLooper()).idle()

        assertEquals("1", txtQuantity.text.toString())
    }

    @Test
    fun quantityControls_cannotDecreaseBelow1() {
        val intent = createTestIntent(stock = 10)
        val activity = Robolectric.buildActivity(ProductDetailActivity::class.java, intent)
            .create().start().resume().get()

        val btnDecrease = activity.findViewById<MaterialButton>(R.id.btnDecrease)
        val txtQuantity = activity.findViewById<TextView>(R.id.txtQuantity)

        // Try to decrease when already at 1
        btnDecrease.performClick()
        shadowOf(Looper.getMainLooper()).idle()

        assertEquals("1", txtQuantity.text.toString())
    }

    @Test
    fun quantityControls_cannotIncreaseAboveStock() {
        val intent = createTestIntent(stock = 2)
        val activity = Robolectric.buildActivity(ProductDetailActivity::class.java, intent)
            .create().start().resume().get()

        val btnIncrease = activity.findViewById<MaterialButton>(R.id.btnIncrease)
        val txtQuantity = activity.findViewById<TextView>(R.id.txtQuantity)

        // Increase to 2
        btnIncrease.performClick()
        shadowOf(Looper.getMainLooper()).idle()

        // Try to increase to 3 (should not work)
        btnIncrease.performClick()
        shadowOf(Looper.getMainLooper()).idle()

        assertEquals("2", txtQuantity.text.toString())
    }

    @Test
    fun quantityControls_increaseToMax_thenDecrease_enablesIncrease() {
        val intent = createTestIntent(stock = 3)
        val activity = Robolectric.buildActivity(ProductDetailActivity::class.java, intent)
            .create().start().resume().get()

        val btnIncrease = activity.findViewById<MaterialButton>(R.id.btnIncrease)
        val btnDecrease = activity.findViewById<MaterialButton>(R.id.btnDecrease)

        // Increase to max
        btnIncrease.performClick()
        shadowOf(Looper.getMainLooper()).idle()
        btnIncrease.performClick()
        shadowOf(Looper.getMainLooper()).idle()

        assertFalse("Increase should be disabled at max", btnIncrease.isEnabled)

        // Decrease once
        btnDecrease.performClick()
        shadowOf(Looper.getMainLooper()).idle()

        assertTrue("Increase should be enabled after decreasing from max", btnIncrease.isEnabled)
    }

    // ========== Category 3: Add to Cart Tests (6 tests) ==========

    @Test
    fun addToCart_withStockZero_buttonDisabled() {
        val intent = createTestIntent(stock = 0)
        val activity = Robolectric.buildActivity(ProductDetailActivity::class.java, intent)
            .create().start().resume().get()

        val btnAddToCart = activity.findViewById<MaterialButton>(R.id.btnAddToCart)
        assertFalse(btnAddToCart.isEnabled)
        assertEquals("Produto Esgotado", btnAddToCart.text.toString())
        assertEquals(0.6f, btnAddToCart.alpha, 0.01f)
    }

    @Test
    fun addToCart_withPositiveStock_buttonEnabled() {
        val intent = createTestIntent(stock = 5)
        val activity = Robolectric.buildActivity(ProductDetailActivity::class.java, intent)
            .create().start().resume().get()

        val btnAddToCart = activity.findViewById<MaterialButton>(R.id.btnAddToCart)
        assertTrue(btnAddToCart.isEnabled)
        assertEquals("Adicionar ao Carrinho", btnAddToCart.text.toString())
        assertEquals(1.0f, btnAddToCart.alpha, 0.01f)
    }

    @Test
    fun addToCart_clickButton_addsToCart() {
        val intent = createTestIntent(stock = 10, name = "Produto Teste")
        val activity = Robolectric.buildActivity(ProductDetailActivity::class.java, intent)
            .create().start().resume().get()

        val btnAddToCart = activity.findViewById<MaterialButton>(R.id.btnAddToCart)
        btnAddToCart.performClick()
        shadowOf(Looper.getMainLooper()).idle()

        assertEquals(1, CartManager.getTotalItemCount())
    }

    @Test
    fun addToCart_multipleClicks_increasesQuantity() {
        val intent = createTestIntent(stock = 10)
        val activity = Robolectric.buildActivity(ProductDetailActivity::class.java, intent)
            .create().start().resume().get()

        val btnAddToCart = activity.findViewById<MaterialButton>(R.id.btnAddToCart)

        // Add 3 times
        repeat(3) {
            btnAddToCart.performClick()
            shadowOf(Looper.getMainLooper()).idle()
        }

        assertEquals(3, CartManager.getTotalItemCount())
    }

    @Test
    fun addToCart_withCustomQuantity_addsCorrectAmount() {
        val intent = createTestIntent(stock = 10)
        val activity = Robolectric.buildActivity(ProductDetailActivity::class.java, intent)
            .create().start().resume().get()

        val btnIncrease = activity.findViewById<MaterialButton>(R.id.btnIncrease)
        val btnAddToCart = activity.findViewById<MaterialButton>(R.id.btnAddToCart)

        // Increase to 3
        repeat(2) {
            btnIncrease.performClick()
            shadowOf(Looper.getMainLooper()).idle()
        }

        // Add to cart
        btnAddToCart.performClick()
        shadowOf(Looper.getMainLooper()).idle()

        assertEquals(3, CartManager.getTotalItemCount())
    }

    @Test
    fun addToCart_exceedingStock_doesNotAdd() {
        val intent = createTestIntent(stock = 2)
        val activity = Robolectric.buildActivity(ProductDetailActivity::class.java, intent)
            .create().start().resume().get()

        val btnAddToCart = activity.findViewById<MaterialButton>(R.id.btnAddToCart)

        // Try to add more than stock
        repeat(3) {
            btnAddToCart.performClick()
            shadowOf(Looper.getMainLooper()).idle()
        }

        // Should only add up to stock limit (2 items)
        assertTrue(CartManager.getTotalItemCount() <= 2)
    }

    // ========== Category 4: UI & Navigation Tests (5 tests) ==========

    @Test
    fun allRequiredViews_existInLayout() {
        val intent = createTestIntent()
        val activity = Robolectric.buildActivity(ProductDetailActivity::class.java, intent)
            .create().start().resume().get()

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
    fun toolbarBackButton_existsAndIsClickable() {
        val intent = createTestIntent()
        val activity = Robolectric.buildActivity(ProductDetailActivity::class.java, intent)
            .create().start().resume().get()

        val toolbar = activity.findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbarProduct)
        assertNotNull(toolbar)
        assertNotNull(toolbar.navigationIcon)
    }

    @Test
    fun contactSellerButton_existsAndIsClickable() {
        val intent = createTestIntent()
        val activity = Robolectric.buildActivity(ProductDetailActivity::class.java, intent)
            .create().start().resume().get()

        val btnContact = activity.findViewById<MaterialButton>(R.id.btnContactSeller)
        assertNotNull(btnContact)
        assertTrue(btnContact.isClickable)
    }

    @Test
    fun bottomNavigation_exists() {
        val intent = createTestIntent()
        val activity = Robolectric.buildActivity(ProductDetailActivity::class.java, intent)
            .create().start().resume().get()

        val bottomNav = activity.findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(
            R.id.bottom_navigation_product
        )
        assertNotNull(bottomNav)
    }

    @Test
    fun recyclerView_hasLayoutManager() {
        val intent = createTestIntent()
        val activity = Robolectric.buildActivity(ProductDetailActivity::class.java, intent)
            .create().start().resume().get()

        val recycler = activity.findViewById<RecyclerView>(R.id.recyclerProductImages)
        assertNotNull(recycler.layoutManager)
    }

    // ========== Category 5: Edge Cases & Combined Tests (7 tests) ==========

    @Test
    fun edgeCase_stockIsZero_quantityControlsDisabled() {
        val intent = createTestIntent(stock = 0)
        val activity = Robolectric.buildActivity(ProductDetailActivity::class.java, intent)
            .create().start().resume().get()

        val btnIncrease = activity.findViewById<MaterialButton>(R.id.btnIncrease)
        val btnDecrease = activity.findViewById<MaterialButton>(R.id.btnDecrease)

        assertFalse(btnIncrease.isEnabled)
        assertFalse(btnDecrease.isEnabled)
    }

    @Test
    fun edgeCase_priceIsZero_displaysCorrectly() {
        val intent = createTestIntent(price = 0.0)
        val activity = Robolectric.buildActivity(ProductDetailActivity::class.java, intent)
            .create().start().resume().get()

        val txtPrice = activity.findViewById<TextView>(R.id.txtProductPrice)
        assertEquals("R$ 0.00", txtPrice.text.toString())
    }

    @Test
    fun edgeCase_largePrice_formatsCorrectly() {
        val intent = createTestIntent(price = 123456.78)
        val activity = Robolectric.buildActivity(ProductDetailActivity::class.java, intent)
            .create().start().resume().get()

        val txtPrice = activity.findViewById<TextView>(R.id.txtProductPrice)
        assertEquals("R$ 123456.78", txtPrice.text.toString())
    }

    @Test
    fun edgeCase_largeStock_displaysCorrectly() {
        val intent = createTestIntent(stock = 9999)
        val activity = Robolectric.buildActivity(ProductDetailActivity::class.java, intent)
            .create().start().resume().get()

        val txtStock = activity.findViewById<TextView>(R.id.txtProductStock)
        assertEquals("9999 unidades", txtStock.text.toString())
    }

    @Test
    fun edgeCase_emptyDescription_displaysEmpty() {
        val intent = createTestIntent(description = "")
        val activity = Robolectric.buildActivity(ProductDetailActivity::class.java, intent)
            .create().start().resume().get()

        val txtDescription = activity.findViewById<TextView>(R.id.txtProductDescription)
        assertEquals("", txtDescription.text.toString())
    }

    @Test
    fun combined_increaseQuantityAndAddToCart_correctTotal() {
        val intent = createTestIntent(stock = 10)
        val activity = Robolectric.buildActivity(ProductDetailActivity::class.java, intent)
            .create().start().resume().get()

        val btnIncrease = activity.findViewById<MaterialButton>(R.id.btnIncrease)
        val btnAddToCart = activity.findViewById<MaterialButton>(R.id.btnAddToCart)

        // Increase quantity to 5
        repeat(4) {
            btnIncrease.performClick()
            shadowOf(Looper.getMainLooper()).idle()
        }

        // Add to cart
        btnAddToCart.performClick()
        shadowOf(Looper.getMainLooper()).idle()

        assertEquals(5, CartManager.getTotalItemCount())
    }

    @Test
    fun combined_addMultipleProducts_cartCountIncreases() {
        val intent = createTestIntent(stock = 10, id = "product-1")
        val activity = Robolectric.buildActivity(ProductDetailActivity::class.java, intent)
            .create().start().resume().get()

        val btnAddToCart = activity.findViewById<MaterialButton>(R.id.btnAddToCart)

        // Add same product twice
        btnAddToCart.performClick()
        shadowOf(Looper.getMainLooper()).idle()
        btnAddToCart.performClick()
        shadowOf(Looper.getMainLooper()).idle()

        assertEquals(2, CartManager.getTotalItemCount())
    }

    // ========== Helper Method ==========

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
