package com.unasp.unaspmarketplace

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import org.junit.After
import org.junit.Before
import org.robolectric.RuntimeEnvironment

open class BaseFirebaseTest {

    protected lateinit var mockAuth: FirebaseAuth
    private var firebaseInitialized = false

    @Before
    open fun setupFirebase() {
        val context = RuntimeEnvironment.getApplication() as Application

        // Inicializar Firebase apenas uma vez
        if (!firebaseInitialized && FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(context)
            firebaseInitialized = true
        }

        // Mock FirebaseAuth
        mockAuth = mockk(relaxed = true)
        mockkStatic(FirebaseAuth::class)
        every { FirebaseAuth.getInstance() } returns mockAuth

        // Prevent Firestore from creating persistence/threads by mocking the instance
        val mockFs = mockk<FirebaseFirestore>(relaxed = true)
        mockkStatic(FirebaseFirestore::class)
        every { FirebaseFirestore.getInstance() } returns mockFs
    }

    @After
    open fun tearDownFirebase() {
        // Limpar apenas os mocks, não o Firebase
        io.mockk.clearAllMocks()
    }
}
