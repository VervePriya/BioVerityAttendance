package com.bioverity.attendance.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bioverity.attendance.data.model.Employee
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class ProfileViewModel : ViewModel() {

    companion object {
        private const val BASE_URL =
            "http://127.0.0.1:8000"
    }

    private val client = OkHttpClient()

    var employee by mutableStateOf<Employee?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun loadEmployee(personId: String) {

        if (personId.isBlank()) {

            errorMessage =
                "Employee ID is missing"

            return
        }

        if (isLoading) {
            return
        }

        isLoading = true
        errorMessage = null

        viewModelScope.launch(Dispatchers.IO) {

            try {

                val request =
                    Request.Builder()
                        .url(
                            "$BASE_URL/employee/$personId"
                        )
                        .get()
                        .build()

                client.newCall(request)
                    .execute()
                    .use { response ->

                        val responseBody =
                            response.body?.string()

                        if (!response.isSuccessful) {

                            throw Exception(
                                "API error: ${response.code}"
                            )
                        }

                        if (responseBody.isNullOrBlank()) {

                            throw Exception(
                                "Empty response from server"
                            )
                        }

                        val json =
                            JSONObject(responseBody)

                        val success =
                            json.optBoolean(
                                "success",
                                false
                            )

                        if (!success) {

                            val reason =
                                json.optString(
                                    "reason",
                                    "Unable to load employee"
                                )

                            throw Exception(reason)
                        }

                        val employeeJson =
                            json.optJSONObject(
                                "employee"
                            )
                                ?: throw Exception(
                                    "Employee data missing"
                                )

                        val employeeData =
                            Employee(

                                id =
                                    employeeJson
                                        .optString(
                                            "id",
                                            personId
                                        ),

                                name =
                                    employeeJson
                                        .optString(
                                            "name",
                                            "Employee"
                                        ),

                                employeeId =
                                    employeeJson
                                        .optString(
                                            "employee_id",
                                            ""
                                        )
                                        .takeIf {
                                            it.isNotBlank()
                                        },

                                email =
                                    employeeJson
                                        .optString(
                                            "email",
                                            ""
                                        )
                                        .takeIf {
                                            it.isNotBlank()
                                        },

                                phone =
                                    employeeJson
                                        .optString(
                                            "phone",
                                            ""
                                        )
                                        .takeIf {
                                            it.isNotBlank()
                                        },

                                position =
                                    employeeJson
                                        .optString(
                                            "position",
                                            ""
                                        )
                                        .takeIf {
                                            it.isNotBlank()
                                        },

                                department =
                                    employeeJson
                                        .optString(
                                            "department",
                                            ""
                                        )
                                        .takeIf {
                                            it.isNotBlank()
                                        },

                                imageUrl =
                                    employeeJson
                                        .optString(
                                            "image_url",
                                            ""
                                        )
                                        .takeIf {
                                            it.isNotBlank()
                                        }
                            )

                        employee =
                            employeeData

                        isLoading = false
                        errorMessage = null
                    }

            } catch (e: Exception) {

                employee = null

                errorMessage =
                    e.message
                        ?: "Unable to load profile"

                isLoading = false
            }
        }
    }
}