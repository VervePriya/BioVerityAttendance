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

    companion object {
        private const val TAG = "NotificationViewModel"
        private const val SRI_LANKA_TIME_ZONE = "Asia/Colombo"
    }

    private val sriLankaZone =
        ZoneId.of(SRI_LANKA_TIME_ZONE)

    private val notificationTimeFormatter =
        DateTimeFormatter.ofPattern(
            "dd MMM yyyy, hh:mm a",
            Locale.ENGLISH
        )


    // ----------------------------------------------------
    // NOTIFICATIONS
    // ----------------------------------------------------

    private val _notifications =
        MutableStateFlow<List<AppNotification>>(emptyList())

    val notifications: StateFlow<List<AppNotification>> =
        _notifications.asStateFlow()


    // ----------------------------------------------------
    // UNREAD COUNT
    // ----------------------------------------------------

    private val _unreadCount =
        MutableStateFlow(0)

    val unreadCount: StateFlow<Int> =
        _unreadCount.asStateFlow()


    // ----------------------------------------------------
    // LOADING
    // ----------------------------------------------------

    private val _isLoading =
        MutableStateFlow(false)

    val isLoading: StateFlow<Boolean> =
        _isLoading.asStateFlow()


    // ----------------------------------------------------
    // ERROR
    // ----------------------------------------------------

    private val _error =
        MutableStateFlow<String?>(null)

    val error: StateFlow<String?> =
        _error.asStateFlow()


    // ----------------------------------------------------
    // LOAD NOTIFICATIONS
    // ----------------------------------------------------

    fun loadNotifications(
        personId: String
    ) {

        if (personId.isBlank()) {

            _error.value =
                "Employee ID is missing"

            return
        }

        viewModelScope.launch {

            _isLoading.value = true
            _error.value = null

            try {

                val response =
                    ApiClient.api.getNotifications(
                        personId
                    )

                if (response.success) {

                    val mappedNotifications =
                        response.notifications.map {
                            it.toAppNotification()
                        }

                    _notifications.value =
                        mappedNotifications

                    updateUnreadCount()

                } else {

                    _error.value =
                        "Unable to load notifications"
                }

            } catch (e: Exception) {

                _error.value =
                    e.message
                        ?: "Unable to load notifications"

            } finally {

                _isLoading.value = false
            }
        }
    }


    // ----------------------------------------------------
    // LOAD UNREAD COUNT
    // ----------------------------------------------------

    fun loadUnreadCount(
        personId: String
    ) {

        if (personId.isBlank()) {
            return
        }

        viewModelScope.launch {

            try {

                val response =
                    ApiClient.api
                        .getUnreadNotificationCount(
                            personId
                        )

                if (response.success) {

                    _unreadCount.value =
                        response.unread_count
                }

            } catch (e: Exception) {

                // Badge failure should not block the UI.
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

                            if (
                                it.id == notificationId
                            ) {

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
                    e.message
                        ?: "Unable to update notification"
            }
        }
    }


    // ----------------------------------------------------
    // MARK ALL AS READ
    // ----------------------------------------------------

    fun markAllAsRead(
        personId: String
    ) {

        if (personId.isBlank()) {
            return
        }

        viewModelScope.launch {

            try {

                val response =
                    ApiClient.api
                        .markAllNotificationsRead(
                            personId
                        )

                if (response.success) {

                    _notifications.value =
                        _notifications.value.map {
                            it.copy(
                                isRead = true
                            )
                        }

                    _unreadCount.value = 0
                }

            } catch (e: Exception) {

                _error.value =
                    e.message
                        ?: "Unable to update notifications"
            }
        }
    }


    // ----------------------------------------------------
    // DELETE NOTIFICATION
    // ----------------------------------------------------

    fun deleteNotification(
        notificationId: String
    ) {

        viewModelScope.launch {

            try {

                val response =
                    ApiClient.api
                        .deleteNotification(
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
                    e.message
                        ?: "Unable to delete notification"
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

        val notificationType =
            when (
                type.trim().uppercase()
            ) {

                "ATTENDANCE_SUCCESS" ->
                    NotificationType.ATTENDANCE_SUCCESS

                "CHECK_IN_REMINDER" ->
                    NotificationType.CHECK_IN_REMINDER

                "CHECK_OUT_REMINDER" ->
                    NotificationType.CHECK_OUT_REMINDER

                "LOCATION_WARNING" ->
                    NotificationType.LOCATION_VERIFICATION_FAILED

                "LOCATION_VERIFICATION_FAILED" ->
                    NotificationType.LOCATION_VERIFICATION_FAILED

                "SYSTEM" ->
                    NotificationType.SYSTEM_NOTIFICATION

                "SYSTEM_NOTIFICATION" ->
                    NotificationType.SYSTEM_NOTIFICATION

                else ->
                    NotificationType.SYSTEM_NOTIFICATION
            }


        return AppNotification(

            id = id,

            title = title,

            message = message,

            type = notificationType,

            time =
                formatNotificationTime(
                    created_at
                ),

            isRead = is_read
        )
    }


    // ----------------------------------------------------
    // FORMAT TIME
    //
    // Backend notification timestamps may arrive as:
    //
    // 2026-09-29T08:15:25Z
    //
    // or:
    //
    // 2026-09-29 13:45:25
    //
    // The application displays Asia/Colombo time.
    // ----------------------------------------------------

    private fun formatNotificationTime(
        timestamp: String
    ): String {

        if (timestamp.isBlank()) {
            return ""
        }

        return try {

            var normalized =
                timestamp.trim()

            if (
                normalized.contains(" ") &&
                !normalized.contains("T")
            ) {

                normalized =
                    normalized.replaceFirst(
                        " ",
                        "T"
                    )
            }

            if (
                normalized.endsWith("+00")
            ) {

                normalized =
                    normalized.dropLast(3) +
                            "+00:00"
            }


            val instant =
                Instant.parse(
                    normalized
                )


            instant
                .atZone(
                    sriLankaZone
                )
                .format(
                    notificationTimeFormatter
                )

        } catch (e: Exception) {

            /*
             * If the backend already sends a local
             * Sri Lanka timestamp without timezone,
             * parse it as Asia/Colombo.
             */

            try {

                var localValue =
                    timestamp.trim()

                if (
                    localValue.contains(" ") &&
                    !localValue.contains("T")
                ) {

                    localValue =
                        localValue.replaceFirst(
                            " ",
                            "T"
                        )
                }


                val localDateTime =
                    java.time.LocalDateTime.parse(
                        localValue
                    )


                localDateTime.format(
                    notificationTimeFormatter
                )

            } catch (
                localException: Exception
            ) {

                timestamp
            }
        }
    }
}