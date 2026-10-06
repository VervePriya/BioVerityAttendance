
package com.bioverity.attendance.ui.screens.login

import android.app.Application
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bioverity.attendance.data.api.ApiClient
import com.bioverity.attendance.data.model.LoginRequest
import com.bioverity.attendance.data.session.AppSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val loginSuccess: Boolean = false,
    val errorMessage: String? = null
)

class LoginViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(LoginUiState())

    val uiState: StateFlow<LoginUiState> =
        _uiState.asStateFlow()

    fun updateEmail(value: String) {
        _uiState.value = _uiState.value.copy(
            email = value,
            errorMessage = null,
            loginSuccess = false
        )
    }

    fun updatePassword(value: String) {
        _uiState.value = _uiState.value.copy(
            password = value,
            errorMessage = null,
            loginSuccess = false
        )
    }

    fun login() {

        val currentState = _uiState.value

        val email = currentState.email.trim()
        val password = currentState.password

        if (email.isBlank()) {
            _uiState.value = currentState.copy(
                isLoading = false,
                loginSuccess = false,
                errorMessage = "Please enter your email."
            )
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.value = currentState.copy(
                isLoading = false,
                loginSuccess = false,
                errorMessage = "Please enter a valid email address."
            )
            return
        }

        if (password.isBlank()) {
            _uiState.value = currentState.copy(
                isLoading = false,
                loginSuccess = false,
                errorMessage = "Please enter your password."
            )
            return
        }

        viewModelScope.launch {

            _uiState.value = _uiState.value.copy(
                isLoading = true,
                loginSuccess = false,
                errorMessage = null
            )

            try {

                val response = ApiClient.api.login(
                    LoginRequest(
                        email = email,
                        password = password
                    )
                )
                android.util.Log.d(
                    "LOGIN_RESPONSE",
                    "name=${response.name}, imageUrl=${response.imageUrl}"
                )

                if (
                    response.success &&
                    !response.personId.isNullOrBlank()
                ) {

                    val context = getApplication<Application>()

                    AppSession.saveEmployee(
                        context = context,
                        personId = response.personId,
                        employeeId = response.employeeId.orEmpty(),
                        name = response.name.orEmpty(),
                        email = response.email ?: email,
                        imageUrl = response.imageUrl,
                        role=response.role
                    )

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        loginSuccess = true,
                        errorMessage = null
                    )

                    return@launch
                }

                val message = when (response.reason) {

                    "invalid_credentials" ->
                        "Invalid email or password."

                    "employee_not_found" ->
                        "Employee profile was not found."

                    "employee_lookup_error" ->
                        "Unable to load employee profile."

                    "session_token_missing" ->
                        "Login session could not be created."

                    "email_required" ->
                        "Please enter your email."

                    "password_required" ->
                        "Please enter your password."

                    else ->
                        response.error ?: "Login failed."
                }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    loginSuccess = false,
                    errorMessage = message
                )

            } catch (e: Exception) {

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    loginSuccess = false,
                    errorMessage =
                        "Connection error: ${e.javaClass.simpleName}: ${e.message}"
                )
            }
        }
    }

    fun clearLoginSuccess() {
        _uiState.value = _uiState.value.copy(
            loginSuccess = false
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null
        )
    }
}

