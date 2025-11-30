package com.unasp.unaspmarketplace

import android.widget.RadioGroup
import android.widget.TextView
import com.google.android.material.textfield.TextInputEditText
import com.unasp.unaspmarketplace.utils.CartManager
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.shadows.ShadowToast

@RunWith(RobolectricTestRunner::class)
class PaymentActivityTest {

    @Before
    fun setUp() {
        // Initialize Firebase for tests
        val context = org.robolectric.RuntimeEnvironment.getApplication() as android.app.Application
        if (com.google.firebase.FirebaseApp.getApps(context).isEmpty()) {
            com.google.firebase.FirebaseApp.initializeApp(context)
        }
        CartManager.clearCart()
    }

    @After
    fun tearDown() {
        CartManager.clearCart()
    }

    @Test
    fun confirmButtonDisabledInitially() {
        val controller = Robolectric.buildActivity(PaymentActivity::class.java).setup()
        val activity = controller.get()

        val btn = activity.findViewById<android.widget.Button>(R.id.btnConfirmPayment)
        assertFalse(btn.isEnabled)
    }

    @Test
    fun confirmWithEmptyCartShowsToast() {
        val controller = Robolectric.buildActivity(PaymentActivity::class.java).setup()
        val activity = controller.get()

        val name = activity.findViewById<TextInputEditText>(R.id.edtCustomerName)
        val wa = activity.findViewById<TextInputEditText>(R.id.edtWhatsappNumber)
        val rg = activity.findViewById<RadioGroup>(R.id.rgPaymentMethods)
        val btn = activity.findViewById<android.widget.Button>(R.id.btnConfirmPayment)

        // Preencher campos válidos
        name.setText("John Doe")
        wa.setText("11999999999")
        rg.check(R.id.rbPix)

        // Agora o botão deve estar habilitado
        assertTrue(btn.isEnabled)

        // Carrinho vazio -> deve mostrar toast correspondente
        btn.performClick()
        assertTrue(ShadowToast.getTextOfLatestToast().toString().contains("Carrinho vazio"))
    }
}

