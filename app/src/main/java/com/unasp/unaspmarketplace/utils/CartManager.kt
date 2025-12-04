package com.unasp.unaspmarketplace.utils

import com.unasp.unaspmarketplace.models.Product
import kotlin.collections.remove
import kotlin.text.clear

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
    private val actionListeners = mutableListOf<CartActionListener>()

    // Listener para atualização de contagem de itens e preço total
    interface CartUpdateListener {
        fun onCartUpdated(itemCount: Int, totalPrice: Double)
    }

    // Listener para eventos de ação (remoção de itens, limpeza do carrinho)
    interface CartActionListener {
        fun onItemsRemoved(removed: List<CartItem>) {}
        fun onCartCleared(removed: List<CartItem>) {}
    }

    fun addListener(listener: CartUpdateListener) { listeners.add(listener) }

    fun removeListener(listener: CartUpdateListener) { listeners.remove(listener) }

    fun addActionListener(listener: CartActionListener) { actionListeners.add(listener) }

    fun removeActionListener(listener: CartActionListener) { actionListeners.remove(listener) }

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

    // Helper de deleção com notificação
    private fun removeWhere(predicate: (CartItem) -> Boolean): List<CartItem> {
        val removed = cartItems.filter(predicate)
        if (removed.isEmpty()) return emptyList()
        cartItems.removeAll(predicate)
        notifyListeners()
        return removed
    }

    fun removeFromCart(productId: String) {
        val removed = removeWhere { it.product.id == productId }
        if (removed.isNotEmpty()) {
            actionListeners.forEach { it.onItemsRemoved(removed) }
        }
    }

    fun updateQuantity(productId: String, newQuantity: Int): Boolean {
        val item = cartItems.find { it.product.id == productId }
        return if (item != null && newQuantity <= item.product.stock && newQuantity > 0) {
            item.quantity = newQuantity
            notifyListeners()
            true
        } else if (item != null && newQuantity <= 0) {
            removeFromCart(productId)
            true
        } else {
            false
        }
    }

    fun clearCart() {
        if (cartItems.isEmpty()) return
        val removedSnapshot = cartItems.map { it.copy() }
        cartItems.clear()
        notifyListeners()
        actionListeners.forEach { it.onCartCleared(removedSnapshot) }
    }

    fun getCartItems(): List<CartItem> = cartItems.toList()

    fun getTotalItemCount(): Int = cartItems.sumOf { it.quantity }

    fun getTotalPrice(): Double = cartItems.sumOf { it.totalPrice }

    fun isInCart(productId: String): Boolean = cartItems.any { it.product.id == productId }

    fun getItemQuantity(productId: String): Int =
        cartItems.find { it.product.id == productId }?.quantity ?: 0
}
