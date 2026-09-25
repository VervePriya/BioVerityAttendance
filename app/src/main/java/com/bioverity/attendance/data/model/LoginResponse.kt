
package com.bioverity.attendance.data.model

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("reason")
    val reason: String? = null,

    @SerializedName("person_id")
    val personId: String? = null,

    @SerializedName("employee_id")
    val employeeId: String? = null,

    @SerializedName("name")
    val name: String? = null,

    @SerializedName("email")
    val email: String? = null,

    @SerializedName("email_used")
    val emailUsed: String? = null,
    @SerializedName("image_url")
    val imageUrl: String? = null,

    @SerializedName("error")
    val error: String? = null


)

