package com.unasp.unaspmarketplace

import android.app.Application
import android.os.Looper
import android.widget.Button
import com.google.android.material.R as MaterialR
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.FirebaseApp
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import org.junit.After
import com.unasp.unaspmarketplace.utils.UserUtils
import com.unasp.unaspmarketplace.models.User

@RunWith(RobolectricTestRunner::class)
class PostItemActivityTest {

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(context)
        }

        // Mockar UserUtils para sempre retornar um usuário com whatsapp preenchido
        mockkObject(UserUtils)
        val fakeUser = mockk<User>(relaxed = true)
        coEvery { UserUtils.getCurrentUser() } returns fakeUser
        coEvery { fakeUser.whatsappNumber } returns "5511999999999"
    }

    @After
    fun tearDown() {
        // limpar mocks para não colidir com outros testes
        unmockkAll()
    }

    @Test
    fun viewsAreInitialized_and_removeButtonInitiallyDisabled() {
        val controller = Robolectric.buildActivity(PostItemActivity::class.java).create()
        controller.get().setTheme(MaterialR.style.Theme_MaterialComponents_Light_NoActionBar_Bridge)
        controller.start().resume().visible()
        val activity = controller.get()

        val btnRemove = activity.findViewById<Button>(R.id.btnRemoveImage)
        assertNotNull(btnRemove)
        assertFalse("Botão remover deve iniciar desabilitado", btnRemove.isEnabled)
    }

    @Test
    fun save_withEmptyName_showsNameError() {
        val controller = Robolectric.buildActivity(PostItemActivity::class.java).create()
        controller.get().setTheme(MaterialR.style.Theme_MaterialComponents_Light_NoActionBar_Bridge)
        controller.start().resume().visible()
        val activity = controller.get()

        val edtName = activity.findViewById<TextInputEditText>(R.id.edtName)
        val btnSave = activity.findViewById<Button>(R.id.btnSave)

        // Executar alterações e clique na UI thread para disparar validação corretamente
        activity.runOnUiThread {
            edtName.setText("")
            btnSave.performClick()
        }

        // aguarda tasks do Looper para que validação / UI updates sejam aplicados
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        val til = activity.findViewById<TextInputLayout>(R.id.tilName)
        val errorText = til?.error?.toString() ?: edtName.error?.toString()

        assertNotNull("Esperava erro de validação no campo nome", errorText)
        assertEquals("Nome é obrigatório", errorText)
    }
}
