
package com.bioverity.attendance.data.model

import com.google.gson.annotations.SerializedName

data class LeaveApplicationRequest(

    @SerializedName("person_id")
    val personId: String,

    @SerializedName("leave_date")
    val leaveDate: String,

    @SerializedName("reason")
    val reason: String,

    @SerializedName("attachment_url")
    val attachmentUrl: String?
)

