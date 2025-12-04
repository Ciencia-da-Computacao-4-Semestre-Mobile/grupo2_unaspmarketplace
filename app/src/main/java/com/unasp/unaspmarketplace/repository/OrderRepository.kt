package com.unasp.unaspmarketplace.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.unasp.unaspmarketplace.models.Order
import com.unasp.unaspmarketplace.models.OrderStatus
import kotlinx.coroutines.tasks.await

/**
 * Repositório para gerenciar pedidos no Firebase
 */
class OrderRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val ordersCollection = firestore.collection("orders")

    companion object {
        private const val TAG = "OrderRepository"
    }

    /**
     * Cria um novo pedido
     */
    suspend fun createOrder(order: Order): Result<String> {
        return try {
            Log.d(TAG, "=== INICIANDO CRIAÇÃO DE PEDIDO ===")
            Log.d(TAG, "Dados do pedido: $order")
            Log.d(TAG, "Criando pedido: ${order.buyerName} -> ${order.sellerName}")
            Log.d(TAG, "BuyerId: ${order.buyerId}")
            Log.d(TAG, "SellerId: ${order.sellerId}")
            Log.d(TAG, "Items: ${order.items.size}")
            Log.d(TAG, "Total: ${order.totalAmount}")

            if (order.buyerId.isEmpty()) {
                Log.e(TAG, "Erro: buyerId está vazio")
                return Result.failure(IllegalArgumentException("buyerId não pode estar vazio"))
            }

            if (order.sellerId.isEmpty()) {
                Log.e(TAG, "Erro: sellerId está vazio")
                return Result.failure(IllegalArgumentException("sellerId não pode estar vazio"))
            }

            if (order.items.isEmpty()) {
                Log.e(TAG, "Erro: lista de items está vazia")
                return Result.failure(IllegalArgumentException("Lista de items não pode estar vazia"))
            }

            val orderData = order.copy(
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                status = OrderStatus.PENDING.name
            )

            Log.d(TAG, "Dados finais do pedido: $orderData")
            Log.d(TAG, "Adicionando documento à coleção 'orders'...")

            val documentRef = ordersCollection.add(orderData).await()
            val orderId = documentRef.id

            Log.d(TAG, "Pedido criado com sucesso: $orderId")
            Log.d(TAG, "=== PEDIDO CRIADO COM SUCESSO ===")
            Result.success(orderId)
        } catch (e: Exception) {
            Log.e(TAG, "=== ERRO AO CRIAR PEDIDO ===")
            Log.e(TAG, "Tipo de erro: ${e.javaClass.simpleName}")
            Log.e(TAG, "Mensagem: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Busca pedidos do comprador (histórico de compras)
     */
    suspend fun getBuyerOrders(buyerId: String): Result<List<Order>> {
        return try {
            Log.d(TAG, "Buscando pedidos do comprador: $buyerId")

            // Removendo orderBy temporariamente para evitar erro de índice
            // TODO: Criar índice composto no Firestore Console para buyerId + createdAt
            val snapshot = ordersCollection
                .whereEqualTo("buyerId", buyerId)
                .get()
                .await()

            val orders = mutableListOf<Order>()
            for (document in snapshot.documents) {
                val order = document.toObject(Order::class.java)?.copy(id = document.id)
                order?.let { orders.add(it) }
            }

            // Ordenar manualmente por data de criação (mais recente primeiro)
            val sortedOrders = orders.sortedByDescending { it.createdAt }

            Log.d(TAG, "Encontrados ${orders.size} pedidos para o comprador")
            Result.success(sortedOrders)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao buscar pedidos do comprador: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Busca pedidos do vendedor (pedidos recebidos)
     */
    suspend fun getSellerOrders(sellerId: String): Result<List<Order>> {
        return try {
            Log.d(TAG, "Buscando pedidos do vendedor: $sellerId")

            // Removendo orderBy temporariamente para evitar erro de índice
            // TODO: Criar índice composto no Firestore Console para sellerId + createdAt
            val snapshot = ordersCollection
                .whereEqualTo("sellerId", sellerId)
                .get()
                .await()

            val orders = mutableListOf<Order>()
            for (document in snapshot.documents) {
                val order = document.toObject(Order::class.java)?.copy(id = document.id)
                order?.let { orders.add(it) }
            }

            // Ordenar manualmente por data de criação (mais recente primeiro)
            val sortedOrders = orders.sortedByDescending { it.createdAt }

            Log.d(TAG, "Encontrados ${orders.size} pedidos para o vendedor")
            Result.success(sortedOrders)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao buscar pedidos do vendedor: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Atualiza o status de um pedido
     */
    suspend fun updateOrderStatus(orderId: String, newStatus: OrderStatus): Result<Unit> {
        return try {
            Log.d(TAG, "Atualizando status do pedido $orderId para $newStatus")

            val updateData = mutableMapOf<String, Any>(
                "status" to newStatus.name,
                "updatedAt" to System.currentTimeMillis()
            )

            // Se for concluído, adicionar data de conclusão
            if (newStatus == OrderStatus.COMPLETED) {
                updateData["completedAt"] = System.currentTimeMillis()
            }

            ordersCollection.document(orderId)
                .update(updateData)
                .await()

            Log.d(TAG, "Status do pedido atualizado com sucesso")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao atualizar status do pedido: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Busca um pedido específico por ID
     */
    suspend fun getOrderById(orderId: String): Result<Order?> {
        return try {
            Log.d(TAG, "Buscando pedido: $orderId")

            val snapshot = ordersCollection.document(orderId).get().await()
            val order = snapshot.toObject(Order::class.java)?.copy(id = snapshot.id)

            Result.success(order)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao buscar pedido: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Busca pedidos pendentes do vendedor
     */
    suspend fun getPendingSellerOrders(sellerId: String): Result<List<Order>> {
        return try {
            Log.d(TAG, "Buscando pedidos pendentes do vendedor: $sellerId")

            // Removendo orderBy temporariamente para evitar erro de índice
            // TODO: Criar índice composto no Firestore Console para sellerId + status + createdAt
            val snapshot = ordersCollection
                .whereEqualTo("sellerId", sellerId)
                .whereEqualTo("status", OrderStatus.PENDING.name)
                .get()
                .await()

            val orders = mutableListOf<Order>()
            for (document in snapshot.documents) {
                val order = document.toObject(Order::class.java)?.copy(id = document.id)
                order?.let { orders.add(it) }
            }

            // Ordenar manualmente por data de criação (mais recente primeiro)
            val sortedOrders = orders.sortedByDescending { it.createdAt }

            Log.d(TAG, "Encontrados ${orders.size} pedidos pendentes")
            Result.success(sortedOrders)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao buscar pedidos pendentes: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Cancela um pedido
     */
    suspend fun cancelOrder(orderId: String, reason: String = ""): Result<Unit> {
        return try {
            Log.d(TAG, "Cancelando pedido: $orderId")

            val updateData = mapOf(
                "status" to OrderStatus.CANCELLED.name,
                "updatedAt" to System.currentTimeMillis(),
                "notes" to reason
            )

            ordersCollection.document(orderId)
                .update(updateData)
                .await()

            Log.d(TAG, "Pedido cancelado com sucesso")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao cancelar pedido: ${e.message}", e)
            Result.failure(e)
        }
    }
}
