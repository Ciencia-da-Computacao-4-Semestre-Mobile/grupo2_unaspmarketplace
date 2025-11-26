package com.unasp.unaspmarketplace.data.model

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.unasp.unaspmarketplace.data.repository.AuthService

class LoginViewModel : ViewModel() {
    private val authService = AuthService()
    val loginState = MutableLiveData<Boolean>()
    val errorMessage = MutableLiveData<String>()

    fun login(email: String, password: String) {
        Log.d("LoginViewModel", "Attempting login for email: $email")

        authService.loginUser(email, password) { success, error ->
            Log.d("LoginViewModel", "Login result: success=$success, error=$error")

            loginState.postValue(success)
            if (!success && error != null) {
                // Se por algum motivo 'error' ainda for nulo aqui, usa uma string padrão.
                // Isso garante para o Lint que NUNCA passaremos null.
                errorMessage.postValue(error ?: "Ocorreu um erro desconhecido.")
            }


        }
    }
}