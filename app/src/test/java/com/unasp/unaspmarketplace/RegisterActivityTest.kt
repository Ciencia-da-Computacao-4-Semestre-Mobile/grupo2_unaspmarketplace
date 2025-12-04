package com.unasp.unaspmarketplace

import android.app.Application
import android.content.Intent
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.FirebaseApp
import com.unasp.unaspmarketplace.auth.GoogleAuthHelper
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows
import org.robolectric.android.controller.ActivityController
import org.robolectric.shadows.ShadowToast

@RunWith(RobolectricTestRunner::class)
class RegisterActivityTest : BaseFirebaseTest() {

    private lateinit var controller: ActivityController<RegisterActivity>
    private lateinit var activity: RegisterActivity

    @Before
    override fun setupFirebase() {
        super.setupFirebase()

        // Prevent heavy Google sign-in wiring during tests
        mockkObject(GoogleAuthHelper)
        val mockClient = mockk<GoogleSignInClient>(relaxed = true)
        val fakeIntent = Intent("com.unasp.FAKE_GOOGLE_SIGN_IN")
        io.mockk.every { GoogleAuthHelper.getClient(any()) } returns mockClient
        io.mockk.every { mockClient.signInIntent } returns fakeIntent

        controller = Robolectric.buildActivity(RegisterActivity::class.java)
        activity = controller.get()
    }

    @After
    override fun tearDownFirebase() {
        if (::controller.isInitialized) {
            controller.pause().stop().destroy()
        }
        unmockkObject(GoogleAuthHelper)
        super.tearDownFirebase()
    }

    @Test
    fun shouldNavigateToLoginOnLoginTextClick() {
        controller.create().start().resume().visible()

        val loginText = activity.findViewById<TextView>(R.id.log_in_text)
        loginText.performClick()

        val nextIntent = Shadows.shadowOf(activity).nextStartedActivity
        assertEquals(LoginActivity::class.java.name, nextIntent.component?.className)
        assertTrue(activity.isFinishing)
    }

    @Test
    fun shouldShowErrorForEmptyFields() {
        controller.create().start().resume().visible()

        val btnCadastrar = activity.findViewById<LinearLayout>(R.id.btnCadastrar)
        btnCadastrar.performClick()

        assertNotNull(ShadowToast.getTextOfLatestToast())
    }

    @Test
    fun shouldShowErrorForPasswordMismatch() {
        controller.create().start().resume().visible()

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
        controller.create().start().resume().visible()

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
        controller.create().start().resume().visible()

        activity.findViewById<EditText>(R.id.edtNome).setText("Teste")
        activity.findViewById<EditText>(R.id.edtEmailSignIn).setText("emailinvalido")
        activity.findViewById<EditText>(R.id.edtSenhaSignIn).setText("senha123")
        activity.findViewById<EditText>(R.id.edtConfirmarSenha).setText("senha123")

        val btnCadastrar = activity.findViewById<LinearLayout>(R.id.btnCadastrar)
        btnCadastrar.performClick()

        assertNotNull(ShadowToast.getTextOfLatestToast())
    }
}
