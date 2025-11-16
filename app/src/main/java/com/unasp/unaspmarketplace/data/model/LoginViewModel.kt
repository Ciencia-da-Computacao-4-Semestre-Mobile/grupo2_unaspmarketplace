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
                // AQUI ESTÁ A CORREÇÃO:
                // "Use o valor de 'error'. Se por acaso 'error' for nulo,
                // use a string 'Ocorreu um erro desconhecido.' no lugar."
                // Isso GARANTE que nunca passaremos um nulo para o postValue.
                errorMessage.postValue(error ?: "Ocorreu um erro desconhecido.")
            }
        }
    }
}
