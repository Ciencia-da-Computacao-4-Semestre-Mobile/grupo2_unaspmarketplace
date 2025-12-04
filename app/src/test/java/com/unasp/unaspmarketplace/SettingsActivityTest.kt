package com.unasp.unaspmarketplace

import android.content.Context
import android.widget.Switch
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SettingsActivityTest {

    @Test
    fun loadsPreferences_and_updatesUi() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        val prefs = ctx.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        // Apenas setar preferência de notificações que é segura para testes unitários
        prefs.edit()
            .putBoolean("notifications_enabled", false)
            .apply()

        val controller = Robolectric.buildActivity(SettingsActivity::class.java).setup()
        val activity = controller.get()

        val switchNotifications = activity.findViewById<Switch>(R.id.switch_notifications)

        assertFalse(switchNotifications.isChecked)
    }
}