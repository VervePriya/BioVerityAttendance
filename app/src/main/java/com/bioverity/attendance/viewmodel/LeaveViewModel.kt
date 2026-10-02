
package com.bioverity.attendance.viewmodel

import android.net.Uri

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.bioverity.attendance.data.api.ApiClient
import com.bioverity.attendance.data.model.LeaveApplication
import com.bioverity.attendance.data.model.LeaveApplicationRequest

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class LeaveViewModel : ViewModel() {

    // ====================================================
    // LEAVE APPLICATIONS
    // ====================================================

    private val _leaveApplications =
        MutableStateFlow<List<LeaveApplication>>(emptyList())

    val leaveApplications: StateFlow<List<LeaveApplication>> =
        _leaveApplications.asStateFlow()


    // ====================================================
    // LOADING STATE
    // ====================================================

    private val _isLoading =
        MutableStateFlow(false)

    val isLoading: StateFlow<Boolean> =
        _isLoading.asStateFlow()


    // ====================================================
    // SUBMITTING STATE
    // ====================================================

    private val _isSubmitting =
        MutableStateFlow(false)

    val isSubmitting: StateFlow<Boolean> =
        _isSubmitting.asStateFlow()


    // ====================================================
    // ERROR MESSAGE
    // ====================================================

    private val _errorMessage =
        MutableStateFlow<String?>(null)

    val errorMessage: StateFlow<String?> =
        _errorMessage.asStateFlow()


    // ====================================================
    // SUCCESS MESSAGE
    // ====================================================

    private val _successMessage =
        MutableStateFlow<String?>(null)

    val successMessage: StateFlow<String?> =
        _successMessage.asStateFlow()


    // ====================================================
    // LOGGED-IN EMPLOYEE
    // ====================================================

    private var personId: String? = null


    // ====================================================
    // SET PERSON ID
    // ====================================================

    fun setPersonId(
        personId: String
    ) {

        this.personId = personId
    }


    // ====================================================
    // LOAD LEAVE APPLICATIONS
    // ====================================================

    fun loadLeaveApplications() {

        val currentPersonId =
            personId

        if (currentPersonId.isNullOrBlank()) {

            _errorMessage.value =
                "Employee information is not available."

            return
        }


        viewModelScope.launch {

            _isLoading.value = true

            _errorMessage.value = null


            try {

                val response =
                    ApiClient.api.getLeaveApplications(
                        currentPersonId
                    )


                if (response.success) {

                    _leaveApplications.value =
                        response.leaves

                } else {

                    _errorMessage.value =
                        "Failed to load leave applications."
                }

            } catch (e: Exception) {

                _errorMessage.value =
                    e.message
                        ?: "Unable to load leave applications."

            } finally {

                _isLoading.value = false
            }
        }
    }


    // ====================================================
    // SUBMIT LEAVE APPLICATION
    // ====================================================

    fun submitLeave(
        leaveDate: String,
        reason: String,
        fileUri: Uri?
    ) {

        val currentPersonId =
            personId


        // ------------------------------------------------
        // CHECK EMPLOYEE
        // ------------------------------------------------

        if (currentPersonId.isNullOrBlank()) {

            _errorMessage.value =
                "Employee information is not available."

            return
        }


        // ------------------------------------------------
        // CHECK REASON
        // ------------------------------------------------

        val cleanReason =
            reason.trim()

        if (cleanReason.isEmpty()) {

            _errorMessage.value =
                "Please enter a reason for leave."

            return
        }


        // ------------------------------------------------
        // CLEAR PREVIOUS MESSAGES
        // ------------------------------------------------

        _errorMessage.value = null

        _successMessage.value = null


        // ------------------------------------------------
        // SUBMIT
        // ------------------------------------------------

        viewModelScope.launch {

            _isSubmitting.value = true


            try {

                /*
                 * Attachment upload will be connected
                 * in the next step.
                 *
                 * For now the selected file URI is
                 * intentionally not sent to FastAPI.
                 */

                val attachmentUrl: String? = null


                val request =
                    LeaveApplicationRequest(

                        personId =
                            currentPersonId,

                        leaveDate =
                            leaveDate,

                        reason =
                            cleanReason,

                        attachmentUrl =
                            attachmentUrl
                    )


                val response =
                    ApiClient.api.submitLeave(
                        request
                    )


                if (response.success) {

                    // ------------------------------------
                    // ADD NEW APPLICATION TO TOP OF LIST
                    // ------------------------------------

                    response.leave?.let { newLeave ->

                        _leaveApplications.value =
                            listOf(newLeave) +
                                    _leaveApplications.value
                    }


                    _successMessage.value =
                        response.message
                            ?: "Leave application submitted successfully."


                } else {

                    _errorMessage.value =
                        response.message
                            ?: "Failed to submit leave application."
                }

            } catch (e: Exception) {

                _errorMessage.value =
                    e.message
                        ?: "Unable to submit leave application."

            } finally {

                _isSubmitting.value = false
            }
        }
    }


    // ====================================================
    // CLEAR ERROR
    // ====================================================

    fun clearError() {

        _errorMessage.value = null
    }


    // ====================================================
    // CLEAR SUCCESS MESSAGE
    // ====================================================

    fun clearSuccessMessage() {

        _successMessage.value = null
    }


    // ====================================================
    // REFRESH
    // ====================================================

    fun refresh() {

        loadLeaveApplications()
    }
}

