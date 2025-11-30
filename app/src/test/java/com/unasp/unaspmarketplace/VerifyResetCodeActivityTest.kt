package com.unasp.unaspmarketplace

import android.content.Intent
import android.widget.TextView
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class VerifyResetCodeActivityTest {

    @Test
    fun maskedEmailDisplayed_inEmailTextView() {
        val intent = Intent().apply {
            putExtra("email", "user@example.com")
        }
        val controller = Robolectric.buildActivity(VerifyResetCodeActivity::class.java, intent).setup()
        val activity = controller.get()

        val txtEmail = activity.findViewById<TextView>(R.id.txtEmail)
        assertTrue(txtEmail.text.toString().contains("us**@example.com"))
    }
}
