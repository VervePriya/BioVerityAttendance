package com.bioverity.attendance.data.model

data class NotificationDto(
    val id: String,
    val person_id: String,
    val title: String,
    val message: String,
    val type: String,
    val is_read: Boolean,
    val created_at: String
)

data class NotificationsResponse(
    val success: Boolean,
    val notifications: List<NotificationDto> = emptyList()
)

data class UnreadNotificationResponse(
    val success: Boolean,
    val unread_count: Int = 0
)

data class NotificationActionResponse(
    val success: Boolean,
    val message: String? = null
)