package com.unasp.unaspmarketplace.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.unasp.unaspmarketplace.models.Product
import kotlinx.coroutines.tasks.await

class ProductRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val productsCollection = firestore.collection("products")

    companion object {
        private const val TAG = "ProductRepository"
    }

    // Salvar produto
    suspend fun saveProduct(product: Product): Result<String> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                Log.e(TAG, "Usuário não autenticado ao tentar salvar produto")
                return Result.failure(Exception("Usuário não autenticado"))
            }

            Log.d(TAG, "Salvando produto: ${product.name} para usuário: ${currentUser.uid}")

            val productData = product.copy(
                sellerId = currentUser.uid,
                createdAt = System.currentTimeMillis()
            )

            Log.d(TAG, "Dados do produto a ser salvo: $productData")

            val documentId = if (product.id.isEmpty()) {
                Log.d(TAG, "Criando novo produto...")
                val documentRef = productsCollection.add(productData).await()
                Log.d(TAG, "Produto criado com ID: ${documentRef.id}")
                documentRef.id
            } else {
                Log.d(TAG, "Atualizando produto existente: ${product.id}")
                productsCollection.document(product.id).set(productData).await()
                Log.d(TAG, "Produto atualizado com sucesso")
                product.id
            }

            Log.d(TAG, "Produto salvo com sucesso, ID: $documentId")
            Result.success(documentId)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao salvar produto: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Buscar produtos ativos
    suspend fun getActiveProducts(): Result<List<Product>> {
        return try {
            Log.d(TAG, "Iniciando busca por produtos ativos...")

            // Estratégia 1: Buscar produtos ativos
            try {
                val snapshot = productsCollection
                    .whereEqualTo("active", true)
                    .get()
                    .await()

                Log.d(TAG, "Documentos encontrados: ${snapshot.size()}")

                val products = mutableListOf<Product>()
                for (document in snapshot.documents) {
                    try {
                        Log.d(TAG, "Processando documento: ${document.id}")
                        Log.d(TAG, "Dados do documento: ${document.data}")

                        val product = document.toObject(Product::class.java)?.copy(id = document.id)
                        if (product != null) {
                            products.add(product)
                            Log.d(TAG, "Produto adicionado: ${product.name}")
                        } else {
                            Log.w(TAG, "Falha ao converter documento ${document.id} em Product")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Erro ao processar documento ${document.id}: ${e.message}", e)
                    }
                }

                Log.d(TAG, "Total de produtos processados: ${products.size}")
                return Result.success(products)

            } catch (e1: Exception) {
                Log.e(TAG, "Erro na estratégia 1: ${e1.message}", e1)

                // Estratégia 2: Buscar todos os produtos e filtrar localmente
                try {
                    Log.d(TAG, "Tentando estratégia 2: buscar todos os produtos...")
                    val allSnapshot = productsCollection.get().await()
                    Log.d(TAG, "Total de documentos na coleção: ${allSnapshot.size()}")

                    val allProducts = mutableListOf<Product>()
                    for (document in allSnapshot.documents) {
                        try {
                            val product = document.toObject(Product::class.java)?.copy(id = document.id)
                            if (product != null) {
                                allProducts.add(product)
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Erro ao processar documento na estratégia 2: ${e.message}")
                        }
                    }

                    val activeProducts = allProducts.filter { it.active }
                    Log.d(TAG, "Produtos ativos filtrados: ${activeProducts.size}")
                    return Result.success(activeProducts)

                } catch (e2: Exception) {
                    Log.e(TAG, "Erro na estratégia 2: ${e2.message}", e2)
                    return Result.failure(Exception("Falha ao carregar produtos: ${e1.message} | ${e2.message}"))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro geral na busca de produtos: ${e.message}", e)
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
