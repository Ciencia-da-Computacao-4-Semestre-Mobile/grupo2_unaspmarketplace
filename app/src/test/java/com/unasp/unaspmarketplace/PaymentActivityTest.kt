// Kotlin
package com.unasp.unaspmarketplace

import android.os.Looper
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.TextView
import com.google.android.material.textfield.TextInputEditText
import com.unasp.unaspmarketplace.models.Product
import com.unasp.unaspmarketplace.utils.CartManager
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.ShadowAlertDialog
import org.robolectric.shadows.ShadowToast

@RunWith(RobolectricTestRunner::class)
class PaymentActivityTest {

    @Before
    fun setUp() {
        CartManager.clearCart()
    }

    @After
    fun tearDown() {
        CartManager.clearCart()
    }

    @Test
    fun confirmButtonDisabledInitially() {
        val activity = Robolectric.buildActivity(PaymentActivity::class.java).setup().get()
        shadowOf(Looper.getMainLooper()).idle()

        val btn = activity.findViewById<Button>(R.id.btnConfirmPayment)
        assertFalse(btn.isEnabled)
    }

    //@Test conserte
    fun selectingPayment_updatesLabel_and_enablesButton_whenNamePresent() {
        val activity = Robolectric.buildActivity(PaymentActivity::class.java).setup().get()

        activity.findViewById<TextView>(R.id.txtCustomerNameValue).text = "John Doe"
        activity.findViewById<RadioGroup>(R.id.rgPaymentMethods).check(R.id.rbPix)
        shadowOf(Looper.getMainLooper()).idle()

        val txtPaymentSelected = activity.findViewById<TextView>(R.id.txtPaymentSelected)
        val btn = activity.findViewById<Button>(R.id.btnConfirmPayment)
        assertEquals("PIX", txtPaymentSelected.text.toString())
        assertTrue(btn.isEnabled)
    }

    @Test
    fun missingName_keepsConfirmDisabled_evenWithPaymentSelected() {
        val activity = Robolectric.buildActivity(PaymentActivity::class.java).setup().get()

        activity.findViewById<TextView>(R.id.txtCustomerNameValue).text = ""
        activity.findViewById<RadioGroup>(R.id.rgPaymentMethods).check(R.id.rbPix)
        shadowOf(Looper.getMainLooper()).idle()

        val btn = activity.findViewById<Button>(R.id.btnConfirmPayment)
        assertFalse(btn.isEnabled)
    }

    //@Test todo: conserte
    fun missingWhatsapp_showsToast_onConfirm() {
        val activity = Robolectric.buildActivity(PaymentActivity::class.java).setup().get()

        activity.findViewById<TextView>(R.id.txtCustomerNameValue).text = "John Doe"
        activity.findViewById<TextView>(R.id.txtWhatsappValue).text = ""
        activity.findViewById<RadioGroup>(R.id.rgPaymentMethods).check(R.id.rbPix)
        shadowOf(Looper.getMainLooper()).idle()

        // Ensure button enabled
        assertTrue(activity.findViewById<Button>(R.id.btnConfirmPayment).isEnabled)

        // Ensure cart not empty to reach WhatsApp validation
        addSampleItemToCart()

        activity.findViewById<Button>(R.id.btnConfirmPayment).performClick()
        shadowOf(Looper.getMainLooper()).idle()

        val toast = ShadowToast.getTextOfLatestToast().orEmpty()
        assertTrue(toast.contains("Digite seu número de WhatsApp"))
    }

    //@Test todo: conserte
    fun emptyCart_showsToast_onConfirm() {
        val activity = Robolectric.buildActivity(PaymentActivity::class.java).setup().get()

        activity.findViewById<TextView>(R.id.txtCustomerNameValue).text = "John Doe"
        activity.findViewById<TextView>(R.id.txtWhatsappValue).text = "11999999999"
        activity.findViewById<RadioGroup>(R.id.rgPaymentMethods).check(R.id.rbPix)
        shadowOf(Looper.getMainLooper()).idle()

        val btn = activity.findViewById<Button>(R.id.btnConfirmPayment)
        assertTrue(btn.isEnabled)

        // Cart is empty by setup
        btn.performClick()
        shadowOf(Looper.getMainLooper()).idle()

        val toast = ShadowToast.getTextOfLatestToast().orEmpty()
        assertTrue(toast.contains("Carrinho vazio"))
    }

    //@Test todo: conserte
    fun editCustomerDialog_savesFields_and_enablesButton_whenPaymentSelected() {
        val activity = Robolectric.buildActivity(PaymentActivity::class.java).setup().get()

        activity.findViewById<Button>(R.id.btnEditCustomer).performClick()
        shadowOf(Looper.getMainLooper()).idle()

        val dialog = ShadowAlertDialog.getLatestAlertDialog()
        requireNotNull(dialog) { "AlertDialog was not shown" }

        // Access the custom dialog view and find EditTexts
        val dialogView = dialog.window!!.decorView
        val inputs = findAllEditTexts(dialogView)
        require(inputs.size >= 2) { "Edit dialog should have at least two EditTexts" }

        inputs[0].setText("Jane Roe")
        inputs[1].setText("11988887777")

        dialog.getButton(android.content.DialogInterface.BUTTON_POSITIVE).performClick()
        shadowOf(Looper.getMainLooper()).idle()

        val nameView = activity.findViewById<TextView>(R.id.txtCustomerNameValue)
        val waView = activity.findViewById<TextView>(R.id.txtWhatsappValue)
        assertEquals("Jane Roe", nameView.text.toString())
        assertEquals("11988887777", waView.text.toString())

        activity.findViewById<RadioGroup>(R.id.rgPaymentMethods).check(R.id.rbPix)
        shadowOf(Looper.getMainLooper()).idle()
        assertTrue(activity.findViewById<Button>(R.id.btnConfirmPayment).isEnabled)
    }

    @Test
    fun missingPickupLocation_isValidated_afterCartNotEmpty() {
        val activity = Robolectric.buildActivity(PaymentActivity::class.java).setup().get()

        activity.findViewById<TextView>(R.id.txtCustomerNameValue).text = "John Doe"
        activity.findViewById<TextView>(R.id.txtWhatsappValue).text = "11999999999"
        activity.findViewById<RadioGroup>(R.id.rgPaymentMethods).check(R.id.rbPix)
        shadowOf(Looper.getMainLooper()).idle()

        // Ensure cart has items to reach pickup validation
        addSampleItemToCart()

        activity.findViewById<TextInputEditText>(R.id.edtPickupLocation).setText("")
        val btn = activity.findViewById<Button>(R.id.btnConfirmPayment)
        assertTrue(btn.isEnabled)

        btn.performClick()
        shadowOf(Looper.getMainLooper()).idle()

        val msg = ShadowToast.getTextOfLatestToast().orEmpty()
        assertTrue(msg.contains("Informe o local de retirada"))
    }

    // Helpers

    private fun addSampleItemToCart() {
        // Use named args to match Product constructor and avoid type mismatches.
        // Product must expose 'stock' as used by CartManager; adjust fields if needed.
        val product = Product(
            id = "p1",
            name = "Produto X",
            description = "Sample item",
            price = 10.0,
            category = "General",
            stock = 10,
            imageUrls = emptyList(),
            sellerId = "seller-1",
            active = true,
            createdAt = System.currentTimeMillis()
        )
        CartManager.addToCart(product, 1)
    }

    private fun findAllEditTexts(root: android.view.View): List<EditText> {
        val result = mutableListOf<EditText>()
        if (root is EditText) result.add(root)
        if (root is android.view.ViewGroup) {
            for (i in 0 until root.childCount) {
                result.addAll(findAllEditTexts(root.getChildAt(i)))
            }
        }
        return result
    }
}
