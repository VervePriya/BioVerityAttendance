package com.bioverity.attendance.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bioverity.attendance.location.LocationManager
import com.bioverity.attendance.location.LocationResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class LocationUiState {

    data object NotChecked : LocationUiState()

    data object Checking : LocationUiState()

    data object PermissionRequired : LocationUiState()

    data object LocationUnavailable : LocationUiState()

    data class Verified(
        val distanceMeters: Float
    ) : LocationUiState()

    data class OutsideOffice(
        val distanceMeters: Float
    ) : LocationUiState()

    data class Error(
        val message: String
    ) : LocationUiState()
}

class LocationViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val locationManager =
        LocationManager(application)

    private val _locationState =
        MutableStateFlow<LocationUiState>(
            LocationUiState.NotChecked
        )

    val locationState: StateFlow<LocationUiState> =
        _locationState.asStateFlow()

    fun checkLocation() {

        _locationState.value =
            LocationUiState.Checking

        viewModelScope.launch {

            when (
                val result =
                    locationManager.getCurrentLocation()
            ) {

                LocationResult.PermissionRequired -> {

                    _locationState.value =
                        LocationUiState.PermissionRequired
                }

                LocationResult.Unavailable -> {

                    _locationState.value =
                        LocationUiState.LocationUnavailable
                }

                is LocationResult.Success -> {

                    _locationState.value =
                        if (result.insideOffice) {

                            LocationUiState.Verified(
                                distanceMeters =
                                    result.distanceMeters
                            )

                        } else {

                            LocationUiState.OutsideOffice(
                                distanceMeters =
                                    result.distanceMeters
                            )
                        }
                }

                is LocationResult.Error -> {

                    _locationState.value =
                        LocationUiState.Error(
                            result.message
                        )
                }
            }
        }
    }
}