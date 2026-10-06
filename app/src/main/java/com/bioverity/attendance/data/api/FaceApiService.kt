
package com.bioverity.attendance.data.api

import com.bioverity.attendance.data.model.AttendanceResponse
import com.bioverity.attendance.data.model.LoginRequest
import com.bioverity.attendance.data.model.LoginResponse
import com.bioverity.attendance.data.model.NotificationActionResponse
import com.bioverity.attendance.data.model.NotificationsResponse
import com.bioverity.attendance.data.model.UnreadNotificationResponse
import com.bioverity.attendance.data.model.LeaveApplicationRequest
import com.bioverity.attendance.data.model.LeaveListResponse
import com.bioverity.attendance.data.model.LeaveResponse
import com.bioverity.attendance.data.model.LeaveAttachmentUploadResponse
import com.bioverity.attendance.data.model.ForgotPasswordRequest
import com.bioverity.attendance.data.model.ForgotPasswordResponse
import com.bioverity.attendance.data.model.ResetPasswordRequest
import com.bioverity.attendance.data.model.ResetPasswordResponse

import okhttp3.MultipartBody

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

interface FaceApiService {

    // ---------------------------------------------------------
    // LOGIN
    // ---------------------------------------------------------

    @POST("login")
    suspend fun login(
        @Body request: LoginRequest
    ): LoginResponse

    // ---------------------------------------------------------
// FORGOT PASSWORD
// ---------------------------------------------------------

    @POST("forgot-password")
    suspend fun forgotPassword(
        @Body request: ForgotPasswordRequest
    ): ForgotPasswordResponse


    // ---------------------------------------------------------
// RESET PASSWORD
// ---------------------------------------------------------

    @POST("reset-password")
    suspend fun resetPassword(
        @Body request: ResetPasswordRequest
    ): ResetPasswordResponse


    // ---------------------------------------------------------
    // ATTENDANCE
    // ---------------------------------------------------------

    @GET("attendance/{person_id}")
    suspend fun getAttendance(
        @Path("person_id") personId: String
    ): AttendanceResponse


    // ---------------------------------------------------------
    // NOTIFICATIONS
    // ---------------------------------------------------------

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


    // ---------------------------------------------------------
    // LEAVE - EMPLOYEE
    // ---------------------------------------------------------

    @Multipart
    @POST("leave/upload-attachment")
    suspend fun uploadLeaveAttachment(
        @Part file: MultipartBody.Part,
        @Part("person_id") personId: okhttp3.RequestBody
    ): LeaveAttachmentUploadResponse

    @POST("leave")
    suspend fun submitLeave(
        @Body request: LeaveApplicationRequest
    ): LeaveResponse

    @GET("leave/{personId}")
    suspend fun getLeaveApplications(
        @Path("personId") personId: String
    ): LeaveListResponse


    // ---------------------------------------------------------
    // LEAVE - ADMIN / MANAGER
    // ---------------------------------------------------------

    @GET("leave/all")
    suspend fun getAllLeaveApplications(): LeaveListResponse

    @PUT("leave/{leave_id}/approve")
    suspend fun approveLeave(
        @Path("leave_id") leaveId: String
    ): LeaveResponse

    @PUT("leave/{leave_id}/reject")
    suspend fun rejectLeave(
        @Path("leave_id") leaveId: String
    ): LeaveResponse




}

