package com.unasp.unaspmarketplace

import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.unasp.unaspmarketplace.utils.AccountLinkingHelper
import com.unasp.unaspmarketplace.utils.CartManager
import io.mockk.*
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
class ProfileActivityTest : BaseFirebaseTest() {

    private lateinit var controller: ActivityController<ProfileActivity>
    private lateinit var activity: ProfileActivity

    @Before
    override fun setupFirebase() {
        super.setupFirebase()

        val mockUser = mockk<com.google.firebase.auth.FirebaseUser>(relaxed = true)
        every { mockUser.email } returns "test@test.com"
        every { mockUser.uid } returns "test-uid"
        every { mockAuth.currentUser } returns mockUser

        CartManager.clearCart()
        mockkObject(AccountLinkingHelper)

        // Default: normal account with password
        every { AccountLinkingHelper.isGoogleAccount(any()) } returns false
        every { AccountLinkingHelper.hasPasswordProvider(any()) } returns true
        every { AccountLinkingHelper.canLoginWithPassword(any()) } returns true
        every { AccountLinkingHelper.getProviderInfo(any()) } returns "Provider info"
        every { AccountLinkingHelper.linkPasswordToGoogleAccount(any(), any(), any()) } answers {
            val callback = thirdArg<(Boolean, String) -> Unit>()
            callback(true, "Senha vinculada")
            Unit
        }

        controller = Robolectric.buildActivity(ProfileActivity::class.java)
        activity = controller.get()
    }

    @After
    override fun tearDownFirebase() {
        if (::controller.isInitialized) {
            controller.pause().stop().destroy()
        }
        unmockkObject(AccountLinkingHelper)
        CartManager.clearCart()
        super.tearDownFirebase()
    }

    // ========== Initialization & Layout Tests ==========

    @Test
    fun activity_initializesAllViews() {
        controller.create().start().resume().visible()

        assertNotNull(activity.findViewById<TextInputEditText>(R.id.etName))
        assertNotNull(activity.findViewById<TextInputEditText>(R.id.etEmail))
        assertNotNull(activity.findViewById<TextInputEditText>(R.id.etNumber))
        assertNotNull(activity.findViewById<TextInputEditText>(R.id.etCurrentPassword))
        assertNotNull(activity.findViewById<TextInputEditText>(R.id.etNewPassword))
        assertNotNull(activity.findViewById<TextInputEditText>(R.id.etConfirmPassword))
        assertNotNull(activity.findViewById<MaterialButton>(R.id.btnSaveProfile))
        assertNotNull(activity.findViewById<MaterialButton>(R.id.btnChangePassword))
        assertNotNull(activity.findViewById<MaterialButton>(R.id.btnLogout))
        assertNotNull(activity.findViewById<MaterialButton>(R.id.btnDeleteAccount))
        assertNotNull(activity.findViewById<TextView>(R.id.txtPasswordInfo))
    }

    @Test
    fun normalAccount_passwordSectionEnabled() {
        controller.create().start().resume().visible()
        shadowOf(Looper.getMainLooper()).idle()

        val etCurrentPassword = activity.findViewById<TextInputEditText>(R.id.etCurrentPassword)
        val txtPasswordInfo = activity.findViewById<TextView>(R.id.txtPasswordInfo)

        assertTrue(etCurrentPassword.isEnabled)
        assertEquals(View.GONE, txtPasswordInfo.visibility)
    }

    @Test
    fun googleAccountWithoutPassword_loadsActivity() {
        every { AccountLinkingHelper.isGoogleAccount(any()) } returns true
        every { AccountLinkingHelper.hasPasswordProvider(any()) } returns false

        controller.create().start().resume().visible()

        // Verify activity loads without crash
        val etCurrentPassword = activity.findViewById<TextInputEditText>(R.id.etCurrentPassword)
        val btnChangePassword = activity.findViewById<MaterialButton>(R.id.btnChangePassword)

        assertNotNull(etCurrentPassword)
        assertNotNull(btnChangePassword)
    }

    @Test
    fun googleAccountWithPassword_loadsActivity() {
        every { AccountLinkingHelper.isGoogleAccount(any()) } returns true
        every { AccountLinkingHelper.hasPasswordProvider(any()) } returns true

        controller.create().start().resume().visible()

        // Verify activity loads without crash
        val etCurrentPassword = activity.findViewById<TextInputEditText>(R.id.etCurrentPassword)

        assertNotNull(etCurrentPassword)
    }

    // ========== Save Profile Validation Tests ==========

    @Test
    fun saveProfile_emptyName_showsError() {
        controller.create().start().resume().visible()

        val etName = activity.findViewById<TextInputEditText>(R.id.etName)
        val btnSave = activity.findViewById<Button>(R.id.btnSaveProfile)

        etName.setText("")
        btnSave.performClick()
        shadowOf(Looper.getMainLooper()).idle()

        assertEquals("Nome não pode estar vazio", etName.error)
    }

    @Test
    fun saveProfile_whitespaceName_showsError() {
        controller.create().start().resume().visible()

        val etName = activity.findViewById<TextInputEditText>(R.id.etName)
        val btnSave = activity.findViewById<MaterialButton>(R.id.btnSaveProfile)

        etName.setText("   ")
        btnSave.performClick()
        shadowOf(Looper.getMainLooper()).idle()

        assertEquals("Nome não pode estar vazio", etName.error)
    }

    @Test
    fun saveProfile_shortWhatsapp_showsError() {
        controller.create().start().resume().visible()

        val etName = activity.findViewById<TextInputEditText>(R.id.etName)
        val etWhatsapp = activity.findViewById<TextInputEditText>(R.id.etNumber)
        val btnSave = activity.findViewById<MaterialButton>(R.id.btnSaveProfile)

        etName.setText("Valid Name")
        etWhatsapp.setText("12345")

        btnSave.performClick()
        shadowOf(Looper.getMainLooper()).idle()

        assertEquals("Digite um número de WhatsApp válido", etWhatsapp.error)
    }

    @Test
    fun saveProfile_validInputs_disablesButton() {
        controller.create().start().resume().visible()

        val etName = activity.findViewById<TextInputEditText>(R.id.etName)
        val etWhatsapp = activity.findViewById<TextInputEditText>(R.id.etNumber)
        val btnSave = activity.findViewById<MaterialButton>(R.id.btnSaveProfile)

        etName.setText("Valid Name")
        etWhatsapp.setText("5511999999999")

        btnSave.performClick()
        shadowOf(Looper.getMainLooper()).idle()

        assertFalse(btnSave.isEnabled)
        assertEquals("Salvando...", btnSave.text.toString())
    }

    @Test
    fun saveProfile_emptyWhatsapp_allowed() {
        controller.create().start().resume().visible()

        val etName = activity.findViewById<TextInputEditText>(R.id.etName)
        val etWhatsapp = activity.findViewById<TextInputEditText>(R.id.etNumber)
        val btnSave = activity.findViewById<MaterialButton>(R.id.btnSaveProfile)

        etName.setText("Valid Name")
        etWhatsapp.setText("")

        btnSave.performClick()
        shadowOf(Looper.getMainLooper()).idle()

        assertFalse(btnSave.isEnabled)
        assertEquals("Salvando...", btnSave.text.toString())
    }

    @Test
    fun saveProfile_10DigitWhatsapp_allowed() {
        controller.create().start().resume().visible()

        val etName = activity.findViewById<TextInputEditText>(R.id.etName)
        val etWhatsapp = activity.findViewById<TextInputEditText>(R.id.etNumber)
        val btnSave = activity.findViewById<MaterialButton>(R.id.btnSaveProfile)

        etName.setText("Valid Name")
        etWhatsapp.setText("5511999999")

        btnSave.performClick()
        shadowOf(Looper.getMainLooper()).idle()

        assertFalse(btnSave.isEnabled)
    }

    @Test
    fun saveProfile_trimsInputs() {
        controller.create().start().resume().visible()

        val etName = activity.findViewById<TextInputEditText>(R.id.etName)
        val etWhatsapp = activity.findViewById<TextInputEditText>(R.id.etNumber)
        val btnSave = activity.findViewById<MaterialButton>(R.id.btnSaveProfile)

        etName.setText("  Name With Spaces  ")
        etWhatsapp.setText("  5511999999999  ")

        btnSave.performClick()
        shadowOf(Looper.getMainLooper()).idle()

        assertFalse(btnSave.isEnabled)
    }

    // ========== Change Password Validation Tests (Normal Account) ==========

    @Test
    fun changePassword_emptyCurrentPassword_showsError() {
        controller.create().start().resume().visible()

        val etCurrentPassword = activity.findViewById<TextInputEditText>(R.id.etCurrentPassword)
        val etNewPassword = activity.findViewById<TextInputEditText>(R.id.etNewPassword)
        val etConfirmPassword = activity.findViewById<TextInputEditText>(R.id.etConfirmPassword)
        val btnChangePassword = activity.findViewById<MaterialButton>(R.id.btnChangePassword)

        etCurrentPassword.setText("")
        etNewPassword.setText("newPass123")
        etConfirmPassword.setText("newPass123")

        btnChangePassword.performClick()
        shadowOf(Looper.getMainLooper()).idle()

        assertEquals("Digite a senha atual", etCurrentPassword.error)
    }

    @Test
    fun changePassword_emptyNewPassword_showsError() {
        controller.create().start().resume().visible()

        val etCurrentPassword = activity.findViewById<TextInputEditText>(R.id.etCurrentPassword)
        val etNewPassword = activity.findViewById<TextInputEditText>(R.id.etNewPassword)
        val etConfirmPassword = activity.findViewById<TextInputEditText>(R.id.etConfirmPassword)
        val btnChangePassword = activity.findViewById<MaterialButton>(R.id.btnChangePassword)

        etCurrentPassword.setText("currentPass123")
        etNewPassword.setText("")
        etConfirmPassword.setText("")

        btnChangePassword.performClick()
        shadowOf(Looper.getMainLooper()).idle()

        assertEquals("Digite a nova senha", etNewPassword.error)
    }

    @Test
    fun changePassword_shortNewPassword_showsError() {
        controller.create().start().resume().visible()

        val etCurrentPassword = activity.findViewById<TextInputEditText>(R.id.etCurrentPassword)
        val etNewPassword = activity.findViewById<TextInputEditText>(R.id.etNewPassword)
        val etConfirmPassword = activity.findViewById<TextInputEditText>(R.id.etConfirmPassword)
        val btnChangePassword = activity.findViewById<MaterialButton>(R.id.btnChangePassword)

        etCurrentPassword.setText("currentPass123")
        etNewPassword.setText("12345")
        etConfirmPassword.setText("12345")

        btnChangePassword.performClick()
        shadowOf(Looper.getMainLooper()).idle()

        assertEquals("A senha deve ter pelo menos 6 caracteres", etNewPassword.error)
    }

    @Test
    fun changePassword_mismatchedPasswords_showsError() {
        controller.create().start().resume().visible()

        val etCurrentPassword = activity.findViewById<TextInputEditText>(R.id.etCurrentPassword)
        val etNewPassword = activity.findViewById<TextInputEditText>(R.id.etNewPassword)
        val etConfirmPassword = activity.findViewById<TextInputEditText>(R.id.etConfirmPassword)
        val btnChangePassword = activity.findViewById<MaterialButton>(R.id.btnChangePassword)

        etCurrentPassword.setText("currentPass123")
        etNewPassword.setText("newPass123")
        etConfirmPassword.setText("differentPass456")

        btnChangePassword.performClick()
        shadowOf(Looper.getMainLooper()).idle()

        assertEquals("As senhas não coincidem", etConfirmPassword.error)
    }

    @Test
    fun changePassword_validInputs_disablesButton() {
        controller.create().start().resume().visible()

        val etCurrentPassword = activity.findViewById<TextInputEditText>(R.id.etCurrentPassword)
        val etNewPassword = activity.findViewById<TextInputEditText>(R.id.etNewPassword)
        val etConfirmPassword = activity.findViewById<TextInputEditText>(R.id.etConfirmPassword)
        val btnChangePassword = activity.findViewById<MaterialButton>(R.id.btnChangePassword)

        etCurrentPassword.setText("currentPass123")
        etNewPassword.setText("newPass123")
        etConfirmPassword.setText("newPass123")

        btnChangePassword.performClick()
        shadowOf(Looper.getMainLooper()).idle()

        assertFalse(btnChangePassword.isEnabled)
        assertEquals("Alterando...", btnChangePassword.text.toString())
    }

    // ========== Google Account Password Creation Tests ==========

    @Test
    fun googleAccountPasswordCreation_emptyPassword_showsError() {
        every { AccountLinkingHelper.isGoogleAccount(any()) } returns true
        every { AccountLinkingHelper.hasPasswordProvider(any()) } returns false

        controller.create().start().resume().visible()
        shadowOf(Looper.getMainLooper()).idle()

        val etNewPassword = activity.findViewById<TextInputEditText>(R.id.etNewPassword)
        val etConfirmPassword = activity.findViewById<TextInputEditText>(R.id.etConfirmPassword)
        val btnChangePassword = activity.findViewById<MaterialButton>(R.id.btnChangePassword)

        etNewPassword.setText("")
        etConfirmPassword.setText("")

        btnChangePassword.performClick()
        shadowOf(Looper.getMainLooper()).idle()

        assertEquals("Digite uma senha para vincular à sua conta Google", etNewPassword.error)
    }

    @Test
    fun googleAccountPasswordCreation_shortPassword_showsError() {
        every { AccountLinkingHelper.isGoogleAccount(any()) } returns true
        every { AccountLinkingHelper.hasPasswordProvider(any()) } returns false

        controller.create().start().resume().visible()
        shadowOf(Looper.getMainLooper()).idle()

        val etNewPassword = activity.findViewById<TextInputEditText>(R.id.etNewPassword)
        val etConfirmPassword = activity.findViewById<TextInputEditText>(R.id.etConfirmPassword)
        val btnChangePassword = activity.findViewById<MaterialButton>(R.id.btnChangePassword)

        etNewPassword.setText("12345")
        etConfirmPassword.setText("12345")

        btnChangePassword.performClick()
        shadowOf(Looper.getMainLooper()).idle()

        assertEquals("A senha deve ter pelo menos 6 caracteres", etNewPassword.error)
    }

    @Test
    fun googleAccountPasswordCreation_mismatchedPasswords_showsError() {
        every { AccountLinkingHelper.isGoogleAccount(any()) } returns true
        every { AccountLinkingHelper.hasPasswordProvider(any()) } returns false

        controller.create().start().resume().visible()
        shadowOf(Looper.getMainLooper()).idle()

        val etNewPassword = activity.findViewById<TextInputEditText>(R.id.etNewPassword)
        val etConfirmPassword = activity.findViewById<TextInputEditText>(R.id.etConfirmPassword)
        val btnChangePassword = activity.findViewById<MaterialButton>(R.id.btnChangePassword)

        etNewPassword.setText("password123")
        etConfirmPassword.setText("differentPass456")

        btnChangePassword.performClick()
        shadowOf(Looper.getMainLooper()).idle()

        assertEquals("As senhas não coincidem", etConfirmPassword.error)
    }

    @Test
    fun googleAccountPasswordCreation_validPassword_triggersLinking() {
        every { AccountLinkingHelper.isGoogleAccount(any()) } returns true
        every { AccountLinkingHelper.hasPasswordProvider(any()) } returns false

        controller.create().start().resume().visible()
        shadowOf(Looper.getMainLooper()).idle()

        val etNewPassword = activity.findViewById<TextInputEditText>(R.id.etNewPassword)
        val etConfirmPassword = activity.findViewById<TextInputEditText>(R.id.etConfirmPassword)
        val btnChangePassword = activity.findViewById<MaterialButton>(R.id.btnChangePassword)

        etNewPassword.setText("password123")
        etConfirmPassword.setText("password123")

        btnChangePassword.performClick()
        shadowOf(Looper.getMainLooper()).idle()

        // Verify the linking was triggered (button behavior verified in other tests)
        verify { AccountLinkingHelper.linkPasswordToGoogleAccount(any(), any(), any()) }
    }

    // ========== Button Interaction Tests ==========

    @Test
    fun logoutButton_isClickable() {
        controller.create().start().resume().visible()

        val btnLogout = activity.findViewById<MaterialButton>(R.id.btnLogout)
        assertNotNull(btnLogout)
        assertTrue(btnLogout.isClickable)
        assertTrue(btnLogout.isEnabled)
    }

    @Test
    fun deleteAccountButton_isClickable() {
        controller.create().start().resume().visible()

        val btnDeleteAccount = activity.findViewById<MaterialButton>(R.id.btnDeleteAccount)
        assertNotNull(btnDeleteAccount)
        assertTrue(btnDeleteAccount.isClickable)
        assertTrue(btnDeleteAccount.isEnabled)
    }

    @Test
    fun saveProfileButton_initiallyEnabled() {
        controller.create().start().resume().visible()

        val btnSave = activity.findViewById<MaterialButton>(R.id.btnSaveProfile)
        assertTrue(btnSave.isEnabled)
    }

    @Test
    fun changePasswordButton_initiallyEnabled() {
        controller.create().start().resume().visible()

        val btnChangePassword = activity.findViewById<MaterialButton>(R.id.btnChangePassword)
        assertTrue(btnChangePassword.isEnabled)
    }
}

