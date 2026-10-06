package com.bioverity.attendance.data.model

import com.google.gson.annotations.SerializedName

data class LeaveAttachmentUploadResponse(

    @SerializedName("success")
    val success: Boolean,

    @SerializedName("attachment_url")
    val attachmentUrl: String?,

    @SerializedName("message")
    val message: String?
)