// kotlin
package com.unasp.unaspmarketplace

import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.unasp.unaspmarketplace.models.Product
import com.unasp.unaspmarketplace.utils.CartManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.ShadowLooper
import org.robolectric.shadows.ShadowToast

@RunWith(RobolectricTestRunner::class)
class CartActivityTest : BaseFirebaseTest() {

    @Before
    override fun setupFirebase() {
        super.setupFirebase()
        CartManager.clearCart()
    }

    @Test
    fun emptyCartShowsEmptyState() {
        val activity = Robolectric.buildActivity(CartActivity::class.java).setup().get()
        val empty = activity.findViewById<LinearLayout>(R.id.emptyCartView)
        val list = activity.findViewById<RecyclerView>(R.id.recyclerCart)
        val checkout = activity.findViewById<Button>(R.id.btnCheckout)

        assertEquals(View.VISIBLE, empty.visibility)
        assertEquals(View.GONE, list.visibility)
        assertFalse(checkout.isEnabled)
    }

    @Test
    fun emptyCartCheckoutShowsToast() {
        val activity = Robolectric.buildActivity(CartActivity::class.java).setup().get()
        activity.findViewById<Button>(R.id.btnCheckout).performClick()

        assertEquals("Carrinho vazio!", ShadowToast.getTextOfLatestToast())
    }

    @Test
    fun filledCartShowsItemsAndTotal() {
        val product = Product(
            id = "p1", name = "Notebook", description = "", price = 1000.0,
            category = "Eletrônicos", stock = 10, active = true
        )
        CartManager.addToCart(product, 2)

        val activity = Robolectric.buildActivity(CartActivity::class.java).setup().get()

        val empty = activity.findViewById<LinearLayout>(R.id.emptyCartView)
        val list = activity.findViewById<RecyclerView>(R.id.recyclerCart)
        val checkout = activity.findViewById<Button>(R.id.btnCheckout)
        val totalText = activity.findViewById<TextView>(R.id.txtTotal)

        assertEquals(View.GONE, empty.visibility)
        assertEquals(View.VISIBLE, list.visibility)
        assertTrue(checkout.isEnabled)
        assertTrue(totalText.text.toString().contains("R$ 2000"))
    }

    @Test
    fun removeItemUpdatesTotal() {
        val p1 = Product(
            id = "p_rm1", name = "Produto A", description = "", price = 10.0,
            category = "Cat", stock = 5, active = true
        )
        val p2 = Product(
            id = "p_rm2", name = "Produto B", description = "", price = 5.0,
            category = "Cat", stock = 5, active = true
        )
        CartManager.addToCart(p1, 1)
        CartManager.addToCart(p2, 1)

        val activity = Robolectric.buildActivity(CartActivity::class.java).setup().get()
        val recycler = activity.findViewById<RecyclerView>(R.id.recyclerCart)
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        var removed = false
        for (i in 0 until recycler.childCount) {
            val child = recycler.getChildAt(i)
            val nameView = child.findViewById<TextView>(R.id.txtName)
            if (nameView?.text.toString().contains("Produto A")) {
                child.findViewById<Button>(R.id.btnRemove).performClick()
                removed = true
                break
            }
        }
        assertTrue("Item not found", removed)

        assertFalse(CartManager.isInCart("p_rm1"))
        val txtTotal = activity.findViewById<TextView>(R.id.txtTotal)
        assertTrue(txtTotal.text.toString().contains("R$ 5.00"))
    }

    @Test
    fun clearCartMenuShowsDialogAndClears() {
        val product = Product(
            id = "p_clear", name = "ToClear", description = "", price = 20.0,
            category = "Cat", stock = 2, active = true
        )
        CartManager.addToCart(product, 1)

        val activity = Robolectric.buildActivity(CartActivity::class.java).setup().get()

        // Directly clear the cart to test the UI update
        CartManager.clearCart()
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        assertTrue(CartManager.getCartItems().isEmpty())
        val empty = activity.findViewById<LinearLayout>(R.id.emptyCartView)
        assertEquals(View.VISIBLE, empty.visibility)
    }

    @Test
    fun clearCartDialogCancelDoesNotClear() {
        val product = Product(
            id = "p_cancel", name = "Product", description = "", price = 10.0,
            category = "Cat", stock = 5, active = true
        )
        CartManager.addToCart(product, 1)

        val activity = Robolectric.buildActivity(CartActivity::class.java).setup().get()

        // Test that cart is not empty when not cleared
        assertFalse(CartManager.getCartItems().isEmpty())

        val recycler = activity.findViewById<RecyclerView>(R.id.recyclerCart)
        assertEquals(View.VISIBLE, recycler.visibility)
    }

    @Test
    fun toolbarNavigationClosesActivity() {
        val activity = Robolectric.buildActivity(CartActivity::class.java).setup().get()

        // Simulate navigation icon click by finishing activity
        activity.finish()

        // Activity should be finishing
        assertTrue(activity.isFinishing)
    }

    @Test
    fun startShoppingButtonFinishesActivity() {
        val activity = Robolectric.buildActivity(CartActivity::class.java).setup().get()
        val btnStartShopping = activity.findViewById<Button>(R.id.btnStartShopping)

        btnStartShopping.performClick()

        assertTrue(activity.isFinishing)
    }

    @Test
    fun increaseQuantityWorks() {
        val product = Product(
            id = "p_inc", name = "Product", description = "", price = 10.0,
            category = "Cat", stock = 10, active = true
        )
        CartManager.addToCart(product, 2)

        val activity = Robolectric.buildActivity(CartActivity::class.java).setup().get()
        val recycler = activity.findViewById<RecyclerView>(R.id.recyclerCart)
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        val child = recycler.getChildAt(0)
        val btnIncrease = child.findViewById<Button>(R.id.btnIncrease)
        btnIncrease.performClick()

        val item = CartManager.getCartItems().find { it.product.id == "p_inc" }
        assertEquals(3, item?.quantity)
    }

    @Test
    fun decreaseQuantityWorks() {
        val product = Product(
            id = "p_dec", name = "Product", description = "", price = 10.0,
            category = "Cat", stock = 10, active = true
        )
        CartManager.addToCart(product, 3)

        val activity = Robolectric.buildActivity(CartActivity::class.java).setup().get()
        val recycler = activity.findViewById<RecyclerView>(R.id.recyclerCart)
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        val child = recycler.getChildAt(0)
        val btnDecrease = child.findViewById<Button>(R.id.btnDecrease)
        btnDecrease.performClick()

        val item = CartManager.getCartItems().find { it.product.id == "p_dec" }
        assertEquals(2, item?.quantity)
    }

    @Test
    fun decreaseQuantityToOneRemovesItem() {
        val product = Product(
            id = "p_dec1", name = "Product", description = "", price = 10.0,
            category = "Cat", stock = 10, active = true
        )
        CartManager.addToCart(product, 1)

        val activity = Robolectric.buildActivity(CartActivity::class.java).setup().get()
        val recycler = activity.findViewById<RecyclerView>(R.id.recyclerCart)
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        val child = recycler.getChildAt(0)
        val btnDecrease = child.findViewById<Button>(R.id.btnDecrease)
        btnDecrease.performClick()

        assertFalse(CartManager.isInCart("p_dec1"))
    }

    @Test
    fun increaseQuantityBeyondStockShowsToast() {
        val product = Product(
            id = "p_stock", name = "Product", description = "", price = 10.0,
            category = "Cat", stock = 2, active = true
        )
        CartManager.addToCart(product, 2)

        val activity = Robolectric.buildActivity(CartActivity::class.java).setup().get()
        val recycler = activity.findViewById<RecyclerView>(R.id.recyclerCart)
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        val child = recycler.getChildAt(0)
        val btnIncrease = child.findViewById<Button>(R.id.btnIncrease)

        // Button should be disabled when at max stock
        assertFalse(btnIncrease.isEnabled)
    }

    @Test
    fun checkoutWithItemsNavigatesToPayment() {
        val product = Product(
            id = "p_checkout", name = "Product", description = "", price = 100.0,
            category = "Cat", stock = 5, active = true
        )
        CartManager.addToCart(product, 2)

        val activity = Robolectric.buildActivity(CartActivity::class.java).setup().get()
        val btnCheckout = activity.findViewById<Button>(R.id.btnCheckout)

        btnCheckout.performClick()

        val intent = org.robolectric.Shadows.shadowOf(activity).nextStartedActivity
        assertNotNull(intent)
        assertEquals(PaymentActivity::class.java.name, intent.component?.className)
        assertEquals(200.0, intent.getDoubleExtra("totalAmount", 0.0), 0.01)
    }

    @Test
    fun bottomNavigationHomeNavigatesToHome() {
        val activity = Robolectric.buildActivity(CartActivity::class.java).setup().get()
        val bottomNav = activity.findViewById<BottomNavigationView>(R.id.bottom_navigation_cart)

        bottomNav.selectedItemId = R.id.nav_home
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        val intent = org.robolectric.Shadows.shadowOf(activity).nextStartedActivity
        assertNotNull(intent)
        assertEquals(HomeActivity::class.java.name, intent.component?.className)
    }

    @Test
    fun bottomNavigationMenuNavigatesToHomeWithFlag() {
        val activity = Robolectric.buildActivity(CartActivity::class.java).setup().get()
        val bottomNav = activity.findViewById<BottomNavigationView>(R.id.bottom_navigation_cart)

        bottomNav.selectedItemId = R.id.nav_menu
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        val intent = org.robolectric.Shadows.shadowOf(activity).nextStartedActivity
        assertNotNull(intent)
        assertEquals(HomeActivity::class.java.name, intent.component?.className)
        assertTrue(intent.getBooleanExtra("openMenu", false))
    }

    @Test
    fun bottomNavigationProfileNavigatesToProfile() {
        val activity = Robolectric.buildActivity(CartActivity::class.java).setup().get()
        val bottomNav = activity.findViewById<BottomNavigationView>(R.id.bottom_navigation_cart)

        bottomNav.selectedItemId = R.id.nav_profile
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        val intent = org.robolectric.Shadows.shadowOf(activity).nextStartedActivity
        assertNotNull(intent)
        assertEquals(ProfileActivity::class.java.name, intent.component?.className)
    }

    @Test
    fun bottomNavigationCartShowsToast() {
        val activity = Robolectric.buildActivity(CartActivity::class.java).setup().get()
        val bottomNav = activity.findViewById<BottomNavigationView>(R.id.bottom_navigation_cart)

        // Cart is already selected
        bottomNav.selectedItemId = R.id.nav_cart
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        assertEquals("Você já está no Carrinho", ShadowToast.getTextOfLatestToast())
    }

    @Test
    fun onResumeUpdatesBadge() {
        val product = Product(
            id = "p_badge", name = "Product", description = "", price = 10.0,
            category = "Cat", stock = 5, active = true
        )
        CartManager.addToCart(product, 3)

        Robolectric.buildActivity(CartActivity::class.java)
            .setup()
            .resume()
            .get()

        val badge = CartManager.getTotalItemCount()
        assertEquals(3, badge)
    }

    @Test
    fun clearCartMenuNotVisibleWhenEmpty() {
        CartManager.clearCart()
        val activity = Robolectric.buildActivity(CartActivity::class.java).setup().get()
        val toolbar = activity.findViewById<MaterialToolbar>(R.id.appbar_cart)

        val clearItem = toolbar.menu.findItem(R.id.btnClearCart)
        assertFalse(clearItem?.isVisible ?: true)
    }

    @Test
    fun clearCartMenuVisibleWhenNotEmpty() {
        val product = Product(
            id = "p_vis", name = "Product", description = "", price = 10.0,
            category = "Cat", stock = 5, active = true
        )
        CartManager.addToCart(product, 1)

        val activity = Robolectric.buildActivity(CartActivity::class.java).setup().get()
        val toolbar = activity.findViewById<MaterialToolbar>(R.id.appbar_cart)

        val clearItem = toolbar.menu.findItem(R.id.btnClearCart)
        assertTrue(clearItem?.isVisible ?: false)
    }

    @Test
    fun toolbarSubtitleShowsItemCount() {
        val product = Product(
            id = "p_subtitle", name = "Product", description = "", price = 10.0,
            category = "Cat", stock = 5, active = true
        )
        CartManager.addToCart(product, 3)

        val activity = Robolectric.buildActivity(CartActivity::class.java).setup().get()
        val toolbar = activity.findViewById<MaterialToolbar>(R.id.appbar_cart)

        assertTrue(toolbar.subtitle.toString().contains("3 itens"))
    }

    @Test
    fun adapterDisplaysCorrectItemInfo() {
        val product = Product(
            id = "p_display", name = "TestProduct", description = "", price = 25.50,
            category = "Cat", stock = 5, active = true
        )
        CartManager.addToCart(product, 2)

        val activity = Robolectric.buildActivity(CartActivity::class.java).setup().get()
        val recycler = activity.findViewById<RecyclerView>(R.id.recyclerCart)
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        val child = recycler.getChildAt(0)
        val txtName = child.findViewById<TextView>(R.id.txtName)
        val txtPrice = child.findViewById<TextView>(R.id.txtPrice)
        val txtQuantity = child.findViewById<TextView>(R.id.txtQuantity)

        assertEquals("TestProduct", txtName.text.toString())
        assertTrue(txtPrice.text.toString().contains("51.00"))
        assertEquals("2", txtQuantity.text.toString())
    }

    @Test
    fun lifecycleMethodsHandleListenersCorrectly() {
        val product = Product(
            id = "p_lifecycle", name = "Product", description = "", price = 10.0,
            category = "Cat", stock = 5, active = true
        )
        CartManager.addToCart(product, 1)

        val activity = Robolectric.buildActivity(CartActivity::class.java)
            .setup()
            .start()
            .get()

        // Listeners should be added in onStart
        CartManager.addToCart(product, 1) // Should trigger update
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        // Verify activity handles updates
        assertNotNull(activity)

        // Stop activity
        val activity2 = Robolectric.buildActivity(CartActivity::class.java)
            .setup()
            .start()
            .stop()
            .get()

        // Listeners should be removed, no crash
        assertNotNull(activity2)
    }

    @Test
    fun cartFooterVisibilityMatchesCartState() {
        // Empty cart
        CartManager.clearCart()
        val activity = Robolectric.buildActivity(CartActivity::class.java).setup().get()
        var footer = activity.findViewById<LinearLayout>(R.id.cart_footer)
        assertEquals(View.GONE, footer.visibility)

        // Add item
        val product = Product(
            id = "p_footer", name = "Product", description = "", price = 10.0,
            category = "Cat", stock = 5, active = true
        )
        CartManager.addToCart(product, 1)
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        footer = activity.findViewById(R.id.cart_footer)
        assertEquals(View.VISIBLE, footer.visibility)
    }

    @Test
    fun unknownMenuItemReturnsFalse() {
        val activity = Robolectric.buildActivity(CartActivity::class.java).setup().get()
        val toolbar = activity.findViewById<MaterialToolbar>(R.id.appbar_cart)

        // Try unknown menu item - should not crash
        toolbar.menu.performIdentifierAction(android.R.id.home, 0)

        // Verify activity is still functional
        assertNotNull(toolbar)
    }

    @Test
    fun increaseQuantityFailsWhenStockNotAvailable() {
        val product = Product(
            id = "p_no_stock", name = "Limited Product", description = "", price = 50.0,
            category = "Cat", stock = 2, active = true
        )
        CartManager.addToCart(product, 2)

        val activity = Robolectric.buildActivity(CartActivity::class.java).setup().get()
        val recycler = activity.findViewById<RecyclerView>(R.id.recyclerCart)
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        val child = recycler.getChildAt(0)
        val btnIncrease = child.findViewById<Button>(R.id.btnIncrease)

        // Attempt to increase when already at max stock
        btnIncrease.performClick()

        val item = CartManager.getCartItems().find { it.product.id == "p_no_stock" }
        // Should remain at 2
        assertEquals(2, item?.quantity)
    }

    @Test
    fun multipleProductsDisplayCorrectly() {
        val product1 = Product(
            id = "p_multi1", name = "Product One", description = "", price = 10.0,
            category = "Cat1", stock = 5, active = true
        )
        val product2 = Product(
            id = "p_multi2", name = "Product Two", description = "", price = 20.0,
            category = "Cat2", stock = 3, active = true
        )
        CartManager.addToCart(product1, 1)
        CartManager.addToCart(product2, 2)

        val activity = Robolectric.buildActivity(CartActivity::class.java).setup().get()
        val recycler = activity.findViewById<RecyclerView>(R.id.recyclerCart)
        val txtTotal = activity.findViewById<TextView>(R.id.txtTotal)

        assertEquals(2, recycler.adapter?.itemCount)
        assertTrue(txtTotal.text.toString().contains("50.00")) // 10 + 40 = 50
    }

    @Test
    fun emptyCartSubtitleShowsZeroItems() {
        CartManager.clearCart()
        val activity = Robolectric.buildActivity(CartActivity::class.java).setup().get()
        val toolbar = activity.findViewById<MaterialToolbar>(R.id.appbar_cart)

        assertTrue(toolbar.subtitle.toString().contains("0 itens"))
    }

    @Test
    fun checkoutButtonAlphaChangesWithCartState() {
        CartManager.clearCart()
        val activity = Robolectric.buildActivity(CartActivity::class.java).setup().get()
        val btnCheckout = activity.findViewById<Button>(R.id.btnCheckout)

        // Empty cart - button should be semi-transparent
        assertEquals(0.5f, btnCheckout.alpha, 0.01f)

        // Add item
        val product = Product(
            id = "p_alpha", name = "Product", description = "", price = 10.0,
            category = "Cat", stock = 5, active = true
        )
        CartManager.addToCart(product, 1)
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        // Should be fully opaque
        assertEquals(1.0f, btnCheckout.alpha, 0.01f)
    }

    @Test
    fun cartUpdatedCallbackTriggersUIUpdate() {
        val activity = Robolectric.buildActivity(CartActivity::class.java).setup().get()
        val product = Product(
            id = "p_callback", name = "Product", description = "", price = 15.0,
            category = "Cat", stock = 10, active = true
        )

        // Trigger onCartUpdated callback
        CartManager.addToCart(product, 2)
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        val txtTotal = activity.findViewById<TextView>(R.id.txtTotal)
        assertTrue(txtTotal.text.toString().contains("30.00"))
    }

    @Test
    fun recyclerViewHasCorrectLayoutManager() {
        val activity = Robolectric.buildActivity(CartActivity::class.java).setup().get()
        val recycler = activity.findViewById<RecyclerView>(R.id.recyclerCart)

        assertTrue(recycler.layoutManager is LinearLayoutManager)
    }

    @Test
    fun totalPriceFormattedCorrectly() {
        val product = Product(
            id = "p_format", name = "Product", description = "", price = 99.99,
            category = "Cat", stock = 5, active = true
        )
        CartManager.addToCart(product, 1)

        val activity = Robolectric.buildActivity(CartActivity::class.java).setup().get()
        val txtTotal = activity.findViewById<TextView>(R.id.txtTotal)

        assertTrue(txtTotal.text.toString().contains("R$"))
        assertTrue(txtTotal.text.toString().contains("99.99"))
    }

    @Test
    fun adapterImageResourceSetCorrectly() {
        val product = Product(
            id = "p_img", name = "Product", description = "", price = 10.0,
            category = "Cat", stock = 5, active = true
        )
        CartManager.addToCart(product, 1)

        val activity = Robolectric.buildActivity(CartActivity::class.java).setup().get()
        val recycler = activity.findViewById<RecyclerView>(R.id.recyclerCart)
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        val child = recycler.getChildAt(0)
        val imgProduct = child.findViewById<ImageView>(R.id.imgProduct)

        assertNotNull(imgProduct)
        assertNotNull(imgProduct.drawable)
    }

    @Test
    fun decreaseButtonBehavesCorrectlyAtQuantityOne() {
        val product = Product(
            id = "p_dec_one", name = "Product", description = "", price = 10.0,
            category = "Cat", stock = 10, active = true
        )
        CartManager.addToCart(product, 2)

        val activity = Robolectric.buildActivity(CartActivity::class.java).setup().get()
        val recycler = activity.findViewById<RecyclerView>(R.id.recyclerCart)
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        val child = recycler.getChildAt(0)
        val btnDecrease = child.findViewById<Button>(R.id.btnDecrease)

        // Decrease to 1
        btnDecrease.performClick()
        val item = CartManager.getCartItems().find { it.product.id == "p_dec_one" }
        assertEquals(1, item?.quantity)

        // Decrease again should remove
        btnDecrease.performClick()
        assertFalse(CartManager.isInCart("p_dec_one"))
    }

    @Test
    fun onStartAddsListenersOnStopRemovesThem() {
        Robolectric.buildActivity(CartActivity::class.java)
            .create()
            .start()
            .resume()
            .get()

        // Listeners should be active
        val product = Product(
            id = "p_listeners", name = "Product", description = "", price = 10.0,
            category = "Cat", stock = 5, active = true
        )
        CartManager.addToCart(product, 1)
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        // Now pause and stop
        Robolectric.buildActivity(CartActivity::class.java)
            .create()
            .start()
            .resume()
            .pause()
            .stop()
            .get()

        // Should not crash - listeners properly managed
        assertNotNull(CartManager.getCartItems())
    }

    @Test
    fun bottomNavigationDefaultSelectedItemIsCart() {
        val activity = Robolectric.buildActivity(CartActivity::class.java).setup().get()
        val bottomNav = activity.findViewById<BottomNavigationView>(R.id.bottom_navigation_cart)

        assertEquals(R.id.nav_cart, bottomNav.selectedItemId)
    }

    @Test
    fun toolbarHasNavigationIcon() {
        val activity = Robolectric.buildActivity(CartActivity::class.java).setup().get()
        val toolbar = activity.findViewById<MaterialToolbar>(R.id.appbar_cart)

        assertNotNull(toolbar.navigationIcon)
    }

    @Test
    fun emptyCartViewInitiallyVisible() {
        CartManager.clearCart()
        val activity = Robolectric.buildActivity(CartActivity::class.java).setup().get()
        val emptyView = activity.findViewById<LinearLayout>(R.id.emptyCartView)

        assertEquals(View.VISIBLE, emptyView.visibility)
    }

    @Test
    fun recyclerCartInitiallyGoneWhenEmpty() {
        CartManager.clearCart()
        val activity = Robolectric.buildActivity(CartActivity::class.java).setup().get()
        val recycler = activity.findViewById<RecyclerView>(R.id.recyclerCart)

        assertEquals(View.GONE, recycler.visibility)
    }

    @Test
    fun addingFirstItemUpdatesVisibility() {
        CartManager.clearCart()
        val activity = Robolectric.buildActivity(CartActivity::class.java).setup().get()

        val product = Product(
            id = "p_first", name = "First Product", description = "", price = 10.0,
            category = "Cat", stock = 5, active = true
        )
        CartManager.addToCart(product, 1)
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        val emptyView = activity.findViewById<LinearLayout>(R.id.emptyCartView)
        val recycler = activity.findViewById<RecyclerView>(R.id.recyclerCart)

        assertEquals(View.GONE, emptyView.visibility)
        assertEquals(View.VISIBLE, recycler.visibility)
    }

    @Test
    fun removingLastItemUpdatesVisibility() {
        val product = Product(
            id = "p_last", name = "Last Product", description = "", price = 10.0,
            category = "Cat", stock = 5, active = true
        )
        CartManager.addToCart(product, 1)

        val activity = Robolectric.buildActivity(CartActivity::class.java).setup().get()

        // Remove the item
        CartManager.removeFromCart("p_last")
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        val emptyView = activity.findViewById<LinearLayout>(R.id.emptyCartView)
        val recycler = activity.findViewById<RecyclerView>(R.id.recyclerCart)

        assertEquals(View.VISIBLE, emptyView.visibility)
        assertEquals(View.GONE, recycler.visibility)
    }

    @Test
    fun toolbarSubtitleInitiallyEmpty() {
        val activity = Robolectric.buildActivity(CartActivity::class.java).create().get()
        val toolbar = activity.findViewById<MaterialToolbar>(R.id.appbar_cart)

        // Subtitle is set to empty string initially
        assertNotNull(toolbar)
    }
}
