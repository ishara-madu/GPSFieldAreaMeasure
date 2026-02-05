package com.pixeleye.gpsfieldareameasure.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polygon
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.SphericalUtil
import com.pixeleye.gpsfieldareameasure.model.MeasureMode
import com.pixeleye.gpsfieldareameasure.model.Point

private fun createDotBitmap(color: Int, size: Int = 30): com.google.android.gms.maps.model.BitmapDescriptor {
    val bitmap = android.graphics.Bitmap.createBitmap(size, size, android.graphics.Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    val paint = android.graphics.Paint().apply {
        this.color = color
        isAntiAlias = true
        style = android.graphics.Paint.Style.FILL
    }
    
    val strokePaint = android.graphics.Paint().apply {
        this.color = android.graphics.Color.WHITE
        isAntiAlias = true
        style = android.graphics.Paint.Style.STROKE
        strokeWidth = 4f
    }
    
    val radius = size / 2f
    canvas.drawCircle(radius, radius, radius - 2f, paint)
    canvas.drawCircle(radius, radius, radius - 2f, strokePaint)
    
    return com.google.android.gms.maps.model.BitmapDescriptorFactory.fromBitmap(bitmap)
}

private fun createActionMarkerBitmap(
    context: android.content.Context,
    isDelete: Boolean,
    size: Int = 120,
    backgroundColor: Int,
    iconColor: Int
): com.google.android.gms.maps.model.BitmapDescriptor {
    val bitmap = android.graphics.Bitmap.createBitmap(size, size, android.graphics.Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    val center = size / 2f
    val radius = size * 0.4f

    // Shadow
    val shadowPaint = android.graphics.Paint().apply {
        color = android.graphics.Color.argb(80, 0, 0, 0)
        style = android.graphics.Paint.Style.FILL
        maskFilter = android.graphics.BlurMaskFilter(6f, android.graphics.BlurMaskFilter.Blur.NORMAL)
    }
    canvas.drawCircle(center, center + 4f, radius, shadowPaint)

    // Background Circle
    val bgPaint = android.graphics.Paint().apply {
        color = backgroundColor
        style = android.graphics.Paint.Style.FILL
        isAntiAlias = true
    }
    canvas.drawCircle(center, center, radius, bgPaint)

    // Load and draw custom icon with tinting
    val iconRes = if (isDelete) com.pixeleye.gpsfieldareameasure.R.drawable.delete_custom else com.pixeleye.gpsfieldareameasure.R.drawable.move_custom
    val drawable = androidx.core.content.ContextCompat.getDrawable(context, iconRes)
    if (drawable != null) {
        val wrappedDrawable = androidx.core.graphics.drawable.DrawableCompat.wrap(drawable).mutate()
        androidx.core.graphics.drawable.DrawableCompat.setTint(wrappedDrawable, iconColor)
        
        val iconSize = (radius * 0.8f).toInt()
        val left = (center - iconSize / 2).toInt()
        val top = (center - iconSize / 2).toInt()
        val right = left + iconSize
        val bottom = top + iconSize
        
        wrappedDrawable.setBounds(left, top, right, bottom)
        wrappedDrawable.draw(canvas)
    }

    return com.google.android.gms.maps.model.BitmapDescriptorFactory.fromBitmap(bitmap)
}

@Composable
fun MapContent(
    points: List<Point>,
    mode: MeasureMode,
    isTracking: Boolean,
    cameraPositionState: com.google.maps.android.compose.CameraPositionState,
    selectedMapType: MapType = MapType.NORMAL,
    selectedPointIndex: Int? = null,
    onMapClick: (LatLng) -> Unit,
    onPointClick: (Int) -> Unit = {},
    onPointDragEnd: (Int, LatLng) -> Unit = { _, _ -> },
    onDeletePoint: (Int) -> Unit = {},
    isComplete: Boolean = false,
    isLocationPermissionGranted: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    remember(context) {
        MapsInitializer.initialize(context)
    }

    // Colors
    val purpleColor = 0xFF9C27B0.toInt()
    val yellowColor = 0xFFFFEB3B.toInt()
    val greenColor = 0xFF4CAF50.toInt()
    val redColor = 0xFFF44336.toInt()
    val blueColor = 0xFF2196F3.toInt()

    // Theming for Action Icons
    // Theming for Action Icons
    val primaryColorArgb = MaterialTheme.colorScheme.primary.toArgb()
    val surfaceColorArgb = MaterialTheme.colorScheme.surface.toArgb()
    val onSurfaceColorArgb = MaterialTheme.colorScheme.onSurface.toArgb()

    // Bg=Surface, Icon=Primary (to match theme)
    val markerBgColor = surfaceColorArgb
    val markerIconColor = MaterialTheme.colorScheme.onPrimaryContainer.toArgb()

    val defaultDot = remember(primaryColorArgb) { createDotBitmap(purpleColor) }
    val yellowDot = remember { createDotBitmap(yellowColor) }
    val deleteIcon = remember(markerBgColor, markerIconColor, context) { 
        createActionMarkerBitmap(context, isDelete = true, backgroundColor = markerBgColor, iconColor = markerIconColor) 
    }
    val moveIcon = remember(markerBgColor, markerIconColor, context) { 
        createActionMarkerBitmap(context, isDelete = false, backgroundColor = markerBgColor, iconColor = markerIconColor) 
    }

    LaunchedEffect(points.lastOrNull(), isTracking) {
        if (isTracking && points.isNotEmpty()) {
            points.last().latLng.let {
                cameraPositionState.animate(
                    update = CameraUpdateFactory.newLatLng(it),
                    durationMs = 500
                )
            }
        }
    }

    GoogleMap(
        modifier = modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(
            isMyLocationEnabled = isLocationPermissionGranted,
            mapType = selectedMapType
        ),
        uiSettings = MapUiSettings(
            zoomControlsEnabled = false,
            myLocationButtonEnabled = false
        ),
        onMapClick = {
            if (mode == MeasureMode.MANUAL) {
                onMapClick(it)
            }
        }
    ) {
        val path = points.map { it.latLng }

        // Draw Polygon Fill
        if (path.size >= 3) {
            Polygon(
                points = path,
                fillColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                strokeColor = Color.Transparent,
                strokeWidth = 0f
            )
        }

        // Draw Polyline
        if (path.isNotEmpty()) {
            Polyline(
                points = path,
                color = MaterialTheme.colorScheme.primary,
                width = 8f,
                jointType = JointType.ROUND
            )
        }

        // Draw all markers (both modes now support selection/drag)
        points.forEachIndexed { index, point ->
            androidx.compose.runtime.key(point.sequenceNumber) {
                val markerState = com.google.maps.android.compose.rememberMarkerState(position = point.latLng)
                
                LaunchedEffect(point.latLng) {
                    markerState.position = point.latLng
                }

                LaunchedEffect(markerState.dragState) {
                    if (markerState.dragState == com.google.maps.android.compose.DragState.END) {
                        onPointDragEnd(index, markerState.position)
                    }
                }

                val isSelected = index == selectedPointIndex
                val iconBitmap = if (isSelected) yellowDot else defaultDot

                Marker(
                    state = markerState,
                    title = "Point ${point.sequenceNumber}",
                    icon = iconBitmap,
                    anchor = androidx.compose.ui.geometry.Offset(0.5f, 0.5f),
                    draggable = isSelected, // Only draggable when selected
                    onClick = {
                        onPointClick(index)
                        true
                    }
                )

                // Show action icons when selected
                if (isSelected) {
                    val zoom = cameraPositionState.position.zoom
                    val currentMainPos = markerState.position // Use visual position
                    val lat = currentMainPos.latitude
                    
                    // Calculate distance in meters to maintain constant screen pixels
                    val metersPerPixel = 156543.03392 * kotlin.math.cos(lat * kotlin.math.PI / 180.0) / Math.pow(2.0, zoom.toDouble())
                    
                    // Offsets (Pixels * metersPerPixel)
                    val deleteOffsetDist = 30.0 * metersPerPixel
                    val moveOffsetDist = 35.0 * metersPerPixel

                    val deletePos = SphericalUtil.computeOffset(currentMainPos, deleteOffsetDist, 0.0)
                    
                    // Move Marker State
                    val targetMovePos = SphericalUtil.computeOffset(currentMainPos, moveOffsetDist, 180.0)
                    
                    val moveMarkerState = com.google.maps.android.compose.rememberMarkerState(position = targetMovePos)
                    
                    // Bidirectional Visual Sync
                    LaunchedEffect(targetMovePos, moveMarkerState.dragState) {
                        if (moveMarkerState.dragState != com.google.maps.android.compose.DragState.DRAG) {
                            moveMarkerState.position = targetMovePos
                        }
                    }
                    
                    LaunchedEffect(moveMarkerState.position, moveMarkerState.dragState) {
                        if (moveMarkerState.dragState == com.google.maps.android.compose.DragState.DRAG) {
                            val newMainPos = SphericalUtil.computeOffset(moveMarkerState.position, moveOffsetDist, 0.0)
                            markerState.position = newMainPos
                        }
                    }

                    // Connecting Line (The "Stem")
                    Polyline(
                        points = if (isComplete) listOf(currentMainPos, moveMarkerState.position) else listOf(deletePos, currentMainPos, moveMarkerState.position),
                        color = androidx.compose.ui.graphics.Color(markerIconColor),
                        width = 5f,
                        pattern = listOf(com.google.android.gms.maps.model.Dot(), com.google.android.gms.maps.model.Gap(10f))
                    )

                    // Delete Icon (Above) - Hide if saved area is loaded
                    if (!isComplete) {
                        Marker(
                            state = MarkerState(position = deletePos),
                            icon = deleteIcon,
                            anchor = androidx.compose.ui.geometry.Offset(0.5f, 0.5f),
                            zIndex = 20f,
                            onClick = {
                                onDeletePoint(index)
                                true
                            }
                        )
                    }

                    // Handle Move Drag End (Commit change)
                    LaunchedEffect(moveMarkerState.dragState) {
                         if (moveMarkerState.dragState == com.google.maps.android.compose.DragState.END) {
                             val newMainPos = SphericalUtil.computeOffset(moveMarkerState.position, moveOffsetDist, 0.0)
                             onPointDragEnd(index, newMainPos)
                         }
                    }

                    // Move Icon (Below)
                    Marker(
                        state = moveMarkerState,
                        icon = moveIcon,
                        anchor = androidx.compose.ui.geometry.Offset(0.5f, 0.5f),
                        zIndex = 20f,
                        draggable = true,
                        onClick = { true }
                    )
                }
            }
        }
    }
}

