package com.bioverity.attendance.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Employee(
    val id: String,
    val name: String,

    @SerialName("employee_id")
    val employeeId: String? = null,

    val email: String? = null,
    val phone: String? = null,
    val position: String? = null,
    val department: String? = null,

    @SerialName("image_url")
    val imageUrl: String? = null
)