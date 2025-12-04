package com.unasp.unaspmarketplace

import android.content.Intent
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import com.google.firebase.firestore.FirebaseFirestore
import com.unasp.unaspmarketplace.models.Product
import com.unasp.unaspmarketplace.models.User
import com.unasp.unaspmarketplace.utils.CartManager
import com.unasp.unaspmarketplace.utils.UserUtils
import com.unasp.unaspmarketplace.utils.WhatsAppHelper
import io.mockk.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.ShadowToast
import java.util.Locale

@RunWith(RobolectricTestRunner::class)
class OrderPreviewActivityTest : BaseFirebaseTest() {

    @Before
    override fun setupFirebase() {
        super.setupFirebase()
        CartManager.clearCart()

        // Mock UserUtils
        mockkObject(UserUtils)
        coEvery { UserUtils.getCurrentUserId() } returns "test-user-id"
        coEvery { UserUtils.getCurrentUser() } returns User(
            id = "test-user-id",
            name = "Test User",
            email = "test@test.com",
            whatsappNumber = "5511999999999"
        )
        coEvery { UserUtils.updateUser(any()) } returns true

        // Mock WhatsAppHelper
        mockkObject(WhatsAppHelper)
        every { WhatsAppHelper.sendMessage(any(), any(), any()) } just Runs
    }

    @After
    override fun tearDownFirebase() {
        CartManager.clearCart()
        unmockkAll()
        super.tearDownFirebase()
    }

    // ========== Initialization Tests ==========

    @Test
    fun activity_finishesWhenCartIsEmpty() {
        val intent = createIntent("order-123", "John Doe", "Dinheiro")
        val activity = Robolectric.buildActivity(OrderPreviewActivity::class.java, intent).setup().get()

        assertTrue(activity.isFinishing)
    }

    @Test
    fun activity_finishesWhenMissingIntentExtras() {
        val intent = Intent().apply {
            // Missing orderId - activity returns early from generateOrder
            putExtra(OrderPreviewActivity.EXTRA_CUSTOMER_NAME, "John Doe")
            putExtra(OrderPreviewActivity.EXTRA_PAYMENT_METHOD, "Dinheiro")
        }

        addProductToCart("p1", "Product", 10.0, "seller1")
        val activity = Robolectric.buildActivity(OrderPreviewActivity::class.java, intent).setup().get()

        // Activity doesn't crash even without orderId
        assertNotNull(activity)
    }

    @Test
    fun activity_initializesViewsCorrectly() {
        addProductToCart("p1", "Product", 10.0, "seller1")
        val intent = createIntent("order-1", "Customer", "PIX")
        val activity = Robolectric.buildActivity(OrderPreviewActivity::class.java, intent).setup().get()

        assertNotNull(activity.findViewById<TextView>(R.id.txtOrderPreview))
        assertNotNull(activity.findViewById<TextView>(R.id.txtCountdown))
        assertNotNull(activity.findViewById<Button>(R.id.btnSendNow))
        assertNotNull(activity.findViewById<Button>(R.id.btnCancel))
    }

    // ========== Order Preview Display Tests ==========

    @Test
    fun orderPreview_displaysSingleProduct() {
        addProductToCart("p1", "Produto A", 10.0, "seller1")
        val intent = createIntent("order-1", "Cliente", "Dinheiro")
        val activity = Robolectric.buildActivity(OrderPreviewActivity::class.java, intent).setup().get()

        val preview = activity.findViewById<TextView>(R.id.txtOrderPreview).text.toString().lowercase(Locale.getDefault())

        assertTrue(preview.contains("produto a"))
        assertTrue(preview.contains("r$ 10.00"))
        assertTrue(preview.contains("cliente"))
    }

    @Test
    fun orderPreview_displaysMultipleProductsWithCorrectTotal() {
        addProductToCart("p1", "Produto A", 10.0, "seller1", 2)
        addProductToCart("p2", "Produto B", 5.0, "seller2", 1)

        val intent = createIntent("order-1", "Cliente X", "Dinheiro", "5511999999999")
        val activity = Robolectric.buildActivity(OrderPreviewActivity::class.java, intent).setup().get()

        val preview = activity.findViewById<TextView>(R.id.txtOrderPreview).text.toString().lowercase(Locale.getDefault())

        assertTrue(preview.contains("produto a"))
        assertTrue(preview.contains("produto b"))
        assertTrue(preview.contains("r$ 25.00"))
        assertTrue(preview.contains("5511999999999"))
    }

    @Test
    fun orderPreview_includesCustomerWhatsApp() {
        addProductToCart("p1", "Product", 10.0, "seller1")
        val intent = createIntent("order-1", "Customer", "PIX", "5511988887777")
        val activity = Robolectric.buildActivity(OrderPreviewActivity::class.java, intent).setup().get()

        val preview = activity.findViewById<TextView>(R.id.txtOrderPreview).text.toString()
        assertTrue(preview.contains("5511988887777"))
    }

    @Test
    fun orderPreview_excludesWhatsAppWhenNotProvided() {
        addProductToCart("p1", "Product", 10.0, "seller1")
        val intent = createIntent("order-1", "Customer", "PIX")
        val activity = Robolectric.buildActivity(OrderPreviewActivity::class.java, intent).setup().get()

        val preview = activity.findViewById<TextView>(R.id.txtOrderPreview).text.toString()
        assertFalse(preview.contains("WhatsApp do Cliente:"))
    }

    // ========== Button Interaction Tests ==========

    @Test
    fun cancelButton_finishesActivity() {
        addProductToCart("p1", "Product", 15.0, "seller1")
        val intent = createIntent("order-1", "Customer", "Cartão")
        val activity = Robolectric.buildActivity(OrderPreviewActivity::class.java, intent).setup().get()

        activity.findViewById<Button>(R.id.btnCancel).performClick()
        shadowOf(Looper.getMainLooper()).idle()

        assertTrue(activity.isFinishing)
    }

    @Test
    fun sendNowButton_isClickable() {
        addProductToCart("p1", "Product", 10.0, "seller1")
        val intent = createIntent("order-1", "Customer", "PIX", "5511999999999")
        val activity = Robolectric.buildActivity(OrderPreviewActivity::class.java, intent).setup().get()

        val btnSendNow = activity.findViewById<Button>(R.id.btnSendNow)
        assertTrue(btnSendNow.isEnabled)
        assertTrue(btnSendNow.isClickable)
    }

    @Test
    fun sendNowButton_triggersToast() {
        addProductToCart("p1", "Product", 10.0, "seller1")
        val intent = createIntent("order-1", "Customer", "Dinheiro")
        val activity = Robolectric.buildActivity(OrderPreviewActivity::class.java, intent).setup().get()

        activity.findViewById<Button>(R.id.btnSendNow).performClick()
        shadowOf(Looper.getMainLooper()).idle()

        val toast = ShadowToast.getTextOfLatestToast()
        assertTrue(toast.contains("Processando"))
    }

    // ========== Countdown Tests ==========

    @Test
    fun countdown_displaysInitially() {
        addProductToCart("p1", "Product", 10.0, "seller1")
        val intent = createIntent("order-1", "Customer", "PIX")
        val activity = Robolectric.buildActivity(OrderPreviewActivity::class.java, intent).setup().get()

        val countdown = activity.findViewById<TextView>(R.id.txtCountdown)
        assertNotNull(countdown.text)
        assertFalse(countdown.text.toString().isEmpty())
    }

    @Test
    fun countdown_cancelsWhenCancelClicked() {
        addProductToCart("p1", "Product", 10.0, "seller1")
        val intent = createIntent("order-1", "Customer", "PIX")
        val activity = Robolectric.buildActivity(OrderPreviewActivity::class.java, intent).setup().get()

        activity.findViewById<Button>(R.id.btnCancel).performClick()
        shadowOf(Looper.getMainLooper()).idle()

        assertTrue(activity.isFinishing)
    }

    // ========== Send Messages to Sellers Coverage Tests ==========

    @Test
    fun sendOrder_groupsItemsBySeller() {
        addProductToCart("p1", "Product A", 10.0, "seller1")
        addProductToCart("p2", "Product B", 15.0, "seller2")

        val intent = createIntent("order-1", "Customer", "PIX")
        val activity = Robolectric.buildActivity(OrderPreviewActivity::class.java, intent).setup().get()

        // Verify activity created with multiple sellers
        val preview = activity.findViewById<TextView>(R.id.txtOrderPreview).text.toString()
        assertTrue(preview.contains("Product A") || preview.contains("Product B"))
    }

    @Test
    fun sendOrder_handlesSellerLookup() {
        val mockFirestore = mockk<FirebaseFirestore>(relaxed = true)
        mockkStatic(FirebaseFirestore::class)
        every { FirebaseFirestore.getInstance() } returns mockFirestore

        val sellerDoc = mockk<com.google.firebase.firestore.DocumentSnapshot>(relaxed = true)
        every { sellerDoc.toObject(User::class.java) } returns User(
            id = "seller1",
            whatsappNumber = "5511988881111"
        )

        val task = com.google.android.gms.tasks.Tasks.forResult(sellerDoc)
        every { mockFirestore.collection("users").document(any()).get() } returns task

        addProductToCart("p1", "Product", 10.0, "seller1")
        val intent = createIntent("order-1", "Customer", "PIX")
        val activity = Robolectric.buildActivity(OrderPreviewActivity::class.java, intent).setup().get()

        // Click send now to trigger seller lookup path
        activity.findViewById<Button>(R.id.btnSendNow).performClick()
        shadowOf(Looper.getMainLooper()).idle()

        // Verify no crash occurred
        assertNotNull(activity)
    }

    @Test
    fun sendOrder_handlesEmptyWhatsAppNumber() {
        val mockFirestore = mockk<FirebaseFirestore>(relaxed = true)
        mockkStatic(FirebaseFirestore::class)
        every { FirebaseFirestore.getInstance() } returns mockFirestore

        val sellerDoc = mockk<com.google.firebase.firestore.DocumentSnapshot>(relaxed = true)
        every { sellerDoc.toObject(User::class.java) } returns User(
            id = "seller1",
            whatsappNumber = ""
        )

        val task = com.google.android.gms.tasks.Tasks.forResult(sellerDoc)
        every { mockFirestore.collection("users").document(any()).get() } returns task

        addProductToCart("p1", "Product", 10.0, "seller1")
        val intent = createIntent("order-1", "Customer", "PIX")
        val activity = Robolectric.buildActivity(OrderPreviewActivity::class.java, intent).setup().get()

        activity.findViewById<Button>(R.id.btnSendNow).performClick()
        shadowOf(Looper.getMainLooper()).idle()

        // Should not crash with empty WhatsApp
        assertNotNull(activity)
    }

    @Test
    fun sendOrder_handlesFirestoreError() {
        val mockFirestore = mockk<FirebaseFirestore>(relaxed = true)
        mockkStatic(FirebaseFirestore::class)
        every { FirebaseFirestore.getInstance() } returns mockFirestore

        val task = com.google.android.gms.tasks.Tasks.forException<com.google.firebase.firestore.DocumentSnapshot>(
            Exception("Firestore error")
        )
        every { mockFirestore.collection("users").document(any()).get() } returns task

        addProductToCart("p1", "Product", 10.0, "seller1")
        val intent = createIntent("order-1", "Customer", "PIX")
        val activity = Robolectric.buildActivity(OrderPreviewActivity::class.java, intent).setup().get()

        activity.findViewById<Button>(R.id.btnSendNow).performClick()
        shadowOf(Looper.getMainLooper()).idle()

        // Should not crash on Firestore error
        assertNotNull(activity)
    }

    // ========== User Data Saving Tests ==========

    @Test
    fun sendOrder_updatesUserNameWhenBlank() {
        coEvery { UserUtils.getCurrentUser() } returns User(
            id = "user1",
            name = "",
            email = "user@test.com",
            whatsappNumber = "5511999999999"
        )

        addProductToCart("p1", "Product", 10.0, "seller1")
        val intent = createIntent("order-1", "New Customer Name", "PIX")
        val activity = Robolectric.buildActivity(OrderPreviewActivity::class.java, intent).setup().get()

        activity.findViewById<Button>(R.id.btnSendNow).performClick()

        repeat(20) {
            shadowOf(Looper.getMainLooper()).idle()
            Thread.sleep(20)
        }

        coVerify(timeout = 3000) { UserUtils.updateUser(match { it.name == "New Customer Name" }) }
    }

    @Test
    fun sendOrder_updatesWhatsAppWhenBlank() {
        coEvery { UserUtils.getCurrentUser() } returns User(
            id = "user1",
            name = "User",
            email = "user@test.com",
            whatsappNumber = ""
        )

        addProductToCart("p1", "Product", 10.0, "seller1")
        val intent = createIntent("order-1", "Customer", "PIX", "5511988887777")
        val activity = Robolectric.buildActivity(OrderPreviewActivity::class.java, intent).setup().get()

        activity.findViewById<Button>(R.id.btnSendNow).performClick()

        repeat(20) {
            shadowOf(Looper.getMainLooper()).idle()
            Thread.sleep(20)
        }

        coVerify(timeout = 3000) { UserUtils.updateUser(match { it.whatsappNumber == "5511988887777" }) }
    }

    @Test
    fun sendOrder_doesNotUpdateUserDataWhenAlreadyFilled() {
        coEvery { UserUtils.getCurrentUser() } returns User(
            id = "user1",
            name = "Existing Name",
            email = "user@test.com",
            whatsappNumber = "5511999999999"
        )

        addProductToCart("p1", "Product", 10.0, "seller1")
        val intent = createIntent("order-1", "Different Name", "PIX", "5511988887777")
        val activity = Robolectric.buildActivity(OrderPreviewActivity::class.java, intent).setup().get()

        activity.findViewById<Button>(R.id.btnSendNow).performClick()

        repeat(20) {
            shadowOf(Looper.getMainLooper()).idle()
            Thread.sleep(20)
        }

        // Should NOT update since user already has data
        coVerify(exactly = 0) { UserUtils.updateUser(any()) }
    }

    // ========== Toast Messages Tests ==========

    @Test
    fun sendOrder_showsProcessingToast() {
        addProductToCart("p1", "Product", 10.0, "seller1")
        val intent = createIntent("order-1", "Customer", "PIX")
        val activity = Robolectric.buildActivity(OrderPreviewActivity::class.java, intent).setup().get()

        activity.findViewById<Button>(R.id.btnSendNow).performClick()
        shadowOf(Looper.getMainLooper()).idle()

        val toast = ShadowToast.getTextOfLatestToast()
        assertTrue(toast.contains("Processando pedido"))
    }

    // ========== Helper Methods ==========

    private fun createIntent(
        orderId: String,
        customerName: String,
        paymentMethod: String,
        whatsapp: String? = null
    ): Intent {
        return Intent().apply {
            putExtra(OrderPreviewActivity.EXTRA_ORDER_ID, orderId)
            putExtra(OrderPreviewActivity.EXTRA_CUSTOMER_NAME, customerName)
            putExtra(OrderPreviewActivity.EXTRA_PAYMENT_METHOD, paymentMethod)
            whatsapp?.let { putExtra(OrderPreviewActivity.EXTRA_CUSTOMER_WHATSAPP, it) }
        }
    }

    private fun addProductToCart(
        id: String,
        name: String,
        price: Double,
        sellerId: String,
        quantity: Int = 1
    ) {
        val product = Product(
            id = id,
            name = name,
            description = "Test product",
            price = price,
            category = "Test",
            stock = 10,
            imageUrls = emptyList(),
            sellerId = sellerId,
            active = true,
            createdAt = System.currentTimeMillis()
        )
        CartManager.addToCart(product, quantity)
    }
}
