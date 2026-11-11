package com.bioverity.attendance.data.model

data class GeminiResponse(
    val success: Boolean,
    val question: String? = null,
    val answer: String? = null
)