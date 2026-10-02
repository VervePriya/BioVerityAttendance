
package com.bioverity.attendance.data.model

import com.google.gson.annotations.SerializedName

data class LeaveResponse(

    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String?,

    @SerializedName("leave")
    val leave: LeaveApplication?
)

