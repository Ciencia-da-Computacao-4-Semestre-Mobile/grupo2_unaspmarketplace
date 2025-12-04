package com.unasp.unaspmarketplace

import android.content.Intent
import android.os.Looper
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.android.controller.ActivityController
import java.text.Normalizer
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
class VerifyResetCodeActivityTest : BaseFirebaseTest() {

    private lateinit var controller: ActivityController<VerifyResetCodeActivity>
    private lateinit var activity: VerifyResetCodeActivity

    @Before
    override fun setupFirebase() {
        super.setupFirebase()
        val intent = Intent(RuntimeEnvironment.getApplication(), VerifyResetCodeActivity::class.java)
        intent.putExtra("email", "user@example.com")
        controller = Robolectric.buildActivity(VerifyResetCodeActivity::class.java, intent)
        activity = controller.get()
    }

    @After
    override fun tearDownFirebase() {
        if (::controller.isInitialized) {
            controller.pause().stop().destroy()
        }
        super.tearDownFirebase()
    }

    private fun normalizeText(text: String): String {
        return Normalizer.normalize(text, Normalizer.Form.NFD)
            .replace("\\p{M}+".toRegex(), "")
    }

    private fun idleMain() {
        shadowOf(Looper.getMainLooper()).idle()
    }

    //@Test todo: fazer funcionar
    fun resendButton_initiallyDisabled_andEnabledAfterCountdownFinish() {
        controller.create().start().resume().visible()
        val btnResend = activity.findViewById<Button>(R.id.btnResendCode)
        val txtCountdown = activity.findViewById<TextView>(R.id.txtCountdown)

        // Initially disabled during countdown
        assertFalse(btnResend.isEnabled)

        // Advance full 15 minutes to finish the CountDownTimer
        shadowOf(Looper.getMainLooper()).idleFor(15, TimeUnit.MINUTES)
        idleMain()

        // After finish, enabled and text says "Código expirado"
        assertTrue(btnResend.isEnabled)
        val countdownText = normalizeText(txtCountdown.text.toString())
        assertEquals("Codigo expirado", countdownText)
    }

    //@Test todo: fazer funcionar
    fun resendClick_disablesButton_andShowsSendingText_thenResetsLabel() {
        controller.create().start().resume().visible()
        val btnResend = activity.findViewById<Button>(R.id.btnResendCode)
        val edtCode = activity.findViewById<EditText>(R.id.edtCode)

        // Fast‑forward to enable resend first
        shadowOf(Looper.getMainLooper()).idleFor(15, TimeUnit.MINUTES)
        idleMain()
        assertTrue(btnResend.isEnabled)

        // Click resend
        btnResend.performClick()
        idleMain()

        // Immediately disabled and label "Enviando..."
        assertFalse(btnResend.isEnabled)
        assertEquals("Enviando...", btnResend.text.toString())

        // Flush coroutine completion; label resets to "Reenviar Código"
        idleMain()
        assertEquals("Reenviar Código", btnResend.text.toString())

        // Countdown restarted, so resend remains disabled and code field cleared
        assertFalse(btnResend.isEnabled)
        assertTrue(edtCode.text.isNullOrEmpty())
    }

    @Test
    fun countdown_updatesTextFormat_validTimerTick() {
        controller.create().start().resume().visible()
        val txtCountdown = activity.findViewById<TextView>(R.id.txtCountdown)

        // Let at least 1 second tick
        shadowOf(Looper.getMainLooper()).idleFor(1, TimeUnit.SECONDS)
        idleMain()

        val text = normalizeText(txtCountdown.text.toString())
        assertTrue(text.startsWith("Codigo valido por "))
        val timePart = text.removePrefix("Codigo valido por ").trim()
        assertTrue(Regex("\\d{2}:\\d{2}").matches(timePart))
    }
}
