package com.bioverity.attendance.data.model

data class Employee(
    val id: String,
    val name: String,
    val employeeId: String,
    val department: String,
    val profileImageUrl: String? = null
)