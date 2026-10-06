
package com.bioverity.attendance.data.session

import android.content.Context

object AppSession {

    private const val PREF_NAME = "bioverity_session"

    private const val KEY_LOGGED_IN = "logged_in"
    private const val KEY_PERSON_ID = "person_id"
    private const val KEY_EMPLOYEE_ID = "employee_id"
    private const val KEY_NAME = "name"
    private const val KEY_EMAIL = "email"
    private const val KEY_IMAGE_URL = "image_url"
    private const val KEY_ROLE = "role"

    private fun preferences(context: Context) =
        context.getSharedPreferences(
            PREF_NAME,
            Context.MODE_PRIVATE
        )

    fun saveEmployee(
        context: Context,
        personId: String,
        employeeId: String,
        name: String,
        email: String,
        imageUrl: String? = null,
        role: String? = null
    ) {
        preferences(context)
            .edit()
            .putBoolean(KEY_LOGGED_IN, true)
            .putString(KEY_PERSON_ID, personId)
            .putString(KEY_EMPLOYEE_ID, employeeId)
            .putString(KEY_NAME, name)
            .putString(KEY_EMAIL, email)
            .putString(KEY_IMAGE_URL, imageUrl)
            .putString(KEY_ROLE, role?.uppercase())
            .apply()
    }

    fun isLoggedIn(context: Context): Boolean =
        preferences(context)
            .getBoolean(KEY_LOGGED_IN, false)

    fun getPersonId(context: Context): String? =
        preferences(context)
            .getString(KEY_PERSON_ID, null)

    fun getEmployeeId(context: Context): String? =
        preferences(context)
            .getString(KEY_EMPLOYEE_ID, null)

    fun getName(context: Context): String? =
        preferences(context)
            .getString(KEY_NAME, null)

    fun getEmail(context: Context): String? =
        preferences(context)
            .getString(KEY_EMAIL, null)

    fun getImageUrl(context: Context): String? =
        preferences(context)
            .getString(KEY_IMAGE_URL, null)

    fun getRole(context: Context): String? =
        preferences(context)
            .getString(KEY_ROLE, null)

    fun isAdmin(context: Context): Boolean =
        getRole(context) == "ADMIN"

    fun isManager(context: Context): Boolean =
        getRole(context) == "MANAGER"

    fun isAdminOrManager(context: Context): Boolean {
        val role = getRole(context)
        return role == "ADMIN" || role == "MANAGER"
    }

    fun logout(context: Context) {
        preferences(context)
            .edit()
            .clear()
            .apply()
    }
}

