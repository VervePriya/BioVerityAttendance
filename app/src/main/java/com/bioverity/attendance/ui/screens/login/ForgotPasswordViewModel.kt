package com.bioverity.attendance.ui.screens.login

import android.app.Application
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bioverity.attendance.data.api.ApiClient
import com.bioverity.attendance.data.model.ForgotPasswordRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ForgotPasswordUiState(
    val email: String = "",
    val isLoading: Boolean = false,
    val emailSent: Boolean = false,
    val errorMessage: String? = null
)

class ForgotPasswordViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val _uiState =
        MutableStateFlow(ForgotPasswordUiState())

    val uiState: StateFlow<ForgotPasswordUiState> =
        _uiState.asStateFlow()


    fun updateEmail(value: String) {

        _uiState.value =
            _uiState.value.copy(
                email = value,
                errorMessage = null,
                emailSent = false
            )
    }


    fun sendResetEmail() {

        val currentState = _uiState.value

        val email =
            currentState.email.trim().lowercase()

        if (email.isBlank()) {

            _uiState.value =
                currentState.copy(
                    errorMessage =
                        "Please enter your email address.",
                    emailSent = false
                )

            return
        }


        if (
            !Patterns.EMAIL_ADDRESS
                .matcher(email)
                .matches()
        ) {

            _uiState.value =
                currentState.copy(
                    errorMessage =
                        "Please enter a valid email address.",
                    emailSent = false
                )

            return
        }


        viewModelScope.launch {

            _uiState.value =
                _uiState.value.copy(
                    isLoading = true,
                    errorMessage = null,
                    emailSent = false
                )

            try {

                val response =
                    ApiClient.api.forgotPassword(
                        ForgotPasswordRequest(
                            email = email
                        )
                    )

                if (response.success) {

                    _uiState.value =
                        _uiState.value.copy(
                            isLoading = false,
                            emailSent = true,
                            errorMessage = null
                        )

                } else {

                    _uiState.value =
                        _uiState.value.copy(
                            isLoading = false,
                            emailSent = false,
                            errorMessage =
                                response.message
                                    ?: "Unable to send reset email."
                        )
                }

            } catch (e: Exception) {

                _uiState.value =
                    _uiState.value.copy(
                        isLoading = false,
                        emailSent = false,
                        errorMessage =
                            "Connection error: ${e.message}"
                    )
            }
        }
    }


    fun clearMessage() {

        _uiState.value =
            _uiState.value.copy(
                errorMessage = null,
                emailSent = false
            )
    }
}