package com.unasp.unaspmarketplace.utils

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.unasp.unaspmarketplace.models.Product
import kotlinx.coroutines.tasks.await

/**
 * Utilitário para verificar se as imagens dos produtos estão sendo corretamente exibidas
 */
object ProductImageVerifier {
    private const val TAG = "ProductImageVerifier"
    private val firestore = FirebaseFirestore.getInstance()

    /**
     * Verifica se todas as imagens de um produto específico são válidas
     */
    suspend fun verifyProductImages(productId: String): ImageVerificationResult {
        return try {
            Log.d(TAG, "🔍 Verificando imagens do produto: $productId")

            val productSnapshot = firestore.collection("products")
                .document(productId)
                .get()
                .await()

            if (!productSnapshot.exists()) {
                Log.w(TAG, "❌ Produto não encontrado: $productId")
                return ImageVerificationResult.ProductNotFound
            }

            val product = productSnapshot.toObject(Product::class.java)
            if (product == null) {
                Log.e(TAG, "❌ Erro ao converter produto: $productId")
                return ImageVerificationResult.ConversionError
            }

            val imageUrls = product.imageUrls
            Log.d(TAG, "📸 Produto tem ${imageUrls.size} imagens")

            if (imageUrls.isEmpty()) {
                Log.w(TAG, "⚠️ Produto sem imagens: $productId")
                return ImageVerificationResult.NoImages
            }

            val validImages = mutableListOf<String>()
            val invalidImages = mutableListOf<String>()

            for ((index, imageUrl) in imageUrls.withIndex()) {
                if (isValidImageUrl(imageUrl)) {
                    validImages.add(imageUrl)
                    Log.d(TAG, "✅ Imagem $index válida: $imageUrl")
                } else {
                    invalidImages.add(imageUrl)
                    Log.w(TAG, "❌ Imagem $index inválida: $imageUrl")
                }
            }

            ImageVerificationResult.Success(
                totalImages = imageUrls.size,
                validImages = validImages,
                invalidImages = invalidImages,
                product = product
            )
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro ao verificar imagens do produto $productId", e)
            ImageVerificationResult.Error(e.message ?: "Erro desconhecido")
        }
    }

    /**
     * Verifica se uma URL de imagem é válida
     */
    private fun isValidImageUrl(url: String): Boolean {
        return url.isNotEmpty() &&
               (url.startsWith("https://") || url.startsWith("http://")) &&
               (url.contains("firebase") || url.contains("googleapis") || url.contains("storage.cloud"))
    }

    /**
     * Verifica todas as imagens de todos os produtos de um usuário
     */
    suspend fun verifyUserProductImages(userId: String): UserImageVerificationResult {
        return try {
            Log.d(TAG, "🔍 Verificando imagens de todos os produtos do usuário: $userId")

            val productsSnapshot = firestore.collection("products")
                .whereEqualTo("sellerId", userId)
                .get()
                .await()

            val results = mutableListOf<ImageVerificationResult>()

            for (document in productsSnapshot.documents) {
                val result = verifyProductImages(document.id)
                results.add(result)
            }

            val totalProducts = results.size
            val productsWithImages = results.count {
                it is ImageVerificationResult.Success && it.totalImages > 0
            }
            val productsWithoutImages = results.count {
                it is ImageVerificationResult.NoImages
            }
            val productsWithErrors = results.count {
                it is ImageVerificationResult.Error || it is ImageVerificationResult.ConversionError
            }

            Log.d(TAG, "📊 Resumo: $totalProducts produtos, $productsWithImages com imagens, $productsWithoutImages sem imagens, $productsWithErrors com erros")

            UserImageVerificationResult(
                totalProducts = totalProducts,
                productsWithImages = productsWithImages,
                productsWithoutImages = productsWithoutImages,
                productsWithErrors = productsWithErrors,
                detailedResults = results
            )
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro ao verificar imagens do usuário $userId", e)
            UserImageVerificationResult(
                totalProducts = 0,
                productsWithImages = 0,
                productsWithoutImages = 0,
                productsWithErrors = 1,
                detailedResults = listOf(ImageVerificationResult.Error(e.message ?: "Erro desconhecido"))
            )
        }
    }

    /**
     * Gera um relatório detalhado sobre as imagens de um produto
     */
    fun generateImageReport(result: ImageVerificationResult): String {
        return when (result) {
            is ImageVerificationResult.Success -> {
                buildString {
                    appendLine("📊 RELATÓRIO DE IMAGENS")
                    appendLine("─────────────────────")
                    appendLine("✅ Status: Produto verificado")
                    appendLine("📱 Total de imagens: ${result.totalImages}")
                    appendLine("✅ Imagens válidas: ${result.validImages.size}")
                    appendLine("❌ Imagens inválidas: ${result.invalidImages.size}")
                    appendLine()

                    if (result.validImages.isNotEmpty()) {
                        appendLine("✅ IMAGENS VÁLIDAS:")
                        result.validImages.forEachIndexed { index, url ->
                            appendLine("${index + 1}. ${url.take(50)}...")
                        }
                        appendLine()
                    }

                    if (result.invalidImages.isNotEmpty()) {
                        appendLine("❌ IMAGENS INVÁLIDAS:")
                        result.invalidImages.forEachIndexed { index, url ->
                            appendLine("${index + 1}. ${url.take(50)}...")
                        }
                    }
                }
            }
            is ImageVerificationResult.NoImages -> {
                "⚠️ PRODUTO SEM IMAGENS\n\nEste produto não possui nenhuma imagem cadastrada."
            }
            is ImageVerificationResult.ProductNotFound -> {
                "❌ PRODUTO NÃO ENCONTRADO\n\nO produto especificado não foi encontrado no banco de dados."
            }
            is ImageVerificationResult.ConversionError -> {
                "❌ ERRO DE CONVERSÃO\n\nNão foi possível converter os dados do produto."
            }
            is ImageVerificationResult.Error -> {
                "❌ ERRO NA VERIFICAÇÃO\n\n${result.message}"
            }
        }
    }
}

/**
 * Resultado da verificação de imagens de um produto
 */
sealed class ImageVerificationResult {
    data class Success(
        val totalImages: Int,
        val validImages: List<String>,
        val invalidImages: List<String>,
        val product: Product
    ) : ImageVerificationResult()

    object NoImages : ImageVerificationResult()
    object ProductNotFound : ImageVerificationResult()
    object ConversionError : ImageVerificationResult()
    data class Error(val message: String) : ImageVerificationResult()
}

/**
 * Resultado da verificação de imagens de todos os produtos de um usuário
 */
data class UserImageVerificationResult(
    val totalProducts: Int,
    val productsWithImages: Int,
    val productsWithoutImages: Int,
    val productsWithErrors: Int,
    val detailedResults: List<ImageVerificationResult>
)
