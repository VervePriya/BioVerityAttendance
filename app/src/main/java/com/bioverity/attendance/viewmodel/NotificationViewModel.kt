package com.bioverity.attendance.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.bioverity.attendance.data.api.ApiClient
import com.bioverity.attendance.data.model.NotificationDto
import com.bioverity.attendance.ui.screens.AppNotification
import com.bioverity.attendance.ui.screens.NotificationType

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NotificationViewModel : ViewModel() {

    private val _notifications =
        MutableStateFlow<List<AppNotification>>(emptyList())

    val notifications: StateFlow<List<AppNotification>> =
        _notifications.asStateFlow()


    private val _unreadCount =
        MutableStateFlow(0)

    val unreadCount: StateFlow<Int> =
        _unreadCount.asStateFlow()


    private val _isLoading =
        MutableStateFlow(false)

    val isLoading: StateFlow<Boolean> =
        _isLoading.asStateFlow()


    private val _error =
        MutableStateFlow<String?>(null)

    val error: StateFlow<String?> =
        _error.asStateFlow()


    // ----------------------------------------------------
    // LOAD NOTIFICATIONS
    // ----------------------------------------------------

    fun loadNotifications(personId: String) {

        if (personId.isBlank()) {
            _error.value = "Employee ID is missing"
            return
        }

        viewModelScope.launch {

            _isLoading.value = true
            _error.value = null

            try {

                val response =
                    ApiClient.api.getNotifications(personId)

                if (response.success) {

                    _notifications.value =
                        response.notifications.map {
                            it.toAppNotification()
                        }

                    updateUnreadCount()

                } else {

                    _error.value =
                        "Unable to load notifications"
                }

            } catch (e: Exception) {

                _error.value =
                    e.message ?: "Unable to load notifications"

            } finally {

                _isLoading.value = false
            }
        }
    }


    // ----------------------------------------------------
    // UNREAD COUNT
    // ----------------------------------------------------

    fun loadUnreadCount(personId: String) {

        if (personId.isBlank()) {
            return
        }

        viewModelScope.launch {

            try {

                val response =
                    ApiClient.api.getUnreadNotificationCount(
                        personId
                    )

                if (response.success) {

                    _unreadCount.value =
                        response.unread_count
                }

            } catch (e: Exception) {

                // Do not block the UI if badge loading fails.
            }
        }
    }


    // ----------------------------------------------------
    // MARK AS READ
    // ----------------------------------------------------

    fun markAsRead(
        notificationId: String,
        personId: String
    ) {

        viewModelScope.launch {

            try {

                val response =
                    ApiClient.api.markNotificationRead(
                        notificationId
                    )

                if (response.success) {

                    _notifications.value =
                        _notifications.value.map {

                            if (it.id == notificationId) {

                                it.copy(
                                    isRead = true
                                )

                            } else {

                                it
                            }
                        }

                    updateUnreadCount()
                }

            } catch (e: Exception) {

                _error.value =
                    e.message ?: "Unable to update notification"
            }
        }
    }


    // ----------------------------------------------------
    // MARK ALL READ
    // ----------------------------------------------------

    fun markAllAsRead(
        personId: String
    ) {

        viewModelScope.launch {

            try {

                val response =
                    ApiClient.api.markAllNotificationsRead(
                        personId
                    )

                if (response.success) {

                    _notifications.value =
                        _notifications.value.map {
                            it.copy(isRead = true)
                        }

                    _unreadCount.value = 0
                }

            } catch (e: Exception) {

                _error.value =
                    e.message ?: "Unable to update notifications"
            }
        }
    }


    // ----------------------------------------------------
    // DELETE
    // ----------------------------------------------------

    fun deleteNotification(
        notificationId: String
    ) {

        viewModelScope.launch {

            try {

                val response =
                    ApiClient.api.deleteNotification(
                        notificationId
                    )

                if (response.success) {

                    _notifications.value =
                        _notifications.value.filter {
                            it.id != notificationId
                        }

                    updateUnreadCount()
                }

            } catch (e: Exception) {

                _error.value =
                    e.message ?: "Unable to delete notification"
            }
        }
    }


    // ----------------------------------------------------
    // LOCAL UNREAD COUNT
    // ----------------------------------------------------

    private fun updateUnreadCount() {

        _unreadCount.value =
            _notifications.value.count {
                !it.isRead
            }
    }


    // ----------------------------------------------------
    // DTO → UI MODEL
    // ----------------------------------------------------

    private fun NotificationDto.toAppNotification():
            AppNotification {

        return AppNotification(
            id = id,
            title = title,
            message = message,
            type = when (type.uppercase()) {

                "ATTENDANCE_SUCCESS" ->
                    NotificationType.ATTENDANCE_SUCCESS

                "CHECK_IN_REMINDER" ->
                    NotificationType.CHECK_IN_REMINDER

                "CHECK_OUT_REMINDER" ->
                    NotificationType.CHECK_OUT_REMINDER

                "LOCATION_WARNING" ->
                    NotificationType.LOCATION_WARNING

                else ->
                    NotificationType.SYSTEM
            },
            time = formatNotificationTime(
                created_at
            ),
            isRead = is_read
        )
    }


    // ----------------------------------------------------
    // TIME FORMAT
    // ----------------------------------------------------

    private fun formatNotificationTime(
        timestamp: String
    ): String {

        return try {

            val instant =
                java.time.Instant.parse(timestamp)

            val zoned =
                instant.atZone(
                    java.time.ZoneId.of(
                        "Asia/Colombo"
                    )
                )

            val formatter =
                java.time.format.DateTimeFormatter.ofPattern(
                    "dd MMM yyyy, hh:mm a"
                )

            zoned.format(formatter)

        } catch (e: Exception) {

            timestamp
        }
    }
}
private fun formatNotificationTime(timestamp: String): String {
    return try {
        val instant = Instant.parse(timestamp)

        val formatter = DateTimeFormatter.ofPattern(
            "dd MMM yyyy, hh:mm a",
            Locale.ENGLISH
        )

        instant
            .atZone(ZoneId.of("Asia/Colombo"))
            .format(formatter)

    } catch (e: Exception) {
        timestamp
    }
}