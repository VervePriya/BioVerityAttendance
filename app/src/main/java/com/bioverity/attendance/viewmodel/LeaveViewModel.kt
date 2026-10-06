
package com.bioverity.attendance.viewmodel

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.bioverity.attendance.data.api.ApiClient
import com.bioverity.attendance.data.model.LeaveApplication
import com.bioverity.attendance.data.model.LeaveApplicationRequest

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody

import java.io.File

class LeaveViewModel : ViewModel() {

    // ---------------------------------------------------------
    // EMPLOYEE LEAVE LIST
    // ---------------------------------------------------------

    private val _leaveApplications =
        MutableStateFlow<List<LeaveApplication>>(emptyList())

    val leaveApplications: StateFlow<List<LeaveApplication>> =
        _leaveApplications.asStateFlow()


    // ---------------------------------------------------------
    // ADMIN / MANAGER LEAVE LIST
    // ---------------------------------------------------------

    private val _allLeaveApplications =
        MutableStateFlow<List<LeaveApplication>>(emptyList())

    val allLeaveApplications: StateFlow<List<LeaveApplication>> =
        _allLeaveApplications.asStateFlow()


    // ---------------------------------------------------------
    // LOADING
    // ---------------------------------------------------------

    private val _isLoading =
        MutableStateFlow(false)

    val isLoading: StateFlow<Boolean> =
        _isLoading.asStateFlow()


    private val _isSubmitting =
        MutableStateFlow(false)

    val isSubmitting: StateFlow<Boolean> =
        _isSubmitting.asStateFlow()


    private val _isProcessingApproval =
        MutableStateFlow(false)

    val isProcessingApproval: StateFlow<Boolean> =
        _isProcessingApproval.asStateFlow()


    // ---------------------------------------------------------
    // MESSAGES
    // ---------------------------------------------------------

    private val _errorMessage =
        MutableStateFlow<String?>(null)

    val errorMessage: StateFlow<String?> =
        _errorMessage.asStateFlow()


    private val _successMessage =
        MutableStateFlow<String?>(null)

    val successMessage: StateFlow<String?> =
        _successMessage.asStateFlow()


    private var personId: String? = null


    // ---------------------------------------------------------
    // EMPLOYEE
    // ---------------------------------------------------------

    fun setPersonId(personId: String) {
        this.personId = personId
    }


    fun loadLeaveApplications() {

        val currentPersonId = personId

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


    // ---------------------------------------------------------
    // ADMIN / MANAGER
    // LOAD ALL LEAVE APPLICATIONS
    // ---------------------------------------------------------

    fun loadAllLeaveApplications() {

        viewModelScope.launch {

            _isLoading.value = true
            _errorMessage.value = null

            try {

                val response =
                    ApiClient.api.getAllLeaveApplications()

                if (response.success) {

                    _allLeaveApplications.value =
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


    // ---------------------------------------------------------
    // APPROVE LEAVE
    // ---------------------------------------------------------

    fun approveLeave(leaveId: String) {

        if (_isProcessingApproval.value) {
            return
        }

        viewModelScope.launch {

            _isProcessingApproval.value = true
            _errorMessage.value = null
            _successMessage.value = null

            try {

                val response =
                    ApiClient.api.approveLeave(leaveId)

                if (response.success) {

                    response.leave?.let { updatedLeave ->

                        _allLeaveApplications.value =
                            _allLeaveApplications.value.map { leave ->

                                if (leave.id == updatedLeave.id) {
                                    updatedLeave
                                } else {
                                    leave
                                }
                            }
                    }

                    _successMessage.value =
                        response.message
                            ?: "Leave application approved."

                } else {

                    _errorMessage.value =
                        response.message
                            ?: "Failed to approve leave."
                }

            } catch (e: Exception) {

                _errorMessage.value =
                    e.message
                        ?: "Unable to approve leave."

            } finally {

                _isProcessingApproval.value = false
            }
        }
    }


    // ---------------------------------------------------------
    // REJECT LEAVE
    // ---------------------------------------------------------

    fun rejectLeave(leaveId: String) {

        if (_isProcessingApproval.value) {
            return
        }

        viewModelScope.launch {

            _isProcessingApproval.value = true
            _errorMessage.value = null
            _successMessage.value = null

            try {

                val response =
                    ApiClient.api.rejectLeave(leaveId)

                if (response.success) {

                    response.leave?.let { updatedLeave ->

                        _allLeaveApplications.value =
                            _allLeaveApplications.value.map { leave ->

                                if (leave.id == updatedLeave.id) {
                                    updatedLeave
                                } else {
                                    leave
                                }
                            }
                    }

                    _successMessage.value =
                        response.message
                            ?: "Leave application rejected."

                } else {

                    _errorMessage.value =
                        response.message
                            ?: "Failed to reject leave."
                }

            } catch (e: Exception) {

                _errorMessage.value =
                    e.message
                        ?: "Unable to reject leave."

            } finally {

                _isProcessingApproval.value = false
            }
        }
    }


    // ---------------------------------------------------------
    // SUBMIT LEAVE
    // ---------------------------------------------------------

    fun submitLeave(
        context: Context,
        leaveDate: String,
        reason: String,
        fileUri: Uri?
    ) {

        val currentPersonId = personId

        if (currentPersonId.isNullOrBlank()) {
            _errorMessage.value =
                "Employee information is not available."
            return
        }

        val cleanReason = reason.trim()

        if (cleanReason.isEmpty()) {
            _errorMessage.value =
                "Please enter a reason for leave."
            return
        }

        _errorMessage.value = null
        _successMessage.value = null

        viewModelScope.launch {

            _isSubmitting.value = true

            try {

                var attachmentUrl: String? = null

                if (fileUri != null) {

                    val contentResolver =
                        context.contentResolver

                    val fileName =
                        getFileName(
                            context,
                            fileUri
                        ) ?: "attachment"

                    val mimeType =
                        contentResolver.getType(fileUri)
                            ?: "application/octet-stream"

                    val tempFile =
                        File(
                            context.cacheDir,
                            fileName
                        )

                    contentResolver
                        .openInputStream(fileUri)
                        ?.use { input ->

                            tempFile
                                .outputStream()
                                .use { output ->

                                    input.copyTo(output)
                                }

                        }
                        ?: throw Exception(
                            "Unable to read selected attachment."
                        )

                    val requestBody =
                        tempFile.asRequestBody(
                            mimeType.toMediaTypeOrNull()
                        )

                    val multipartFile =
                        MultipartBody.Part.createFormData(
                            "file",
                            fileName,
                            requestBody
                        )

                    val personIdBody =
                        currentPersonId.toRequestBody(
                            "text/plain".toMediaTypeOrNull()
                        )

                    val uploadResponse =
                        ApiClient.api.uploadLeaveAttachment(
                            file = multipartFile,
                            personId = personIdBody
                        )

                    if (!uploadResponse.success) {

                        throw Exception(
                            uploadResponse.message
                                ?: "Failed to upload attachment."
                        )
                    }

                    attachmentUrl =
                        uploadResponse.attachmentUrl

                    if (attachmentUrl.isNullOrBlank()) {

                        throw Exception(
                            "Attachment uploaded but URL was not returned."
                        )
                    }

                    tempFile.delete()
                }

                val request =
                    LeaveApplicationRequest(
                        personId = currentPersonId,
                        leaveDate = leaveDate,
                        reason = cleanReason,
                        attachmentUrl = attachmentUrl
                    )

                val response =
                    ApiClient.api.submitLeave(request)

                if (response.success) {

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


    // ---------------------------------------------------------
    // FILE NAME
    // ---------------------------------------------------------

    private fun getFileName(
        context: Context,
        uri: Uri
    ): String? {

        var fileName: String? = null

        context.contentResolver
            .query(
                uri,
                null,
                null,
                null,
                null
            )
            ?.use { cursor ->

                val nameIndex =
                    cursor.getColumnIndex(
                        OpenableColumns.DISPLAY_NAME
                    )

                if (
                    nameIndex >= 0 &&
                    cursor.moveToFirst()
                ) {

                    fileName =
                        cursor.getString(nameIndex)
                }
            }

        return fileName
    }


    // ---------------------------------------------------------
    // CLEAR MESSAGES
    // ---------------------------------------------------------

    fun clearError() {
        _errorMessage.value = null
    }

    fun clearSuccessMessage() {
        _successMessage.value = null
    }


    // ---------------------------------------------------------
    // REFRESH EMPLOYEE LEAVES
    // ---------------------------------------------------------

    fun refresh() {
        loadLeaveApplications()
    }


    // ---------------------------------------------------------
    // REFRESH ADMIN / MANAGER LEAVES
    // ---------------------------------------------------------

    fun refreshAllLeaves() {
        loadAllLeaveApplications()
    }
}

