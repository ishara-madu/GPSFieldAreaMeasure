package com.pixeleye.gpsfieldareameasure.model

import com.google.android.gms.maps.model.LatLng

data class Point(
    val latLng: LatLng,
    val sequenceNumber: Int,
    val timestamp: Long = System.currentTimeMillis()
)
