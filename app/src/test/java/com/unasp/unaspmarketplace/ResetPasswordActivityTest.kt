// Kotlin
package com.unasp.unaspmarketplace

import android.content.Intent
import android.os.Looper
import android.widget.Button
import com.google.android.material.textfield.TextInputEditText
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.android.controller.ActivityController

@RunWith(RobolectricTestRunner::class)
class ResetPasswordActivityTest : BaseFirebaseTest() {

    private lateinit var controller: ActivityController<ResetPasswordActivity>
    private lateinit var activity: ResetPasswordActivity

    @Before
    override fun setupFirebase() {
        super.setupFirebase()
        val intent = Intent().apply { putExtra("email", "user@example.com") }
        controller = Robolectric.buildActivity(ResetPasswordActivity::class.java, intent)
        activity = controller.get()
    }

    @After
    override fun tearDownFirebase() {
        if (::controller.isInitialized && ::activity.isInitialized) {
            try {
                if (!activity.isFinishing) {
                    controller.pause().stop().destroy()
                } else {
                    controller.destroy()
                }
            } catch (_: IllegalStateException) {
                // Keep teardown robust
            }
        }
        super.tearDownFirebase()
    }

    //@Test todo: Fazer funcionar
    @Test
    fun missingEmail_finishesActivity() {
        val intent = Intent() // no email
        val local = Robolectric.buildActivity(ResetPasswordActivity::class.java, intent)
        val localActivity = local.create().get()
        assertTrue(localActivity.isFinishing)
        local.destroy()
    }

    @Test
    fun resetButton_initiallyDisabled() {
        controller.create().start().resume().visible()
        val btnReset = activity.findViewById<Button>(R.id.btnResetPassword)
        assertFalse(btnReset.isEnabled)
    }

    @Test
    fun passwordMismatch_showsError() {
        controller.create().start().resume().visible()

        val edtNewPassword = activity.findViewById<TextInputEditText>(R.id.edtNewPassword)
        val edtConfirmPassword = activity.findViewById<TextInputEditText>(R.id.edtConfirmPassword)

        edtNewPassword.setText("newpass123")
        edtConfirmPassword.setText("different456")
        shadowOf(Looper.getMainLooper()).idle()

        val tilConfirm = activity.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilConfirmPassword)
        assertEquals("As senhas não coincidem", tilConfirm.error)
    }

    @Test
    fun resetClick_disablesButton_andShowsResettingText() {
        controller.create().start().resume().visible()
        shadowOf(Looper.getMainLooper()).idle()

        val edtNewPassword = activity.findViewById<TextInputEditText>(R.id.edtNewPassword)
        val edtConfirmPassword = activity.findViewById<TextInputEditText>(R.id.edtConfirmPassword)
        val btnReset = activity.findViewById<Button>(R.id.btnResetPassword)

        edtNewPassword.setText("newpass123")
        edtConfirmPassword.setText("newpass123")
        shadowOf(Looper.getMainLooper()).idle()

        btnReset.performClick()
        shadowOf(Looper.getMainLooper()).idle()

        assertFalse(btnReset.isEnabled)
        assertEquals("Redefinindo...", btnReset.text.toString())
    }
}
