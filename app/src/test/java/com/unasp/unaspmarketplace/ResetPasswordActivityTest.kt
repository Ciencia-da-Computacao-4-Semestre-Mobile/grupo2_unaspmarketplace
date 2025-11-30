package com.unasp.unaspmarketplace

import android.content.Intent
import android.widget.Button
import com.google.android.material.textfield.TextInputEditText
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ResetPasswordActivityTest {

    @Test
    fun passwordFields_validation_enablesResetButton() {
        val intent = Intent().apply {
            putExtra("email", "user@example.com")
        }
        val controller = Robolectric.buildActivity(ResetPasswordActivity::class.java, intent).setup()
        val activity = controller.get()

        val edtNew = activity.findViewById<TextInputEditText>(R.id.edtNewPassword)
        val edtConfirm = activity.findViewById<TextInputEditText>(R.id.edtConfirmPassword)
        val btnReset = activity.findViewById<Button>(R.id.btnResetPassword)

        edtNew.setText("abcdef")
        edtConfirm.setText("abcdef")

        // listeners adicionados no onCreate devem habilitar o botão
        assertTrue(btnReset.isEnabled)
    }
}
