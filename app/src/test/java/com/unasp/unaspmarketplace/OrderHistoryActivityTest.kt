package com.unasp.unaspmarketplace

import android.app.Application
import android.content.Intent
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.firebase.FirebaseApp
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows

@RunWith(RobolectricTestRunner::class)
class OrderHistoryActivityTest {

    @Before
    fun setUp() {
        // Initialize Firebase for tests
        val context = RuntimeEnvironment.getApplication() as Application
        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(context)
        }
    }

    @Test
    fun showsEmptyStateOnCreate() {
        val controller = Robolectric.buildActivity(OrderHistoryActivity::class.java).setup()
        val activity = controller.get()

        val txtEmpty = activity.findViewById<View>(R.id.txtEmptyState)
        val recycler = activity.findViewById<RecyclerView>(R.id.recyclerOrders)

        assertEquals(View.VISIBLE, txtEmpty.visibility)
        assertEquals(View.GONE, recycler.visibility)
    }

    @Test
    fun startShoppingNavigatesToHome() {
        val controller = Robolectric.buildActivity(OrderHistoryActivity::class.java).setup()
        val activity = controller.get()

        activity.findViewById<MaterialButton>(R.id.btnStartShopping).performClick()

        val nextIntent: Intent = Shadows.shadowOf(activity).nextStartedActivity
        assertEquals(HomeActivity::class.java.name, nextIntent.component?.className)
    }
}

