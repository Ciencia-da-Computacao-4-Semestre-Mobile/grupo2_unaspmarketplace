package com.unasp.unaspmarketplace.data.model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.unasp.unaspmarketplace.data.repository.AuthService

class LoginViewModel : ViewModel() {
    private val authService = AuthService()
    val loginState = MutableLiveData<Boolean>()

    fun login(email: String, password: String) {
        authService.loginUser(email, password) { success, _ ->
            loginState.postValue(success)
        }
    }
}