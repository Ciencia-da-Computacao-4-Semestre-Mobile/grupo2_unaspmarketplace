package com.unasp.unaspmarketplace

import com.unasp.unaspmarketplace.models.Product
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ProductModelTest {

    @Test
    fun productDefaultConstructorWorks() {
        val product = Product()

        assertEquals("", product.id)
        assertEquals("", product.name)
        assertEquals("", product.description)
        assertEquals(0.0, product.price, 0.01)
        assertEquals("", product.category)
        assertEquals(0, product.stock)
        assertTrue(product.imageUrls.isEmpty())
        assertEquals("", product.sellerId)
        assertTrue(product.active)
        assertTrue(product.createdAt > 0)
    }

    @Test
    fun productParameterizedConstructorWorks() {
        val product = Product(
            id = "123",
            name = "Test Product",
            description = "Test Description",
            price = 99.99,
            category = "Electronics",
            stock = 10,
            imageUrls = listOf("url1", "url2"),
            sellerId = "seller123",
            active = false,
            createdAt = 1234567890L
        )

        assertEquals("123", product.id)
        assertEquals("Test Product", product.name)
        assertEquals("Test Description", product.description)
        assertEquals(99.99, product.price, 0.01)
        assertEquals("Electronics", product.category)
        assertEquals(10, product.stock)
        assertEquals(2, product.imageUrls.size)
        assertEquals("seller123", product.sellerId)
        assertFalse(product.active)
        assertEquals(1234567890L, product.createdAt)
    }

    @Test
    fun productDataClassEqualityWorks() {
        val product1 = Product(
            id = "1", name = "Test", description = "", price = 10.0,
            category = "Cat", stock = 5, active = true
        )
        val product2 = Product(
            id = "1", name = "Test", description = "", price = 10.0,
            category = "Cat", stock = 5, active = true
        )

        assertEquals(product1, product2)
    }

    @Test
    fun productDataClassCopyWorks() {
        val product1 = Product(
            id = "1", name = "Test", description = "", price = 10.0,
            category = "Cat", stock = 5, active = true
        )
        val product2 = product1.copy(name = "Modified")

        assertEquals("Test", product1.name)
        assertEquals("Modified", product2.name)
        assertEquals(product1.id, product2.id)
    }

    @Test
    fun productWithEmptyImageUrls() {
        val product = Product(
            id = "1", name = "Test", description = "", price = 10.0,
            category = "Cat", stock = 5
        )

        assertTrue(product.imageUrls.isEmpty())
    }

    @Test
    fun productWithMultipleImageUrls() {
        val imageUrls = listOf("url1", "url2", "url3")
        val product = Product(
            id = "1", name = "Test", description = "", price = 10.0,
            category = "Cat", stock = 5, imageUrls = imageUrls
        )

        assertEquals(3, product.imageUrls.size)
        assertEquals("url1", product.imageUrls[0])
        assertEquals("url2", product.imageUrls[1])
        assertEquals("url3", product.imageUrls[2])
    }

    @Test
    fun productActiveByDefault() {
        val product = Product(
            id = "1", name = "Test", description = "", price = 10.0,
            category = "Cat", stock = 5
        )

        assertTrue(product.active)
    }

    @Test
    fun productCreatedAtIsSet() {
        val beforeCreation = System.currentTimeMillis()
        val product = Product(
            id = "1", name = "Test", description = "", price = 10.0,
            category = "Cat", stock = 5
        )
        val afterCreation = System.currentTimeMillis()

        assertTrue(product.createdAt >= beforeCreation)
        assertTrue(product.createdAt <= afterCreation)
    }

    @Test
    fun productHashCodeConsistency() {
        val product = Product(
            id = "1", name = "Test", description = "", price = 10.0,
            category = "Cat", stock = 5, active = true
        )

        val hashCode1 = product.hashCode()
        val hashCode2 = product.hashCode()

        assertEquals(hashCode1, hashCode2)
    }

    @Test
    fun productToStringContainsRelevantInfo() {
        val product = Product(
            id = "1", name = "Test Product", description = "Desc", price = 10.0,
            category = "Cat", stock = 5, active = true
        )

        val toString = product.toString()
        assertTrue(toString.contains("Test Product"))
        assertTrue(toString.contains("10.0"))
    }

    @Test
    fun productsWithDifferentIdsAreNotEqual() {
        val product1 = Product(
            id = "1", name = "Test", description = "", price = 10.0,
            category = "Cat", stock = 5, active = true
        )
        val product2 = Product(
            id = "2", name = "Test", description = "", price = 10.0,
            category = "Cat", stock = 5, active = true
        )

        assertNotEquals(product1, product2)
    }

    @Test
    fun productStockCanBeZero() {
        val product = Product(
            id = "1", name = "Out of Stock", description = "", price = 10.0,
            category = "Cat", stock = 0, active = true
        )

        assertEquals(0, product.stock)
    }

    @Test
    fun productPriceCanBeZero() {
        val product = Product(
            id = "1", name = "Free Item", description = "", price = 0.0,
            category = "Cat", stock = 5, active = true
        )

        assertEquals(0.0, product.price, 0.01)
    }

    @Test
    fun productCanBeInactive() {
        val product = Product(
            id = "1", name = "Inactive", description = "", price = 10.0,
            category = "Cat", stock = 5, active = false
        )

        assertFalse(product.active)
    }

    @Test
    fun productDescriptionCanBeEmpty() {
        val product = Product(
            id = "1", name = "Test", description = "", price = 10.0,
            category = "Cat", stock = 5, active = true
        )

        assertEquals("", product.description)
    }

    @Test
    fun productWithLongDescription() {
        val longDesc = "A".repeat(1000)
        val product = Product(
            id = "1", name = "Test", description = longDesc, price = 10.0,
            category = "Cat", stock = 5, active = true
        )

        assertEquals(1000, product.description.length)
    }
}

