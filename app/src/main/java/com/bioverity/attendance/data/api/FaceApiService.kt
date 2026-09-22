
package com.bioverity.attendance.data.api

import com.bioverity.attendance.data.model.LoginRequest
import com.bioverity.attendance.data.model.LoginResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface FaceApiService {

    @POST("login")
    suspend fun login(
        @Body request: LoginRequest
    ): LoginResponse
}
