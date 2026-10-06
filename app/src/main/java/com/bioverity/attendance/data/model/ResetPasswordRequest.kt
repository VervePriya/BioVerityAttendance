
package com.bioverity.attendance.data.model

data class ResetPasswordRequest(
    val access_token: String,
    val new_password: String
)

