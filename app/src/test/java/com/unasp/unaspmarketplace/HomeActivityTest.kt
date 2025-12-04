package com.unasp.unaspmarketplace

import android.os.Looper
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.RecyclerView
import com.unasp.unaspmarketplace.models.Product
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.ShadowToast
import android.view.View
import kotlin.compareTo
import kotlin.text.category
import kotlin.text.clear
import kotlin.text.get
import kotlin.toString

@RunWith(RobolectricTestRunner::class)
class HomeActivityTest : BaseFirebaseTest() {

    private lateinit var activity: HomeActivity

    @Before
    override fun setupFirebase() {
        super.setupFirebase()
        // Create activity with minimal setup
        activity = Robolectric.buildActivity(HomeActivity::class.java)
            .create()
            .start()
            .resume()
            .get()
        shadowOf(Looper.getMainLooper()).idle()
    }

    @After
    override fun tearDownFirebase() {
        // Clear products to free memory
        getAllProducts().clear()
        getFilteredProducts().clear()
        super.tearDownFirebase()
    }

    // Combine related tests to reduce activity recreation

    @Test
    fun searchProducts_priceFilters_workCorrectly() {
        // Arrange
        val products = createLightweightProducts() // Reduce product count
        setAllProducts(products)

        // Test 1: Less than
        callSearchProducts("até 100")
        shadowOf(Looper.getMainLooper()).idle()
        assertTrue(getFilteredProducts().all { it.price <= 100.0 })

        // Test 2: Greater than
        callSearchProducts("maior que 100")
        shadowOf(Looper.getMainLooper()).idle()
        assertTrue(getFilteredProducts().all { it.price >= 100.0 })

        // Test 3: Range
        callSearchProducts("entre 50 e 200")
        shadowOf(Looper.getMainLooper()).idle()
        assertTrue(getFilteredProducts().all { it.price in 50.0..200.0 })
    }

    @Test
    fun extractPrice_handlesVariousInputs() {
        // Combine multiple extraction tests
        assertEquals(100.0, callExtractPrice("até 100")!!, 0.01)
        assertEquals(50.5, callExtractPrice("menor que 50.5")!!, 0.01)
        assertNull(callExtractPrice("sem numero"))
    }

    @Test
    fun extractPriceRange_handlesVariousInputs() {
        // Test valid ranges
        val range1 = callExtractPriceRange("entre 50 e 200")
        assertNotNull(range1)
        assertEquals(50.0, range1!!.first, 0.01)
        assertEquals(200.0, range1.second, 0.01)

        // Test invalid input
        assertNull(callExtractPriceRange("invalido"))
    }

    @Test
    fun searchHistory_functionsCorrectly() {
        // Combine save, retrieve, limit, and clear tests

        // Save
        callSaveSearchToHistory("Test1")
        callSaveSearchToHistory("Test2")
        var history = callGetSearchHistory()
        assertEquals(2, history.size)

        // Limit to 10
        for (i in 3..12) {
            callSaveSearchToHistory("Test$i")
        }
        history = callGetSearchHistory()
        assertEquals(10, history.size)
        assertFalse(history.contains("Test1"))

        // Clear
        callClearSearchHistory()
        assertTrue(callGetSearchHistory().isEmpty())
    }

    @Test
    fun viewVisibility_togglesWithSearch() {
        val searchView = activity.findViewById<SearchView>(R.id.searchView)
        val banner = activity.findViewById<ImageView>(R.id.bannerPromo)
        val categories = activity.findViewById<RecyclerView>(R.id.recyclerCategorys)

        // Submit query - should hide
        searchView.setQuery("test", true)
        shadowOf(Looper.getMainLooper()).idle()
        assertEquals(View.GONE, banner.visibility)
        assertEquals(View.GONE, categories.visibility)

        // Clear query - should show
        searchView.setQuery("", false)
        shadowOf(Looper.getMainLooper()).idle()
        assertEquals(View.VISIBLE, banner.visibility)
        assertEquals(View.VISIBLE, categories.visibility)
    }

    // Simplified helper for lightweight products (only 2 instead of 4)
    private fun createLightweightProducts(): List<Product> {
        return listOf(
            Product(
                name = "Notebook",
                description = "Test",
                price = 150.0,
                category = "Eletrônicos",
                stock = 1,
                active = true
            ),
            Product(
                name = "Mouse",
                description = "Test",
                price = 50.0,
                category = "Eletrônicos",
                stock = 1,
                active = true
            )
        )
    }

    // Keep essential helper methods (remove unused ones)

    private fun getAllProducts(): MutableList<Product> {
        val field = HomeActivity::class.java.getDeclaredField("allProducts")
        field.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        return field.get(activity) as MutableList<Product>
    }

    private fun setAllProducts(products: List<Product>) {
        val field = HomeActivity::class.java.getDeclaredField("allProducts")
        field.isAccessible = true
        val list = field.get(activity) as MutableList<Product>
        list.clear()
        list.addAll(products)
    }

    private fun getFilteredProducts(): MutableList<Product> {
        val field = HomeActivity::class.java.getDeclaredField("filteredProducts")
        field.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        return field.get(activity) as MutableList<Product>
    }

    private fun callSearchProducts(query: String?) {
        val method = HomeActivity::class.java.getDeclaredMethod("searchProducts", String::class.java)
        method.isAccessible = true
        method.invoke(activity, query)
    }

    private fun callExtractPrice(query: String): Double? {
        val method = HomeActivity::class.java.getDeclaredMethod("extractPrice", String::class.java)
        method.isAccessible = true
        return method.invoke(activity, query) as? Double
    }

    private fun callExtractPriceRange(query: String): Pair<Double, Double>? {
        val method = HomeActivity::class.java.getDeclaredMethod("extractPriceRange", String::class.java)
        method.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        return method.invoke(activity, query) as? Pair<Double, Double>
    }

    private fun callSaveSearchToHistory(query: String) {
        val method = HomeActivity::class.java.getDeclaredMethod("saveSearchToHistory", String::class.java)
        method.isAccessible = true
        method.invoke(activity, query)
    }

    private fun callGetSearchHistory(): Set<String> {
        val method = HomeActivity::class.java.getDeclaredMethod("getSearchHistory")
        method.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        return method.invoke(activity) as Set<String>
    }

    private fun callClearSearchHistory() {
        val method = HomeActivity::class.java.getDeclaredMethod("clearSearchHistory")
        method.isAccessible = true
        method.invoke(activity)
    }
}
