// kotlin
package com.unasp.unaspmarketplace

import android.view.View
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.unasp.unaspmarketplace.models.Product
import com.unasp.unaspmarketplace.utils.CartManager
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.ShadowAlertDialog
import org.robolectric.shadows.ShadowLooper
import org.robolectric.shadows.ShadowToast

@RunWith(RobolectricTestRunner::class)
class CartActivityTest {

    @Before
    fun setup() {
        // Initialize Firebase for tests
        val context = org.robolectric.RuntimeEnvironment.getApplication() as android.app.Application
        if (com.google.firebase.FirebaseApp.getApps(context).isEmpty()) {
            com.google.firebase.FirebaseApp.initializeApp(context)
        }
        // Garantir estado limpo antes de cada teste
        CartManager.clearCart()
    }

    @After
    fun tearDown() {
        CartManager.clearCart()
    }

    @Test
    fun showsEmptyStateWhenCartIsEmpty() {
        val controller = Robolectric.buildActivity(CartActivity::class.java).setup()
        val activity = controller.get()

        val empty = activity.findViewById<LinearLayout>(R.id.emptyCartView)
        val list = activity.findViewById<RecyclerView>(R.id.recyclerCart)
        val checkout = activity.findViewById<Button>(R.id.btnCheckout)

        assertEquals(View.VISIBLE, empty.visibility)
        assertEquals(View.GONE, list.visibility)
        assertFalse(checkout.isEnabled)
    }

    @Test
    fun clickingCheckoutWithEmptyCartShowsToast() {
        val controller = Robolectric.buildActivity(CartActivity::class.java).setup()
        val activity = controller.get()

        activity.findViewById<Button>(R.id.btnCheckout).performClick()
        assertEquals("Carrinho vazio!", ShadowToast.getTextOfLatestToast())
    }

    @Test
    fun showsItemsAndTotalWhenCartHasItems() {
        // Arrange: adicionar item ao carrinho
        val product = Product(
            id = "p1",
            name = "Notebook",
            description = "",
            price = 1000.0,
            category = "Eletrônicos",
            stock = 10,
            active = true
        )
        CartManager.addToCart(product, 2)

        // Act
        val controller = Robolectric.buildActivity(CartActivity::class.java).setup()
        val activity = controller.get()

        val empty = activity.findViewById<LinearLayout>(R.id.emptyCartView)
        val list = activity.findViewById<RecyclerView>(R.id.recyclerCart)
        val checkout = activity.findViewById<Button>(R.id.btnCheckout)
        val totalText = activity.findViewById<TextView>(R.id.txtTotal)

        // Assert
        assertEquals(View.GONE, empty.visibility)
        assertEquals(View.VISIBLE, list.visibility)
        assertTrue(checkout.isEnabled)
        assertTrue(totalText.text.toString().contains("R$"))
        assertTrue(totalText.text.toString().contains("2000"))
    }

    @Test
    fun removeButton_removesSpecificItem_and_updatesTotal() {
        val p1 = Product(
            id = "p_rm1",
            name = "Produto A",
            description = "",
            price = 10.0,
            category = "Cat",
            stock = 5,
            active = true
        )
        val p2 = Product(
            id = "p_rm2",
            name = "Produto B",
            description = "",
            price = 5.0,
            category = "Cat",
            stock = 5,
            active = true
        )
        CartManager.addToCart(p1, 1)
        CartManager.addToCart(p2, 1)

        val controller = Robolectric.buildActivity(CartActivity::class.java).setup()
        val activity = controller.get()

        val recycler = activity.findViewById<RecyclerView>(R.id.recyclerCart)
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        // Encontrar o item cujo nome é "Produto A" e clicar no botão remove
        var removed = false
        for (i in 0 until recycler.childCount) {
            val child = recycler.getChildAt(i)
            val nameView = child.findViewById<TextView>(R.id.txtName)
            if (nameView != null && nameView.text.toString().contains("Produto A")) {
                val btnRemove = child.findViewById<Button>(R.id.btnRemove)
                btnRemove.performClick()
                removed = true
                break
            }
        }
        assertTrue("Não encontrou item para remover", removed)

        // Verificar estado do CartManager e total
        assertFalse(CartManager.isInCart("p_rm1"))
        val txtTotal = activity.findViewById<TextView>(R.id.txtTotal)
        assertTrue(txtTotal.text.toString().contains("R$ 5.00"))
    }

    //@Test todo: funfa esse teste
    fun clearCart_menuAction_showsDialog_and_clearsOnConfirm() {
        val product = Product(
            id = "p_clear",
            name = "ToClear",
            description = "",
            price = 20.0,
            category = "Cat",
            stock = 2,
            active = true
        )
        CartManager.addToCart(product, 1)

        val controller = Robolectric.buildActivity(CartActivity::class.java).setup()
        val activity = controller.get()

        val toolbar = activity.findViewById<MaterialToolbar>(R.id.appbar_cart)
        // abrir ação de menu que cria o diálogo
        toolbar.menu.performIdentifierAction(R.id.btnClearCart, 0)
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        val latest = ShadowAlertDialog.getLatestAlertDialog() as AlertDialog?
        assertNotNull("Diálogo de confirmação não foi exibido", latest)

        // Clicar no botão positivo do diálogo (Limpar)
        val positive = latest!!.getButton(AlertDialog.BUTTON_POSITIVE)
        positive.performClick()

        // Após confirmação, carrinho deve estar vazio e UI refletir
        assertTrue(CartManager.getCartItems().isEmpty())
        val empty = activity.findViewById<LinearLayout>(R.id.emptyCartView)
        assertEquals(View.VISIBLE, empty.visibility)
    }
}
