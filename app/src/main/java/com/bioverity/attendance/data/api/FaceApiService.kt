package com.bioverity.attendance.data.api

import com.bioverity.attendance.data.model.AttendanceResponse
import com.bioverity.attendance.data.model.LoginRequest
import com.bioverity.attendance.data.model.LoginResponse
import com.bioverity.attendance.data.model.NotificationActionResponse
import com.bioverity.attendance.data.model.NotificationsResponse
import com.bioverity.attendance.data.model.UnreadNotificationResponse

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface FaceApiService {

    @POST("login")
    suspend fun login(
        @Body request: LoginRequest
    ): LoginResponse

    @GET("attendance/{person_id}")
    suspend fun getAttendance(
        @Path("person_id") personId: String
    ): AttendanceResponse

    // ----------------------------------------------------
    // NOTIFICATIONS
    // ----------------------------------------------------

    @GET("notifications/{person_id}")
    suspend fun getNotifications(
        @Path("person_id") personId: String
    ): NotificationsResponse

    @GET("notifications/{person_id}/unread-count")
    suspend fun getUnreadNotificationCount(
        @Path("person_id") personId: String
    ): UnreadNotificationResponse

    @POST("notifications/{notification_id}/read")
    suspend fun markNotificationRead(
        @Path("notification_id") notificationId: String
    ): NotificationActionResponse

    @POST("notifications/{person_id}/read-all")
    suspend fun markAllNotificationsRead(
        @Path("person_id") personId: String
    ): NotificationActionResponse

    @DELETE("notifications/{notification_id}")
    suspend fun deleteNotification(
        @Path("notification_id") notificationId: String
    ): NotificationActionResponse
}