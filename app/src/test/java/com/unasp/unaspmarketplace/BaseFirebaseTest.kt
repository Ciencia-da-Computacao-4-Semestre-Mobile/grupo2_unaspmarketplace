package com.unasp.unaspmarketplace

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import org.junit.After
import org.junit.Before
import org.robolectric.RuntimeEnvironment

open class BaseFirebaseTest {

    protected lateinit var mockAuth: FirebaseAuth

    companion object {
        private var firebaseInitialized = false
    }

    @Before
    open fun setupFirebase() {
        val context = RuntimeEnvironment.getApplication() as Application

        // Initialize Firebase only once per test run
        if (!firebaseInitialized && FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(context)
            firebaseInitialized = true
        }

        setupMocks()
    }

    protected open fun setup() {
        setupFirebase()
    }

    @After
    open fun tearDownFirebase() {
        clearAllMocks()
    }

    private fun setupMocks() {
        // Mock FirebaseAuth
        mockAuth = mockk(relaxed = true)
        mockkStatic(FirebaseAuth::class)
        every { FirebaseAuth.getInstance() } returns mockAuth

        // Mock FirebaseFirestore to prevent persistence layer overhead
        val mockFirestore = mockk<FirebaseFirestore>(relaxed = true)
        mockkStatic(FirebaseFirestore::class)
        every { FirebaseFirestore.getInstance() } returns mockFirestore
    }
}



