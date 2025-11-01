package com.unasp.unaspmarketplace.utils

import com.unasp.unaspmarketplace.models.Product

data class CartItem(
    val product: Product,
    var quantity: Int = 1
) {
    val totalPrice: Double
        get() = product.price * quantity
}

object CartManager {
    private val cartItems = mutableListOf<CartItem>()
    private val listeners = mutableListOf<CartUpdateListener>()

    interface CartUpdateListener {
        fun onCartUpdated(itemCount: Int, totalPrice: Double)
    }

    fun addListener(listener: CartUpdateListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: CartUpdateListener) {
        listeners.remove(listener)
    }

    private fun notifyListeners() {
        val itemCount = getTotalItemCount()
        val totalPrice = getTotalPrice()
        listeners.forEach { it.onCartUpdated(itemCount, totalPrice) }
    }

    fun addToCart(product: Product, quantity: Int = 1): Boolean {
        // Verificar se produto já existe no carrinho
        val existingItem = cartItems.find { it.product.id == product.id }

        if (existingItem != null) {
            // Aumentar quantidade se já existe
            val newQuantity = existingItem.quantity + quantity
            if (newQuantity <= product.stock) {
                existingItem.quantity = newQuantity
                notifyListeners()
                return true
            } else {
                return false // Estoque insuficiente
            }
        } else {
            // Adicionar novo item
            if (quantity <= product.stock) {
                cartItems.add(CartItem(product, quantity))
                notifyListeners()
                return true
            } else {
                return false // Estoque insuficiente
            }
        }
    }

    fun removeFromCart(productId: String) {
        cartItems.removeAll { it.product.id == productId }
        notifyListeners()
    }

    fun updateQuantity(productId: String, newQuantity: Int): Boolean {
        val item = cartItems.find { it.product.id == productId }
        return if (item != null && newQuantity <= item.product.stock && newQuantity > 0) {
            item.quantity = newQuantity
            notifyListeners()
            true
        } else {
            false
        }
    }

    fun clearCart() {
        cartItems.clear()
        notifyListeners()
    }

    fun getCartItems(): List<CartItem> = cartItems.toList()

    fun getTotalItemCount(): Int = cartItems.sumOf { it.quantity }

    fun getTotalPrice(): Double = cartItems.sumOf { it.totalPrice }

    fun isInCart(productId: String): Boolean = cartItems.any { it.product.id == productId }

    fun getItemQuantity(productId: String): Int =
        cartItems.find { it.product.id == productId }?.quantity ?: 0
}
