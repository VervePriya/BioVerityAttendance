
package com.bioverity.attendance.data.model

import com.google.gson.annotations.SerializedName

data class LeaveListResponse(

    @SerializedName("success")
    val success: Boolean,

    @SerializedName("leaves")
    val leaves: List<LeaveApplication>
)

