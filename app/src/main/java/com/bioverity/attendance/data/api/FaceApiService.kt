package com.bioverity.attendance.data.api

import com.bioverity.attendance.data.model.AttendanceResponse
import com.bioverity.attendance.data.model.LoginRequest
import com.bioverity.attendance.data.model.LoginResponse

import retrofit2.http.Body
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
}