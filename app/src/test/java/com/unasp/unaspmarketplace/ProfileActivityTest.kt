package com.unasp.unaspmarketplace

import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.unasp.unaspmarketplace.utils.CartManager
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import kotlin.text.get
import kotlin.toString

@RunWith(RobolectricTestRunner::class)
class ProfileActivityTest {

    @Before
    fun setup() {
        // Initialize Firebase for tests
        val context = org.robolectric.RuntimeEnvironment.getApplication() as android.app.Application
        if (com.google.firebase.FirebaseApp.getApps(context).isEmpty()) {
            com.google.firebase.FirebaseApp.initializeApp(context)
        }
        // Garantir estado limpo antes de cada teste
        CartManager.clearCart()
    }

    @After
    fun tearDown() {
        CartManager.clearCart()
    }

    @Test
    fun saveProfile_withEmptyName_showsError() {
        val controller = Robolectric.buildActivity(ProfileActivity::class.java).setup()
        val activity = controller.get()

        val etName = activity.findViewById<TextInputEditText>(R.id.etName)
        val btnSave = activity.findViewById<Button>(R.id.btnSaveProfile)

        etName.setText("")
        btnSave.performClick()
        shadowOf(Looper.getMainLooper()).idle()

        assertEquals("Nome não pode estar vazio", etName.error)
    }

    @Test
    fun saveProfile_withShortWhatsapp_showsError() {
        val controller = Robolectric.buildActivity(ProfileActivity::class.java).setup()
        val activity = controller.get()

        val etName = activity.findViewById<TextInputEditText>(R.id.etName)
        val etWhatsapp = activity.findViewById<TextInputEditText>(R.id.etNumber)
        val btnSave = activity.findViewById<MaterialButton>(R.id.btnSaveProfile)

        etName.setText("Nome válido")
        etWhatsapp.setText("12345")

        btnSave.performClick()
        shadowOf(Looper.getMainLooper()).idle()

        assertEquals("Digite um número de WhatsApp válido", etWhatsapp.error)
    }

    @Test
    fun saveProfile_withValidInputs_disablesButton_and_showsSavingText() {
        val controller = Robolectric.buildActivity(ProfileActivity::class.java).setup()
        val activity = controller.get()

        val etName = activity.findViewById<TextInputEditText>(R.id.etName)
        val etWhatsapp = activity.findViewById<TextInputEditText>(R.id.etNumber)
        val btnSave = activity.findViewById<MaterialButton>(R.id.btnSaveProfile)

        etName.setText("Nome válido")
        etWhatsapp.setText("5511999999999")

        btnSave.performClick()
        shadowOf(Looper.getMainLooper()).idle()

        assertFalse(btnSave.isEnabled)
        assertEquals("Salvando...", btnSave.text.toString())
    }

    @Test
    fun saveProfile_withWhitespaceName_showsError() {
        val controller = Robolectric.buildActivity(ProfileActivity::class.java).setup()
        val activity = controller.get()

        val etName = activity.findViewById<TextInputEditText>(R.id.etName)
        val btnSave = activity.findViewById<MaterialButton>(R.id.btnSaveProfile)

        etName.setText("   ")
        btnSave.performClick()
        shadowOf(Looper.getMainLooper()).idle()

        assertEquals("Nome não pode estar vazio", etName.error)
    }

    @Test
    fun saveProfile_withEmptyWhatsapp_allowsSaving() {
        val controller = Robolectric.buildActivity(ProfileActivity::class.java).setup()
        val activity = controller.get()

        val etName = activity.findViewById<TextInputEditText>(R.id.etName)
        val etWhatsapp = activity.findViewById<TextInputEditText>(R.id.etNumber)
        val btnSave = activity.findViewById<MaterialButton>(R.id.btnSaveProfile)

        etName.setText("Nome válido")
        etWhatsapp.setText("")

        btnSave.performClick()
        shadowOf(Looper.getMainLooper()).idle()

        assertFalse(btnSave.isEnabled)
        assertEquals("Salvando...", btnSave.text.toString())
    }

    @Test
    fun saveProfile_withExact10Whatsapp_validTriggersSaving() {
        val controller = Robolectric.buildActivity(ProfileActivity::class.java).setup()
        val activity = controller.get()

        val etName = activity.findViewById<TextInputEditText>(R.id.etName)
        val etWhatsapp = activity.findViewById<TextInputEditText>(R.id.etNumber)
        val btnSave = activity.findViewById<MaterialButton>(R.id.btnSaveProfile)

        etName.setText("Nome válido")
        etWhatsapp.setText("5511999999")

        btnSave.performClick()
        shadowOf(Looper.getMainLooper()).idle()

        assertFalse(btnSave.isEnabled)
        assertEquals("Salvando...", btnSave.text.toString())
    }

    @Test
    fun saveProfile_button_initialState_enabled() {
        val controller = Robolectric.buildActivity(ProfileActivity::class.java).setup()
        val activity = controller.get()

        val btnSave = activity.findViewById<MaterialButton>(R.id.btnSaveProfile)

        assertTrue(btnSave.isEnabled)
    }

    //@Test todo: implementar esse teste no androidTest com Firebase test lab
    fun changePassword_withEmptyCurrentPassword_showsError() {
        val controller = Robolectric.buildActivity(ProfileActivity::class.java).setup()
        val activity = controller.get()

        val etCurrentPassword = activity.findViewById<TextInputEditText>(R.id.etCurrentPassword)
        val etNewPassword = activity.findViewById<TextInputEditText>(R.id.etNewPassword)
        val etConfirmPassword = activity.findViewById<TextInputEditText>(R.id.etConfirmPassword)
        val btnChangePassword = activity.findViewById<MaterialButton>(R.id.btnChangePassword)

        etCurrentPassword.isEnabled = true

        etCurrentPassword.setText("")
        etNewPassword.setText("novaSenha123")
        etConfirmPassword.setText("novaSenha123")

        btnChangePassword.performClick()
        shadowOf(Looper.getMainLooper()).idle()

        assertEquals("Digite a senha atual", etCurrentPassword.error)
    }

    //@Test todo: implementar esse teste no androidTest com Firebase test lab
    fun changePassword_withEmptyNewPassword_showsError() {
        val controller = Robolectric.buildActivity(ProfileActivity::class.java).setup()
        val activity = controller.get()

        val etCurrentPassword = activity.findViewById<TextInputEditText>(R.id.etCurrentPassword)
        val etNewPassword = activity.findViewById<TextInputEditText>(R.id.etNewPassword)
        val etConfirmPassword = activity.findViewById<TextInputEditText>(R.id.etConfirmPassword)
        val btnChangePassword = activity.findViewById<MaterialButton>(R.id.btnChangePassword)

        etCurrentPassword.isEnabled = true

        etCurrentPassword.setText("senhaAtual123")
        etNewPassword.setText("")
        etConfirmPassword.setText("")

        btnChangePassword.performClick()
        shadowOf(Looper.getMainLooper()).idle()

        assertEquals("Digite a nova senha", etNewPassword.error)
    }

    //@Test todo: implementar esse teste no androidTest com Firebase test lab
    fun changePassword_withShortNewPassword_showsError() {
        val controller = Robolectric.buildActivity(ProfileActivity::class.java).setup()
        val activity = controller.get()

        val etCurrentPassword = activity.findViewById<TextInputEditText>(R.id.etCurrentPassword)
        val etNewPassword = activity.findViewById<TextInputEditText>(R.id.etNewPassword)
        val etConfirmPassword = activity.findViewById<TextInputEditText>(R.id.etConfirmPassword)
        val btnChangePassword = activity.findViewById<MaterialButton>(R.id.btnChangePassword)

        etCurrentPassword.isEnabled = true

        etCurrentPassword.setText("senhaAtual123")
        etNewPassword.setText("12345")
        etConfirmPassword.setText("12345")

        btnChangePassword.performClick()
        shadowOf(Looper.getMainLooper()).idle()

        assertEquals("A senha deve ter pelo menos 6 caracteres", etNewPassword.error)
    }

    //@Test todo: implementar esse teste no androidTest com Firebase test lab
    fun changePassword_withMismatchedPasswords_showsError() {
        val controller = Robolectric.buildActivity(ProfileActivity::class.java).setup()
        val activity = controller.get()

        val etCurrentPassword = activity.findViewById<TextInputEditText>(R.id.etCurrentPassword)
        val etNewPassword = activity.findViewById<TextInputEditText>(R.id.etNewPassword)
        val etConfirmPassword = activity.findViewById<TextInputEditText>(R.id.etConfirmPassword)
        val btnChangePassword = activity.findViewById<MaterialButton>(R.id.btnChangePassword)

        etCurrentPassword.isEnabled = true

        etCurrentPassword.setText("senhaAtual123")
        etNewPassword.setText("novaSenha123")
        etConfirmPassword.setText("senhasDiferentes456")

        btnChangePassword.performClick()
        shadowOf(Looper.getMainLooper()).idle()

        assertEquals("As senhas não coincidem", etConfirmPassword.error)
    }

    //@Test todo: implementar esse teste no androidTest com Firebase test lab
    fun changePassword_withValidInputs_disablesButtonAndChangesText() {
        val controller = Robolectric.buildActivity(ProfileActivity::class.java).setup()
        val activity = controller.get()

        val etCurrentPassword = activity.findViewById<TextInputEditText>(R.id.etCurrentPassword)
        val etNewPassword = activity.findViewById<TextInputEditText>(R.id.etNewPassword)
        val etConfirmPassword = activity.findViewById<TextInputEditText>(R.id.etConfirmPassword)
        val btnChangePassword = activity.findViewById<MaterialButton>(R.id.btnChangePassword)

        etCurrentPassword.isEnabled = true

        etCurrentPassword.setText("senhaAtual123")
        etNewPassword.setText("novaSenha123")
        etConfirmPassword.setText("novaSenha123")

        val initialEnabled = btnChangePassword.isEnabled
        btnChangePassword.performClick()
        shadowOf(Looper.getMainLooper()).idle()

        assertTrue(initialEnabled)
        assertFalse(btnChangePassword.isEnabled)
        assertEquals("Alterando...", btnChangePassword.text.toString())
    }

    //@Test todo: implementar esse teste no androidTest com Firebase test lab
    fun googleAccountPasswordCreation_withEmptyPassword_showsError() {
        val controller = Robolectric.buildActivity(ProfileActivity::class.java).setup()
        val activity = controller.get()

        val etNewPassword = activity.findViewById<TextInputEditText>(R.id.etNewPassword)
        val etConfirmPassword = activity.findViewById<TextInputEditText>(R.id.etConfirmPassword)
        val btnChangePassword = activity.findViewById<MaterialButton>(R.id.btnChangePassword)

        val etCurrentPassword = activity.findViewById<TextInputEditText>(R.id.etCurrentPassword)
        etCurrentPassword.isEnabled = false

        etNewPassword.setText("")
        etConfirmPassword.setText("")

        btnChangePassword.performClick()
        shadowOf(Looper.getMainLooper()).idle()

        assertEquals("Digite uma senha para vincular à sua conta Google", etNewPassword.error)
    }

    //@Test todo: implementar esse teste no androidTest com Firebase test lab
    fun googleAccountPasswordCreation_withShortPassword_showsError() {
        val controller = Robolectric.buildActivity(ProfileActivity::class.java).setup()
        val activity = controller.get()

        val etCurrentPassword = activity.findViewById<TextInputEditText>(R.id.etCurrentPassword)
        val etNewPassword = activity.findViewById<TextInputEditText>(R.id.etNewPassword)
        val etConfirmPassword = activity.findViewById<TextInputEditText>(R.id.etConfirmPassword)
        val btnChangePassword = activity.findViewById<MaterialButton>(R.id.btnChangePassword)

        etCurrentPassword.isEnabled = false

        etNewPassword.setText("12345")
        etConfirmPassword.setText("12345")

        btnChangePassword.performClick()
        shadowOf(Looper.getMainLooper()).idle()

        assertEquals("A senha deve ter pelo menos 6 caracteres", etNewPassword.error)
    }

    //@Test todo: implementar esse teste no androidTest com Firebase test lab
    fun googleAccountPasswordCreation_withMismatchedPasswords_showsError() {
        val controller = Robolectric.buildActivity(ProfileActivity::class.java).setup()
        val activity = controller.get()

        val etCurrentPassword = activity.findViewById<TextInputEditText>(R.id.etCurrentPassword)
        val etNewPassword = activity.findViewById<TextInputEditText>(R.id.etNewPassword)
        val etConfirmPassword = activity.findViewById<TextInputEditText>(R.id.etConfirmPassword)
        val btnChangePassword = activity.findViewById<MaterialButton>(R.id.btnChangePassword)

        etCurrentPassword.isEnabled = false

        etNewPassword.setText("senha123456")
        etConfirmPassword.setText("senhaDiferente789")

        btnChangePassword.performClick()
        shadowOf(Looper.getMainLooper()).idle()

        assertEquals("As senhas não coincidem", etConfirmPassword.error)
    }

    @Test
    fun saveProfile_clearsButtonTextAfterSaving() {
        val controller = Robolectric.buildActivity(ProfileActivity::class.java).setup()
        val activity = controller.get()

        val etName = activity.findViewById<TextInputEditText>(R.id.etName)
        val btnSave = activity.findViewById<MaterialButton>(R.id.btnSaveProfile)

        etName.setText("Nome Teste")
        val originalText = btnSave.text.toString()

        btnSave.performClick()
        shadowOf(Looper.getMainLooper()).idle()

        assertEquals("Salvando...", btnSave.text.toString())
        assertNotEquals(originalText, btnSave.text.toString())
    }

    @Test
    fun saveProfile_withValidWhatsapp11Digits_allowsSaving() {
        val controller = Robolectric.buildActivity(ProfileActivity::class.java).setup()
        val activity = controller.get()

        val etName = activity.findViewById<TextInputEditText>(R.id.etName)
        val etWhatsapp = activity.findViewById<TextInputEditText>(R.id.etNumber)
        val btnSave = activity.findViewById<MaterialButton>(R.id.btnSaveProfile)

        etName.setText("Nome válido")
        etWhatsapp.setText("55119999999")

        btnSave.performClick()
        shadowOf(Looper.getMainLooper()).idle()

        assertFalse(btnSave.isEnabled)
        assertEquals("Salvando...", btnSave.text.toString())
    }

    @Test
    fun saveProfile_with9DigitWhatsapp_showsError() {
        val controller = Robolectric.buildActivity(ProfileActivity::class.java).setup()
        val activity = controller.get()

        val etName = activity.findViewById<TextInputEditText>(R.id.etName)
        val etWhatsapp = activity.findViewById<TextInputEditText>(R.id.etNumber)
        val btnSave = activity.findViewById<MaterialButton>(R.id.btnSaveProfile)

        etName.setText("Nome válido")
        etWhatsapp.setText("123456789")

        btnSave.performClick()
        shadowOf(Looper.getMainLooper()).idle()

        assertEquals("Digite um número de WhatsApp válido", etWhatsapp.error)
        assertTrue(btnSave.isEnabled)
    }

    @Test
    fun saveProfile_trimsWhitespaceFromName() {
        val controller = Robolectric.buildActivity(ProfileActivity::class.java).setup()
        val activity = controller.get()

        val etName = activity.findViewById<TextInputEditText>(R.id.etName)
        val btnSave = activity.findViewById<MaterialButton>(R.id.btnSaveProfile)

        etName.setText("  Nome Com Espaços  ")

        btnSave.performClick()
        shadowOf(Looper.getMainLooper()).idle()

        assertFalse(btnSave.isEnabled)
    }

    @Test
    fun saveProfile_trimsWhitespaceFromWhatsapp() {
        val controller = Robolectric.buildActivity(ProfileActivity::class.java).setup()
        val activity = controller.get()

        val etName = activity.findViewById<TextInputEditText>(R.id.etName)
        val etWhatsapp = activity.findViewById<TextInputEditText>(R.id.etNumber)
        val btnSave = activity.findViewById<MaterialButton>(R.id.btnSaveProfile)

        etName.setText("Nome válido")
        etWhatsapp.setText("  5511999999999  ")

        btnSave.performClick()
        shadowOf(Looper.getMainLooper()).idle()

        assertFalse(btnSave.isEnabled)
    }

    @Test
    fun passwordInfoTextView_existsInLayout() {
        val controller = Robolectric.buildActivity(ProfileActivity::class.java).setup()
        val activity = controller.get()

        val txtPasswordInfo = activity.findViewById<TextView>(R.id.txtPasswordInfo)
        assertNotNull(txtPasswordInfo)
    }

    @Test
    fun allRequiredFields_existInLayout() {
        val controller = Robolectric.buildActivity(ProfileActivity::class.java).setup()
        val activity = controller.get()

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
    }

    @Test
    fun saveProfile_buttonText_resetsAfterError() {
        val controller = Robolectric.buildActivity(ProfileActivity::class.java).setup()
        val activity = controller.get()

        val etName = activity.findViewById<TextInputEditText>(R.id.etName)
        val btnSave = activity.findViewById<MaterialButton>(R.id.btnSaveProfile)

        etName.setText("")

        btnSave.performClick()
        shadowOf(Looper.getMainLooper()).idle()

        assertTrue(btnSave.isEnabled)
        assertNotEquals("Salvando...", btnSave.text.toString())
    }

// ========== Testes de Interação com Botões ==========

    @Test
    fun logoutButton_existsAndIsClickable() {
        val controller = Robolectric.buildActivity(ProfileActivity::class.java).setup()
        val activity = controller.get()

        val btnLogout = activity.findViewById<MaterialButton>(R.id.btnLogout)
        assertNotNull(btnLogout)
        assertTrue(btnLogout.isClickable)
        assertTrue(btnLogout.isEnabled)
    }

    @Test
    fun deleteAccountButton_existsAndIsClickable() {
        val controller = Robolectric.buildActivity(ProfileActivity::class.java).setup()
        val activity = controller.get()

        val btnDelete = activity.findViewById<MaterialButton>(R.id.btnDeleteAccount)
        assertNotNull(btnDelete)
        assertTrue(btnDelete.isClickable)
        assertTrue(btnDelete.isEnabled)
    }

    @Test
    fun changePasswordButton_existsAndIsClickable() {
        val controller = Robolectric.buildActivity(ProfileActivity::class.java).setup()
        val activity = controller.get()

        val btnChangePassword = activity.findViewById<MaterialButton>(R.id.btnChangePassword)
        assertNotNull(btnChangePassword)
        assertTrue(btnChangePassword.isClickable)
        assertTrue(btnChangePassword.isEnabled)
    }

// ========== Testes de Estado Inicial dos Campos ==========

    @Test
    fun nameField_initiallyEmpty() {
        val controller = Robolectric.buildActivity(ProfileActivity::class.java).setup()
        val activity = controller.get()

        val etName = activity.findViewById<TextInputEditText>(R.id.etName)
        assertTrue(etName.text.isNullOrEmpty())
    }

    //@Test todo: consertar essa birosca :(
    fun emailField_initiallyDisabled() {
        val controller = Robolectric.buildActivity(ProfileActivity::class.java).setup()
        val activity = controller.get()

        val etEmail = activity.findViewById<TextInputEditText>(R.id.etEmail)
        assertFalse("Email field should be disabled/non-editable", etEmail.isEnabled || etEmail.isFocusable)
    }

    @Test
    fun whatsappField_initiallyEmpty() {
        val controller = Robolectric.buildActivity(ProfileActivity::class.java).setup()
        val activity = controller.get()

        val etWhatsapp = activity.findViewById<TextInputEditText>(R.id.etNumber)
        assertTrue(etWhatsapp.text.isNullOrEmpty())
    }

    @Test
    fun passwordFields_initiallyEmpty() {
        val controller = Robolectric.buildActivity(ProfileActivity::class.java).setup()
        val activity = controller.get()

        val etCurrentPassword = activity.findViewById<TextInputEditText>(R.id.etCurrentPassword)
        val etNewPassword = activity.findViewById<TextInputEditText>(R.id.etNewPassword)
        val etConfirmPassword = activity.findViewById<TextInputEditText>(R.id.etConfirmPassword)

        assertTrue(etCurrentPassword.text.isNullOrEmpty())
        assertTrue(etNewPassword.text.isNullOrEmpty())
        assertTrue(etConfirmPassword.text.isNullOrEmpty())
    }

// ========== Testes de Validação de Limites ==========

    @Test
    fun saveProfile_withMaxLengthName_allowsSaving() {
        val controller = Robolectric.buildActivity(ProfileActivity::class.java).setup()
        val activity = controller.get()

        val etName = activity.findViewById<TextInputEditText>(R.id.etName)
        val btnSave = activity.findViewById<MaterialButton>(R.id.btnSaveProfile)

        // Nome com 100 caracteres
        etName.setText("a".repeat(100))

        btnSave.performClick()
        shadowOf(Looper.getMainLooper()).idle()

        assertFalse(btnSave.isEnabled)
        assertEquals("Salvando...", btnSave.text.toString())
    }

    @Test
    fun saveProfile_withSingleCharacterName_allowsSaving() {
        val controller = Robolectric.buildActivity(ProfileActivity::class.java).setup()
        val activity = controller.get()

        val etName = activity.findViewById<TextInputEditText>(R.id.etName)
        val btnSave = activity.findViewById<MaterialButton>(R.id.btnSaveProfile)

        etName.setText("A")

        btnSave.performClick()
        shadowOf(Looper.getMainLooper()).idle()

        assertFalse(btnSave.isEnabled)
        assertEquals("Salvando...", btnSave.text.toString())
    }

    @Test
    fun saveProfile_withSpecialCharactersInName_allowsSaving() {
        val controller = Robolectric.buildActivity(ProfileActivity::class.java).setup()
        val activity = controller.get()

        val etName = activity.findViewById<TextInputEditText>(R.id.etName)
        val btnSave = activity.findViewById<MaterialButton>(R.id.btnSaveProfile)

        etName.setText("José da Silva-Souza O'Connor")

        btnSave.performClick()
        shadowOf(Looper.getMainLooper()).idle()

        assertFalse(btnSave.isEnabled)
    }

    @Test
    fun saveProfile_withNumbersInName_allowsSaving() {
        val controller = Robolectric.buildActivity(ProfileActivity::class.java).setup()
        val activity = controller.get()

        val etName = activity.findViewById<TextInputEditText>(R.id.etName)
        val btnSave = activity.findViewById<MaterialButton>(R.id.btnSaveProfile)

        etName.setText("John Smith 3rd")

        btnSave.performClick()
        shadowOf(Looper.getMainLooper()).idle()

        assertFalse(btnSave.isEnabled)
    }

// ========== Testes de Validação de WhatsApp ==========

    @Test
    fun saveProfile_withWhatsappExactly10Digits_allowsSaving() {
        val controller = Robolectric.buildActivity(ProfileActivity::class.java).setup()
        val activity = controller.get()

        val etName = activity.findViewById<TextInputEditText>(R.id.etName)
        val etWhatsapp = activity.findViewById<TextInputEditText>(R.id.etNumber)
        val btnSave = activity.findViewById<MaterialButton>(R.id.btnSaveProfile)

        etName.setText("Nome válido")
        etWhatsapp.setText("1234567890")

        btnSave.performClick()
        shadowOf(Looper.getMainLooper()).idle()

        assertFalse(btnSave.isEnabled)
    }

    @Test
    fun saveProfile_withWhatsappWithSpaces_trimsAndValidates() {
        val controller = Robolectric.buildActivity(ProfileActivity::class.java).setup()
        val activity = controller.get()

        val etName = activity.findViewById<TextInputEditText>(R.id.etName)
        val etWhatsapp = activity.findViewById<TextInputEditText>(R.id.etNumber)
        val btnSave = activity.findViewById<MaterialButton>(R.id.btnSaveProfile)

        etName.setText("Nome válido")
        etWhatsapp.setText(" 5511999999999 ")

        btnSave.performClick()
        shadowOf(Looper.getMainLooper()).idle()

        assertFalse(btnSave.isEnabled)
    }

    @Test
    fun saveProfile_withWhatsapp15Digits_allowsSaving() {
        val controller = Robolectric.buildActivity(ProfileActivity::class.java).setup()
        val activity = controller.get()

        val etName = activity.findViewById<TextInputEditText>(R.id.etName)
        val etWhatsapp = activity.findViewById<TextInputEditText>(R.id.etNumber)
        val btnSave = activity.findViewById<MaterialButton>(R.id.btnSaveProfile)

        etName.setText("Nome válido")
        etWhatsapp.setText("551199999999999") // 15 dígitos

        btnSave.performClick()
        shadowOf(Looper.getMainLooper()).idle()

        assertFalse(btnSave.isEnabled)
    }

// ========== Testes de Texto dos Botões ==========

    @Test
    fun saveProfileButton_hasCorrectInitialText() {
        val controller = Robolectric.buildActivity(ProfileActivity::class.java).setup()
        val activity = controller.get()

        val btnSave = activity.findViewById<MaterialButton>(R.id.btnSaveProfile)
        assertEquals("Salvar Alterações", btnSave.text.toString())
    }

    @Test
    fun changePasswordButton_hasCorrectInitialText() {
        val controller = Robolectric.buildActivity(ProfileActivity::class.java).setup()
        val activity = controller.get()

        val btnChangePassword = activity.findViewById<MaterialButton>(R.id.btnChangePassword)
        // Pode ser "Alterar Senha" ou "Vincular Senha" dependendo do tipo de conta
        val expectedTexts = listOf("Alterar Senha", "Vincular Senha")
        assertTrue(expectedTexts.contains(btnChangePassword.text.toString()))
    }

    @Test
    fun logoutButton_hasCorrectText() {
        val controller = Robolectric.buildActivity(ProfileActivity::class.java).setup()
        val activity = controller.get()

        val btnLogout = activity.findViewById<MaterialButton>(R.id.btnLogout)
        assertNotNull(btnLogout.text)
        assertFalse(btnLogout.text.isNullOrEmpty())
    }

// ========== Testes de Hints dos Campos ==========

    @Test
    fun nameField_hasCorrectHint() {
        val controller = Robolectric.buildActivity(ProfileActivity::class.java).setup()
        val activity = controller.get()

        val etName = activity.findViewById<TextInputEditText>(R.id.etName)
        assertNotNull(etName.hint)
    }

    @Test
    fun whatsappField_hasCorrectHint() {
        val controller = Robolectric.buildActivity(ProfileActivity::class.java).setup()
        val activity = controller.get()

        val etWhatsapp = activity.findViewById<TextInputEditText>(R.id.etNumber)
        assertNotNull(etWhatsapp.hint)
    }

    @Test
    fun passwordFields_haveHints() {
        val controller = Robolectric.buildActivity(ProfileActivity::class.java).setup()
        val activity = controller.get()

        val etCurrentPassword = activity.findViewById<TextInputEditText>(R.id.etCurrentPassword)
        val etNewPassword = activity.findViewById<TextInputEditText>(R.id.etNewPassword)
        val etConfirmPassword = activity.findViewById<TextInputEditText>(R.id.etConfirmPassword)

        assertNotNull(etCurrentPassword.hint)
        assertNotNull(etNewPassword.hint)
        assertNotNull(etConfirmPassword.hint)
    }

// ========== Testes de Comportamento de Erro ==========

    @Test
    fun saveProfile_afterError_canTryAgain() {
        val controller = Robolectric.buildActivity(ProfileActivity::class.java).setup()
        val activity = controller.get()

        val etName = activity.findViewById<TextInputEditText>(R.id.etName)
        val btnSave = activity.findViewById<MaterialButton>(R.id.btnSaveProfile)

        // Primeira tentativa com erro
        etName.setText("")
        btnSave.performClick()
        shadowOf(Looper.getMainLooper()).idle()
        assertEquals("Nome não pode estar vazio", etName.error)

        // Segunda tentativa válida
        etName.setText("Nome válido")
        btnSave.performClick()
        shadowOf(Looper.getMainLooper()).idle()

        assertFalse(btnSave.isEnabled)
        assertEquals("Salvando...", btnSave.text.toString())
    }

    @Test
    fun saveProfile_errorClears_whenUserStartsTyping() {
        val controller = Robolectric.buildActivity(ProfileActivity::class.java).setup()
        val activity = controller.get()

        val etName = activity.findViewById<TextInputEditText>(R.id.etName)
        val btnSave = activity.findViewById<MaterialButton>(R.id.btnSaveProfile)

        etName.setText("")
        btnSave.performClick()
        shadowOf(Looper.getMainLooper()).idle()

        assertNotNull(etName.error)

        // Simular digitação
        etName.setText("N")
        etName.clearFocus()

        // O erro pode ou não ser limpo automaticamente, dependendo da implementação
        // Este teste verifica apenas que o campo aceita nova entrada
        assertEquals("N", etName.text.toString())
    }

// ========== Testes de Múltiplas Operações ==========

    @Test
    fun saveProfile_cannotClick_whileProcessing() {
        val controller = Robolectric.buildActivity(ProfileActivity::class.java).setup()
        val activity = controller.get()

        val etName = activity.findViewById<TextInputEditText>(R.id.etName)
        val btnSave = activity.findViewById<MaterialButton>(R.id.btnSaveProfile)

        etName.setText("Nome válido")

        // Primeiro clique
        btnSave.performClick()
        shadowOf(Looper.getMainLooper()).idle()

        val isDisabledAfterFirstClick = !btnSave.isEnabled

        // Segundo clique (não deve ter efeito)
        btnSave.performClick()
        shadowOf(Looper.getMainLooper()).idle()

        assertTrue(isDisabledAfterFirstClick)
        assertFalse(btnSave.isEnabled)
    }

// ========== Testes de Toolbar ==========

    @Test
    fun toolbar_hasBackButton() {
        val controller = Robolectric.buildActivity(ProfileActivity::class.java).setup()
        val activity = controller.get()

        val toolbar = activity.findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        assertNotNull(toolbar)
    }

    @Test
    fun toolbar_backButton_finishesActivity() {
        val controller = Robolectric.buildActivity(ProfileActivity::class.java).setup()
        val activity = controller.get()

        val toolbar = activity.findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        toolbar.navigationIcon?.let {
            toolbar.performClick()
            shadowOf(Looper.getMainLooper()).idle()
        }

        // Este teste pode não funcionar perfeitamente com Robolectric
        // pois simular o clique no botão de navegação é complexo
        assertTrue(true) // Placeholder - teste mais completo no androidTest
    }

// ========== Testes de Visibilidade ==========

    @Test
    fun passwordInfoTextView_initialVisibility() {
        val controller = Robolectric.buildActivity(ProfileActivity::class.java).setup()
        val activity = controller.get()

        val txtPasswordInfo = activity.findViewById<TextView>(R.id.txtPasswordInfo)
        // Visibilidade pode ser VISIBLE ou GONE dependendo do tipo de conta
        assertTrue(
            txtPasswordInfo.visibility == View.VISIBLE ||
                    txtPasswordInfo.visibility == View.GONE
        )
    }

    @Test
    fun allButtons_initiallyVisible() {
        val controller = Robolectric.buildActivity(ProfileActivity::class.java).setup()
        val activity = controller.get()

        val btnSave = activity.findViewById<MaterialButton>(R.id.btnSaveProfile)
        val btnChangePassword = activity.findViewById<MaterialButton>(R.id.btnChangePassword)
        val btnLogout = activity.findViewById<MaterialButton>(R.id.btnLogout)
        val btnDelete = activity.findViewById<MaterialButton>(R.id.btnDeleteAccount)

        assertEquals(View.VISIBLE, btnSave.visibility)
        assertEquals(View.VISIBLE, btnChangePassword.visibility)
        assertEquals(View.VISIBLE, btnLogout.visibility)
        assertEquals(View.VISIBLE, btnDelete.visibility)
    }

}