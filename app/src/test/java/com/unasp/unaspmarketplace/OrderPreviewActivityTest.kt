// kotlin
package com.unasp.unaspmarketplace

import android.content.Intent
import com.unasp.unaspmarketplace.models.Product
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import com.unasp.unaspmarketplace.utils.CartManager
import java.util.Locale

@RunWith(RobolectricTestRunner::class)
class OrderPreviewActivityTest {

    @Before
    fun clearCart() {
        // Initialize Firebase for tests
        val context = org.robolectric.RuntimeEnvironment.getApplication() as android.app.Application
        if (com.google.firebase.FirebaseApp.getApps(context).isEmpty()) {
            com.google.firebase.FirebaseApp.initializeApp(context)
        }
        CartManager.clearCart()
    }

    @Test
    fun finishesWhenCartIsEmpty() {
        val intent = Intent().apply {
            putExtra(OrderPreviewActivity.EXTRA_ORDER_ID, "order-123")
            putExtra(OrderPreviewActivity.EXTRA_CUSTOMER_NAME, "John Doe")
            putExtra(OrderPreviewActivity.EXTRA_PAYMENT_METHOD, "Dinheiro")
        }

        val controller = Robolectric.buildActivity(OrderPreviewActivity::class.java, intent).setup()
        val activity = controller.get()

        assertTrue(activity.isFinishing)
    }

    //@Test todo: corrigir esse teste
    fun finishesWhenCartIsEmpty_whenPaymentMethodMissing() {
        val intent = Intent().apply {
            putExtra(OrderPreviewActivity.EXTRA_ORDER_ID, "order-xyz")
            putExtra(OrderPreviewActivity.EXTRA_CUSTOMER_NAME, "Cliente Teste")
            // Payment method intentionally missing
        }

        val controller = Robolectric.buildActivity(OrderPreviewActivity::class.java, intent).setup()
        val activity = controller.get()

        assertTrue(activity.isFinishing)
    }

    @Test
    fun cancelButton_finishesActivity_whenCartHasItems() {
        // Adiciona item ao carrinho (usa a API atual do CartManager)
        val product = Product(id = "p1", name = "Caneca Teste", price = 15.0, sellerId = "seller1")
        CartManager.addToCart(product, 1)

        val intent = Intent().apply {
            putExtra(OrderPreviewActivity.EXTRA_ORDER_ID, "order-1")
            putExtra(OrderPreviewActivity.EXTRA_CUSTOMER_NAME, "Cliente")
            putExtra(OrderPreviewActivity.EXTRA_PAYMENT_METHOD, "Cartão")
        }

        val controller = Robolectric.buildActivity(OrderPreviewActivity::class.java, intent).setup()
        val activity = controller.get()

        val btnCancel = activity.findViewById<android.widget.Button>(R.id.btnCancel)
        btnCancel.performClick()

        assertTrue(activity.isFinishing)
    }

    //@Test todo: corrigir esse teste
    fun displaysOrderPreview_withProductsAndCorrectTotal() {
        // Dois produtos: 2 x 10.0 e 1 x 5.0 => total 25.00
        val p1 = Product(id = "p1", name = "Produto A", price = 10.0, sellerId = "s1")
        val p2 = Product(id = "p2", name = "Produto B", price = 5.0, sellerId = "s2")

        CartManager.addToCart(p1, 2)
        CartManager.addToCart(p2, 1)

        val intent = Intent().apply {
            putExtra(OrderPreviewActivity.EXTRA_ORDER_ID, "order-55")
            putExtra(OrderPreviewActivity.EXTRA_CUSTOMER_NAME, "Cliente X")
            putExtra(OrderPreviewActivity.EXTRA_PAYMENT_METHOD, "Dinheiro")
            putExtra(OrderPreviewActivity.EXTRA_CUSTOMER_WHATSAPP, "5511999999999")
        }

        val controller = Robolectric.buildActivity(OrderPreviewActivity::class.java, intent).setup()
        val activity = controller.get()

        val txtPreview = activity.findViewById<android.widget.TextView>(R.id.txtOrderPreview)
        val previewText = txtPreview.text.toString().lowercase(Locale.getDefault())

        assertTrue("Preview deve conter nome do Produto A", previewText.contains("produto a"))
        assertTrue("Preview deve conter nome do Produto B", previewText.contains("produto b"))
        // Verifica total formatado com duas casas (pode depender de locale, usa ponto)
        assertTrue("Preview deve conter total R$ 25.00", previewText.contains("r$ 25.00"))
        // Verifica inclusão do WhatsApp do cliente
        assertTrue("Preview deve conter whatsapp do cliente", previewText.contains("5511999999999"))
    }
}
