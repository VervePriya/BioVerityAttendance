
package com.bioverity.attendance.ui.screens.login

import android.app.Application

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope

import com.bioverity.attendance.data.api.ApiClient
import com.bioverity.attendance.data.model.ResetPasswordRequest

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


data class ResetPasswordUiState(
    val newPassword: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val success: Boolean = false,
    val errorMessage: String? = null
)


class ResetPasswordViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val _uiState =
        MutableStateFlow(
            ResetPasswordUiState()
        )

    val uiState: StateFlow<ResetPasswordUiState> =
        _uiState.asStateFlow()


    fun updateNewPassword(
        value: String
    ) {

        _uiState.value =
            _uiState.value.copy(
                newPassword = value,
                errorMessage = null
            )
    }


    fun updateConfirmPassword(
        value: String
    ) {

        _uiState.value =
            _uiState.value.copy(
                confirmPassword = value,
                errorMessage = null
            )
    }


    fun resetPassword(
        accessToken: String
    ) {

        val state =
            _uiState.value

        val password =
            state.newPassword

        val confirmPassword =
            state.confirmPassword


        // -----------------------------------------------------
        // PASSWORD VALIDATION
        // -----------------------------------------------------

        if (password.isBlank()) {

            _uiState.value =
                state.copy(
                    errorMessage =
                        "Please enter a new password."
                )

            return
        }


        if (password.length < 8) {

            _uiState.value =
                state.copy(
                    errorMessage =
                        "Password must be at least 8 characters."
                )

            return
        }


        if (confirmPassword.isBlank()) {

            _uiState.value =
                state.copy(
                    errorMessage =
                        "Please confirm your password."
                )

            return
        }


        if (password != confirmPassword) {

            _uiState.value =
                state.copy(
                    errorMessage =
                        "Passwords do not match."
                )

            return
        }


        if (accessToken.isBlank()) {

            _uiState.value =
                state.copy(
                    errorMessage =
                        "Invalid or expired password reset link."
                )

            return
        }


        // -----------------------------------------------------
        // UPDATE PASSWORD
        // -----------------------------------------------------

        viewModelScope.launch {

            _uiState.value =
                state.copy(
                    isLoading = true,
                    errorMessage = null
                )

            try {

                val response =
                    ApiClient.api.resetPassword(

                        ResetPasswordRequest(
                            access_token =
                                accessToken,

                            new_password =
                                password
                        )
                    )


                if (response.success) {

                    _uiState.value =
                        _uiState.value.copy(
                            isLoading = false,
                            success = true,
                            errorMessage = null
                        )

                } else {

                    _uiState.value =
                        _uiState.value.copy(
                            isLoading = false,
                            success = false,
                            errorMessage =
                                response.message
                        )
                }

            } catch (e: Exception) {

                _uiState.value =
                    _uiState.value.copy(
                        isLoading = false,
                        success = false,
                        errorMessage =
                            "Connection error: ${e.message}"
                    )
            }
        }
    }
}

