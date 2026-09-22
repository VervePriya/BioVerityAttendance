package com.bioverity.attendance.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class AppSessionViewModel : ViewModel() {

    var personId by mutableStateOf<String?>(null)
        private set

    var employeeId by mutableStateOf<String?>(null)
        private set

    var employeeName by mutableStateOf<String?>(null)
        private set

    var employeeEmail by mutableStateOf<String?>(null)
        private set

    val isLoggedIn: Boolean
        get() = !personId.isNullOrBlank()

    fun login(
        personId: String,
        employeeId: String?,
        employeeName: String?,
        employeeEmail: String?
    ) {
        this.personId = personId
        this.employeeId = employeeId
        this.employeeName = employeeName
        this.employeeEmail = employeeEmail
    }

    fun logout() {
        personId = null
        employeeId = null
        employeeName = null
        employeeEmail = null
    }
}