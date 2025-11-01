package com.unasp.unaspmarketplace.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.unasp.unaspmarketplace.models.Product
import kotlinx.coroutines.tasks.await

class ProductRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val productsCollection = firestore.collection("products")

    // Salvar produto
    suspend fun saveProduct(product: Product): Result<String> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                return Result.failure(Exception("Usuário não autenticado"))
            }

            val productData = product.copy(
                sellerId = currentUser.uid,
                sellerName = currentUser.displayName ?: "Usuário"
            )

            val documentRef = if (product.id.isEmpty()) {
                productsCollection.add(productData).await()
            } else {
                productsCollection.document(product.id).set(productData).await()
                productsCollection.document(product.id)
            }

            Result.success(documentRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Buscar produtos ativos
    suspend fun getActiveProducts(): Result<List<Product>> {
        return try {
            // Estratégia 1: Buscar produtos ativos
            try {
                val snapshot = productsCollection
                    .whereEqualTo("active", true)
                    .get()
                    .await()

                val products = snapshot.toObjects(Product::class.java)
                return Result.success(products)
            } catch (e1: Exception) {
                // Estratégia 2: Buscar todos os produtos e filtrar localmente
                try {
                    val allSnapshot = productsCollection.get().await()
                    val allProducts = allSnapshot.toObjects(Product::class.java)
                    val activeProducts = allProducts.filter { it.active }
                    return Result.success(activeProducts)
                } catch (e2: Exception) {
                    // Estratégia 3: Buscar apenas por documento específico se soubermos o ID
                    return Result.failure(Exception("Falha ao carregar produtos: ${e1.message} | ${e2.message}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Buscar produtos por categoria
    suspend fun getProductsByCategory(category: String): Result<List<Product>> {
        return try {
            val snapshot = productsCollection
                .whereEqualTo("active", true)
                .whereEqualTo("category", category)
                .get()
                .await()

            val products = snapshot.toObjects(Product::class.java)
            Result.success(products)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Buscar produtos do usuário
    suspend fun getUserProducts(userId: String): Result<List<Product>> {
        return try {
            val snapshot = productsCollection
                .whereEqualTo("sellerId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val products = snapshot.toObjects(Product::class.java)
            Result.success(products)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Deletar produto
    suspend fun deleteProduct(productId: String): Result<Unit> {
        return try {
            productsCollection.document(productId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Buscar produto por ID
    suspend fun getProductById(productId: String): Result<Product?> {
        return try {
            val snapshot = productsCollection.document(productId).get().await()
            val product = snapshot.toObject(Product::class.java)
            Result.success(product)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
