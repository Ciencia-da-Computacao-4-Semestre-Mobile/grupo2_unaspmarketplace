package com.unasp.unaspmarketplace

import com.unasp.unaspmarketplace.models.Product
import com.unasp.unaspmarketplace.utils.CartItem
import com.unasp.unaspmarketplace.utils.CartManager
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CartManagerTest {

    @Before
    fun setup() {
        CartManager.clearCart()
    }

    @Test
    fun cartItemTotalPriceCalculatedCorrectly() {
        val product = Product(
            id = "p1", name = "Test", description = "", price = 10.0,
            category = "Cat", stock = 5, active = true
        )
        val cartItem = CartItem(product, 3)

        assertEquals(30.0, cartItem.totalPrice, 0.01)
    }

    @Test
    fun addToCartIncreasesItemCount() {
        val product = Product(
            id = "p1", name = "Test", description = "", price = 10.0,
            category = "Cat", stock = 5, active = true
        )

        CartManager.addToCart(product, 2)

        assertEquals(1, CartManager.getCartItems().size)
        assertEquals(2, CartManager.getTotalItemCount())
    }

    @Test
    fun addingDuplicateProductIncreasesQuantity() {
        val product = Product(
            id = "p1", name = "Test", description = "", price = 10.0,
            category = "Cat", stock = 10, active = true
        )

        CartManager.addToCart(product, 2)
        CartManager.addToCart(product, 3)

        assertEquals(1, CartManager.getCartItems().size)
        assertEquals(5, CartManager.getTotalItemCount())
    }

    @Test
    fun addToCartReturnsFalseWhenStockInsufficient() {
        val product = Product(
            id = "p1", name = "Test", description = "", price = 10.0,
            category = "Cat", stock = 3, active = true
        )

        val result = CartManager.addToCart(product, 5)

        assertFalse(result)
        assertTrue(CartManager.getCartItems().isEmpty())
    }

    @Test
    fun removeFromCartRemovesItem() {
        val product = Product(
            id = "p1", name = "Test", description = "", price = 10.0,
            category = "Cat", stock = 5, active = true
        )

        CartManager.addToCart(product, 2)
        CartManager.removeFromCart("p1")

        assertTrue(CartManager.getCartItems().isEmpty())
    }

    @Test
    fun updateQuantityWorks() {
        val product = Product(
            id = "p1", name = "Test", description = "", price = 10.0,
            category = "Cat", stock = 10, active = true
        )

        CartManager.addToCart(product, 2)
        val result = CartManager.updateQuantity("p1", 5)

        assertTrue(result)
        assertEquals(5, CartManager.getCartItems()[0].quantity)
    }

    @Test
    fun updateQuantityReturnsFalseForInsufficientStock() {
        val product = Product(
            id = "p1", name = "Test", description = "", price = 10.0,
            category = "Cat", stock = 3, active = true
        )

        CartManager.addToCart(product, 2)
        val result = CartManager.updateQuantity("p1", 5)

        assertFalse(result)
        assertEquals(2, CartManager.getCartItems()[0].quantity)
    }

    @Test
    fun clearCartRemovesAllItems() {
        val product1 = Product(
            id = "p1", name = "Test1", description = "", price = 10.0,
            category = "Cat", stock = 5, active = true
        )
        val product2 = Product(
            id = "p2", name = "Test2", description = "", price = 20.0,
            category = "Cat", stock = 5, active = true
        )

        CartManager.addToCart(product1, 1)
        CartManager.addToCart(product2, 1)
        CartManager.clearCart()

        assertTrue(CartManager.getCartItems().isEmpty())
    }

    @Test
    fun getTotalPriceCalculatesCorrectly() {
        val product1 = Product(
            id = "p1", name = "Test1", description = "", price = 10.0,
            category = "Cat", stock = 5, active = true
        )
        val product2 = Product(
            id = "p2", name = "Test2", description = "", price = 20.0,
            category = "Cat", stock = 5, active = true
        )

        CartManager.addToCart(product1, 2)
        CartManager.addToCart(product2, 3)

        assertEquals(80.0, CartManager.getTotalPrice(), 0.01)
    }

    @Test
    fun isInCartReturnsCorrectValue() {
        val product = Product(
            id = "p1", name = "Test", description = "", price = 10.0,
            category = "Cat", stock = 5, active = true
        )

        assertFalse(CartManager.isInCart("p1"))
        CartManager.addToCart(product, 1)
        assertTrue(CartManager.isInCart("p1"))
    }

    @Test
    fun getItemQuantityReturnsCorrectValue() {
        val product = Product(
            id = "p1", name = "Test", description = "", price = 10.0,
            category = "Cat", stock = 5, active = true
        )

        assertEquals(0, CartManager.getItemQuantity("p1"))
        CartManager.addToCart(product, 3)
        assertEquals(3, CartManager.getItemQuantity("p1"))
    }

    @Test
    fun listenerNotifiedOnAddToCart() {
        var notified = false
        var notifiedItemCount = 0
        var notifiedTotalPrice = 0.0

        val listener = object : CartManager.CartUpdateListener {
            override fun onCartUpdated(itemCount: Int, totalPrice: Double) {
                notified = true
                notifiedItemCount = itemCount
                notifiedTotalPrice = totalPrice
            }
        }

        CartManager.addListener(listener)

        val product = Product(
            id = "p1", name = "Test", description = "", price = 10.0,
            category = "Cat", stock = 5, active = true
        )
        CartManager.addToCart(product, 2)

        assertTrue(notified)
        assertEquals(2, notifiedItemCount)
        assertEquals(20.0, notifiedTotalPrice, 0.01)

        CartManager.removeListener(listener)
    }

    @Test
    fun actionListenerNotifiedOnRemove() {
        var wasRemoved = false
        val listener = object : CartManager.CartActionListener {
            override fun onItemsRemoved(removed: List<CartItem>) {
                wasRemoved = true
            }
        }

        CartManager.addActionListener(listener)

        val product = Product(
            id = "p1", name = "Test", description = "", price = 10.0,
            category = "Cat", stock = 5, active = true
        )
        CartManager.addToCart(product, 1)
        CartManager.removeFromCart("p1")

        CartManager.removeActionListener(listener)
        assertTrue(wasRemoved)
    }

    @Test
    fun actionListenerNotifiedOnClearCart() {
        var wasCleared = false
        val listener = object : CartManager.CartActionListener {
            override fun onCartCleared(removed: List<CartItem>) {
                wasCleared = true
            }
        }

        CartManager.addActionListener(listener)

        val product = Product(
            id = "p1", name = "Test", description = "", price = 10.0,
            category = "Cat", stock = 5, active = true
        )
        CartManager.addToCart(product, 1)
        CartManager.clearCart()

        CartManager.removeActionListener(listener)
        assertTrue(wasCleared)
    }

    @Test
    fun emptyCartReturnsZeroTotalPrice() {
        assertEquals(0.0, CartManager.getTotalPrice(), 0.01)
    }

    @Test
    fun emptyCartReturnsZeroItemCount() {
        assertEquals(0, CartManager.getTotalItemCount())
    }

    @Test
    fun getCartItemsReturnsEmptyListWhenCartIsEmpty() {
        assertTrue(CartManager.getCartItems().isEmpty())
    }

    @Test
    fun multipleListenersReceiveUpdates() {
        var listener1Called = false
        var listener2Called = false

        val listener1 = object : CartManager.CartUpdateListener {
            override fun onCartUpdated(itemCount: Int, totalPrice: Double) {
                listener1Called = true
            }
        }

        val listener2 = object : CartManager.CartUpdateListener {
            override fun onCartUpdated(itemCount: Int, totalPrice: Double) {
                listener2Called = true
            }
        }

        CartManager.addListener(listener1)
        CartManager.addListener(listener2)

        val product = Product(
            id = "p1", name = "Test", description = "", price = 10.0,
            category = "Cat", stock = 5, active = true
        )
        CartManager.addToCart(product, 1)

        assertTrue(listener1Called)
        assertTrue(listener2Called)

        CartManager.removeListener(listener1)
        CartManager.removeListener(listener2)
    }

    @Test
    fun removeNonExistentItemDoesNothing() {
        CartManager.removeFromCart("non_existent")
        assertTrue(CartManager.getCartItems().isEmpty())
    }

    @Test
    fun updateQuantityOfNonExistentItemReturnsFalse() {
        val result = CartManager.updateQuantity("non_existent", 5)
        assertFalse(result)
    }
}

