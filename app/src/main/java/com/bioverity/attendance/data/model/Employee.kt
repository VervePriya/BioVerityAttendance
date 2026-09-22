package com.bioverity.attendance.data.model

data class Employee(
    val id: String,
    val name: String,
    val employeeId: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val position: String? = null,
    val department: String? = null,
    val imageUrl: String? = null
)