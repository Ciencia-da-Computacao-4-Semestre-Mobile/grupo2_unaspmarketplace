package com.unasp.unaspmarketplace

import android.app.Application
import android.widget.EditText
import com.google.android.material.button.MaterialButton
import com.google.firebase.FirebaseApp
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.shadows.ShadowToast

@RunWith(RobolectricTestRunner::class)
class LoginActivityTest {

    @Before
    fun setUp() {
        // Initialize Firebase for tests
        val context = RuntimeEnvironment.getApplication() as Application
        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(context)
        }
    }

    @Test
    fun loginWithEmptyFieldsShowsEmailToast() {
        val controller = Robolectric.buildActivity(LoginActivity::class.java).setup()
        val activity = controller.get()

        activity.findViewById<EditText>(R.id.edtEmail).setText("")
        activity.findViewById<EditText>(R.id.edtSenha).setText("")

        activity.findViewById<MaterialButton>(R.id.btnLogin).performClick()
        assertEquals("Digite o email", ShadowToast.getTextOfLatestToast())
    }

    @Test
    fun loginWithInvalidEmailShowsToast() {
        val controller = Robolectric.buildActivity(LoginActivity::class.java).setup()
        val activity = controller.get()

        activity.findViewById<EditText>(R.id.edtEmail).setText("invalid-email")
        activity.findViewById<EditText>(R.id.edtSenha).setText("123456")

        activity.findViewById<MaterialButton>(R.id.btnLogin).performClick()
        assertEquals("Email inválido", ShadowToast.getTextOfLatestToast())
    }
}

