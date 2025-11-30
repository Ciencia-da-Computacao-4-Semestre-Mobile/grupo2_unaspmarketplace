package com.unasp.unaspmarketplace

import android.app.Application
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import com.google.firebase.FirebaseApp
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows
import org.robolectric.shadows.ShadowToast

@RunWith(RobolectricTestRunner::class)
class RegisterActivityTest {

    @Before
    fun setUp() {
        val context = RuntimeEnvironment.getApplication() as Application
        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(context)
        }
    }

    @Test
    fun shouldNavigateToLoginOnLoginTextClick() {
        val controller = Robolectric.buildActivity(RegisterActivity::class.java).setup()
        val activity = controller.get()

        val loginText = activity.findViewById<TextView>(R.id.log_in_text)
        loginText.performClick()

        val nextIntent = Shadows.shadowOf(activity).nextStartedActivity
        assertEquals(LoginActivity::class.java.name, nextIntent.component?.className)
        assertTrue(activity.isFinishing)
    }

    @Test
    fun shouldShowErrorForEmptyFields() {
        val controller = Robolectric.buildActivity(RegisterActivity::class.java).setup()
        val activity = controller.get()

        val btnCadastrar = activity.findViewById<LinearLayout>(R.id.btnCadastrar)
        btnCadastrar.performClick()

        assertNotNull(ShadowToast.getTextOfLatestToast())
    }

    @Test
    fun shouldShowErrorForPasswordMismatch() {
        val controller = Robolectric.buildActivity(RegisterActivity::class.java).setup()
        val activity = controller.get()

        activity.findViewById<EditText>(R.id.edtNome).setText("Teste")
        activity.findViewById<EditText>(R.id.edtEmailSignIn).setText("teste@email.com")
        activity.findViewById<EditText>(R.id.edtSenhaSignIn).setText("senha123")
        activity.findViewById<EditText>(R.id.edtConfirmarSenha).setText("senha456")

        val btnCadastrar = activity.findViewById<LinearLayout>(R.id.btnCadastrar)
        btnCadastrar.performClick()

        assertNotNull(ShadowToast.getTextOfLatestToast())
    }

    @Test
    fun shouldShowErrorForShortPassword() {
        val controller = Robolectric.buildActivity(RegisterActivity::class.java).setup()
        val activity = controller.get()

        activity.findViewById<EditText>(R.id.edtNome).setText("Teste")
        activity.findViewById<EditText>(R.id.edtEmailSignIn).setText("teste@email.com")
        activity.findViewById<EditText>(R.id.edtSenhaSignIn).setText("12345")
        activity.findViewById<EditText>(R.id.edtConfirmarSenha).setText("12345")

        val btnCadastrar = activity.findViewById<LinearLayout>(R.id.btnCadastrar)
        btnCadastrar.performClick()

        assertNotNull(ShadowToast.getTextOfLatestToast())
    }

    @Test
    fun shouldShowErrorForInvalidEmail() {
        val controller = Robolectric.buildActivity(RegisterActivity::class.java).setup()
        val activity = controller.get()

        activity.findViewById<EditText>(R.id.edtNome).setText("Teste")
        activity.findViewById<EditText>(R.id.edtEmailSignIn).setText("emailinvalido")
        activity.findViewById<EditText>(R.id.edtSenhaSignIn).setText("senha123")
        activity.findViewById<EditText>(R.id.edtConfirmarSenha).setText("senha123")

        val btnCadastrar = activity.findViewById<LinearLayout>(R.id.btnCadastrar)
        btnCadastrar.performClick()

        assertNotNull(ShadowToast.getTextOfLatestToast())
    }
}
