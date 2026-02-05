package com.pixeleye.gpsfieldareameasure.utils

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class LocationClient(private val context: Context) {
    private val client: FusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    fun getLocationUpdates(intervalMs: Long): Flow<android.location.Location> = callbackFlow {
        val request =
                LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000L) // 3 second interval
                        .setMinUpdateDistanceMeters(5f) // Reduced for tighter tracking
                        .setMinUpdateIntervalMillis(2000L) // Min 2s between updates
                        .setMaxUpdateDelayMillis(10000L) // Faster batching
                        .setWaitForAccurateLocation(true) // Wait for good fix
                        .build()

        val callback =
                object : LocationCallback() {
                    override fun onLocationResult(result: LocationResult) {
                        result.lastLocation?.let { location -> trySend(location) }
                    }
                }

        client.requestLocationUpdates(request, callback, Looper.getMainLooper())

        awaitClose { client.removeLocationUpdates(callback) }
    }
}
