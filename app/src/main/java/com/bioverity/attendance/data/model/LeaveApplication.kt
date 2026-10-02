
package com.bioverity.attendance.data.model

import com.google.gson.annotations.SerializedName

data class LeaveApplication(

    @SerializedName("id")
    val id: String,

    @SerializedName("person_id")
    val personId: String,

    @SerializedName("leave_date")
    val leaveDate: String,

    @SerializedName("reason")
    val reason: String,

    @SerializedName("attachment_url")
    val attachmentUrl: String?,

    @SerializedName("status")
    val status: String,

    @SerializedName("created_at")
    val createdAt: String?,

    @SerializedName("updated_at")
    val updatedAt: String?
)

