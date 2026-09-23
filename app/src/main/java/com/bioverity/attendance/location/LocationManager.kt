package com.bioverity.attendance.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class LocationManager(
    private val context: Context
) {

    companion object {

        // Temporary office/reference location for testing
        const val OFFICE_LATITUDE = 6.872289938616773
        const val OFFICE_LONGITUDE = 79.86588002698302

        // Employee must be within 100 meters
        const val OFFICE_RADIUS_METERS = 100f
    }

    private val fusedLocationClient =
        LocationServices.getFusedLocationProviderClient(context)

    fun hasLocationPermission(): Boolean {

        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): LocationResult {

        if (!hasLocationPermission()) {
            return LocationResult.PermissionRequired
        }

        return try {

            val location = suspendCancellableCoroutine<Location?> { continuation ->

                fusedLocationClient
                    .getCurrentLocation(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        null
                    )
                    .addOnSuccessListener { location ->
                        continuation.resume(location)
                    }
                    .addOnFailureListener {
                        continuation.resume(null)
                    }
            }

            if (location == null) {
                LocationResult.Unavailable
            } else {

                val distance = calculateDistanceFromOffice(
                    location.latitude,
                    location.longitude
                )

                LocationResult.Success(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    distanceMeters = distance,
                    insideOffice = distance <= OFFICE_RADIUS_METERS
                )
            }

        } catch (e: Exception) {

            LocationResult.Error(
                e.message ?: "Unable to get location"
            )
        }
    }

    private fun calculateDistanceFromOffice(
        latitude: Double,
        longitude: Double
    ): Float {

        val results = FloatArray(1)

        Location.distanceBetween(
            OFFICE_LATITUDE,
            OFFICE_LONGITUDE,
            latitude,
            longitude,
            results
        )

        return results[0]
    }
}

sealed class LocationResult {

    data object PermissionRequired : LocationResult()

    data object Unavailable : LocationResult()

    data class Success(
        val latitude: Double,
        val longitude: Double,
        val distanceMeters: Float,
        val insideOffice: Boolean
    ) : LocationResult()

    data class Error(
        val message: String
    ) : LocationResult()
}