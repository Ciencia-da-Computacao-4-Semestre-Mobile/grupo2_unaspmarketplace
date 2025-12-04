package com.unasp.unaspmarketplace

import android.app.Application
import com.google.firebase.FirebaseApp
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class MainActivityTest {

    @Before
    fun setUp() {
        // Initialize Firebase for tests
        val context = RuntimeEnvironment.getApplication() as Application
        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(context)
        }
    }

    @Test
    fun setsContentViewSuccessfully() {
        val controller = Robolectric.buildActivity(MainActivity::class.java).setup()
        val activity = controller.get()
        // Verifica que a view raiz existe
        val root = activity.findViewById<android.view.View>(android.R.id.content)
        assertNotNull(root)
    }
}

