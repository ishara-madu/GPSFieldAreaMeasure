package com.pixeleye.gpsfieldareameasure.ui.screens

import android.Manifest
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import android.app.Activity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.rememberCameraPositionState
import com.pixeleye.gpsfieldareameasure.model.MeasureMode
import com.pixeleye.gpsfieldareameasure.ui.components.*
import com.pixeleye.gpsfieldareameasure.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun MainScreen(viewModel: MainViewModel, onOpenDrawer: () -> Unit, onNavigateToVip: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    val haptic = LocalHapticFeedback.current

    val locationPermissionsState =
            rememberMultiplePermissionsState(
                    listOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    )
            )

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val applicationContext = context.applicationContext
    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(applicationContext)
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(6.9271, 79.8612), 15f)
    }

    var showSaveDialog by remember { mutableStateOf(false) }
    var showMapTypeDialog by remember { mutableStateOf(false) }
    var showRationale by remember { mutableStateOf(false) }
    var showSettingsRedirect by remember { mutableStateOf(false) }
    var saveName by remember { mutableStateOf("") }
    var hasFocusedInitialLocation by remember { mutableStateOf(false) }

    // Theming for FABs
    // Use Material Theme colors
    val fabContainerColor = MaterialTheme.colorScheme.primaryContainer
    val fabContentColor = MaterialTheme.colorScheme.onPrimaryContainer
    val fabBorder = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)

    if (showSaveDialog) {
        SaveMeasurementDialog(
                area = uiState.area,
                perimeter = uiState.perimeter,
                unit = uiState.unit,
                pointsCount = uiState.points.size,
                onDismiss = { showSaveDialog = false },
                onSave = { name ->
                    viewModel.saveMeasurement(name)
                    showSaveDialog = false
                    saveName = ""
                    
                    // Show interstitial ad
                    (applicationContext as? Activity ?: (context as? Activity))?.let { activity ->
                        InterstitialAdManager.showAd(activity)
                    }

                    scope.launch {
                        snackbarHostState.showSnackbar(
                                message = "Measurement saved!",
                                duration = androidx.compose.material3.SnackbarDuration.Short
                        )
                    }
                }
        )
    }

    LaunchedEffect(key1 = true) {
        if (!locationPermissionsState.allPermissionsGranted) {
            locationPermissionsState.launchMultiplePermissionRequest()
        }
    }

    LaunchedEffect(uiState.isTracking, uiState.mode) {
        if (uiState.isTracking && uiState.mode == MeasureMode.AUTO) {
            snackbarHostState.showSnackbar(
                    message = "For best accuracy: Clear sky view, hold phone steady, wait 20s",
                    duration = androidx.compose.material3.SnackbarDuration.Long
            )
        }
    }

    // Auto focus on current location when permission is granted (Initial)
    LaunchedEffect(locationPermissionsState.allPermissionsGranted) {
        if (locationPermissionsState.allPermissionsGranted &&
                        !hasFocusedInitialLocation &&
                        !uiState.isComplete
        ) {
            try {
                fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                        .addOnSuccessListener { location ->
                            location?.let {
                                scope.launch {
                                    cameraPositionState.animate(
                                            CameraUpdateFactory.newLatLngZoom(
                                                    LatLng(it.latitude, it.longitude),
                                                    18f
                                            )
                                    )
                                    hasFocusedInitialLocation = true
                                }
                            }
                        }
            } catch (e: SecurityException) {
                // Permission handled by state
            }
        }
    }

    // Auto focus on current location when mode changes
    LaunchedEffect(uiState.mode) {
        if (locationPermissionsState.allPermissionsGranted && !uiState.isComplete) {
            try {
                fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                        .addOnSuccessListener { location ->
                            location?.let {
                                scope.launch {
                                    cameraPositionState.animate(
                                            CameraUpdateFactory.newLatLngZoom(
                                                    LatLng(it.latitude, it.longitude),
                                                    18f
                                            )
                                    )
                                }
                            }
                        }
            } catch (e: SecurityException) {
                // Permission handled by state
            }
        }
    }

    // Auto focus on polygon when measurement is completed/loaded
    LaunchedEffect(uiState.isComplete, uiState.points) {
        if (uiState.isComplete && uiState.points.size >= 3) {
            val builder = LatLngBounds.builder()
            uiState.points.forEach { builder.include(it.latLng) }
            val bounds = builder.build()

            cameraPositionState.animate(CameraUpdateFactory.newLatLngBounds(bounds, 150), 1000)
        }
    }

    val scaffoldState =
            rememberBottomSheetScaffoldState(
                    bottomSheetState =
                            rememberStandardBottomSheetState(
                                    initialValue = uiState.sheetState,
                                    skipHiddenState = true
                            )
            )

    // Track if we should skip the first save (to avoid overwriting saved state on app launch)
    var isFirstComposition by remember { mutableStateOf(true) }
    
    // Save sheet state when it changes (but skip the initial composition)
    LaunchedEffect(scaffoldState.bottomSheetState.currentValue) {
        if (isFirstComposition) {
            isFirstComposition = false
        } else {
            viewModel.saveSheetState(scaffoldState.bottomSheetState.currentValue)
        }
    }

    // Scope for collapsing sheet on map click
    val sheetScope = rememberCoroutineScope()

    BottomSheetScaffold(
            scaffoldState = scaffoldState,
            topBar = {
                AreaTopBar(onOpenDrawer = onOpenDrawer, onNavigateToVip = onNavigateToVip)
            },
            sheetContent = {
                ResultSheet(
                        area = uiState.area,
                        perimeter = uiState.perimeter,
                        currentMode = uiState.mode,
                        currentUnit = uiState.unit,
                        gpsStatus = uiState.gpsStatus,
                        currentAccuracy = uiState.currentAccuracy,
                        onModeChange = viewModel::setMode,
                        onUnitChange = viewModel::setUnit,
                        onSave = { showSaveDialog = true }
                )
            },
            sheetPeekHeight = 120.dp, // Enough to show Area
            sheetDragHandle = { BottomSheetDefaults.DragHandle() },
            sheetSwipeEnabled = true,
            sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            sheetContainerColor = MaterialTheme.colorScheme.surface,
            containerColor = MaterialTheme.colorScheme.background, // Match theme background
            snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(top = paddingValues.calculateTopPadding())) {
            Box(modifier = Modifier.weight(1f)) {
                // Map
                MapContent(
                        points = uiState.points,
                        mode = uiState.mode,
                        isTracking = uiState.isTracking,
                        cameraPositionState = cameraPositionState,
                        selectedMapType = uiState.selectedMapType,
                        selectedPointIndex = uiState.selectedPointIndex,
                        onMapClick = { latLng ->
                            viewModel.addPoint(latLng)
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            // Collapse sheet to partial when user clicks map
                            sheetScope.launch {
                                scaffoldState.bottomSheetState.partialExpand()
                            }
                        },
                        onPointClick = viewModel::selectPoint,
                        onPointDragEnd = viewModel::updatePointPosition,
                        onDeletePoint = { index ->
                            viewModel.selectPoint(index)
                            viewModel.deleteSelectedPoint()
                        },
                        isComplete = uiState.isComplete,
                        isLocationPermissionGranted = locationPermissionsState.allPermissionsGranted,
                        modifier = Modifier.fillMaxSize()
                )

                // Overlays: Top-Right
                Column(
                        modifier = Modifier.align(androidx.compose.ui.Alignment.TopEnd).padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(56.dp)
                ) {
                    // Map Type Selector Button
                    FloatingActionButton(
                            onClick = { showMapTypeDialog = true },
                            containerColor = fabContainerColor,
                            contentColor = fabContentColor,
                            modifier =
                                    Modifier.size(56.dp)
                                            .border(fabBorder, FloatingActionButtonDefaults.shape)
                    ) {
                        Icon(
                                androidx.compose.material.icons.Icons.Default.Layers,
                                contentDescription = "Map Type"
                        )
                    }

                    // My Location Button
                    FloatingActionButton(
                            onClick = {
                                if (locationPermissionsState.allPermissionsGranted) {
                                    try {
                                        fusedLocationClient.lastLocation.addOnSuccessListener { location
                                            ->
                                            location?.let {
                                                scope.launch {
                                                    cameraPositionState.animate(
                                                            CameraUpdateFactory.newLatLngZoom(
                                                                    LatLng(it.latitude, it.longitude),
                                                                    19f
                                                            )
                                                    )
                                                }
                                            }
                                        }
                                    } catch (e: SecurityException) {
                                        // Handled by permission check
                                    }
                                } else if (locationPermissionsState.shouldShowRationale) {
                                    showRationale = true
                                } else {
                                    val preferences = applicationContext.getSharedPreferences("prefs", android.content.Context.MODE_PRIVATE)
                                    val hasRequestedBefore = preferences.getBoolean("location_requested", false)
                                    
                                    if (hasRequestedBefore) {
                                        showSettingsRedirect = true
                                    } else {
                                        locationPermissionsState.launchMultiplePermissionRequest()
                                        preferences.edit().putBoolean("location_requested", true).apply()
                                    }
                                }
                            },
                            containerColor = fabContainerColor,
                            contentColor = fabContentColor,
                            modifier =
                                    Modifier.size(56.dp)
                                            .border(fabBorder, FloatingActionButtonDefaults.shape)
                    ) {
                        Icon(
                                androidx.compose.material.icons.Icons.Default.MyLocation,
                                contentDescription = "My Location"
                        )
                    }
                }

                // Overlays: Bottom-Right
                Column(
                        modifier =
                                Modifier.align(androidx.compose.ui.Alignment.BottomEnd)
                                        .padding(16.dp)
                                        .padding(bottom = 120.dp), // Lift above bottom sheet peek
                        horizontalAlignment = androidx.compose.ui.Alignment.End
                ) {

                    // Save Button (Extended)
                    val showSave = if (uiState.isComplete) uiState.isModified else (uiState.points.size >= 3 && !uiState.isTracking)
                    if (showSave) {
                        FloatingActionButton(
                                onClick = { showSaveDialog = true },
                                containerColor = fabContainerColor,
                                contentColor = fabContentColor,
                                modifier =
                                        Modifier.border(
                                                fabBorder,
                                                FloatingActionButtonDefaults.extendedFabShape
                                        )
                        ) { Icon(Icons.Default.Save, contentDescription = "Save") }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Undo Button
                    val showUndo = if (uiState.isComplete) uiState.isModified else (uiState.points.isNotEmpty() && uiState.selectedPointIndex == null)
                    if (showUndo) {
                        FloatingActionButton(
                                onClick = viewModel::undo,
                                containerColor = fabContainerColor,
                                contentColor = fabContentColor,
                                modifier =
                                        Modifier.size(56.dp)
                                                .border(fabBorder, FloatingActionButtonDefaults.shape)
                        ) { Icon(Icons.Default.Undo, contentDescription = "Undo") }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Clear Button
                    if (uiState.points.isNotEmpty()) {
                        FloatingActionButton(
                                onClick = {
                                    viewModel.clearPoints()
                                    if (locationPermissionsState.allPermissionsGranted) {
                                        try {
                                            fusedLocationClient.lastLocation.addOnSuccessListener {
                                                    location ->
                                                location?.let {
                                                    scope.launch {
                                                        cameraPositionState.animate(
                                                                CameraUpdateFactory.newLatLngZoom(
                                                                        LatLng(
                                                                                it.latitude,
                                                                                it.longitude
                                                                        ),
                                                                        18f
                                                                )
                                                        )
                                                    }
                                                }
                                            }
                                        } catch (e: SecurityException) {}
                                    }
                                },
                                containerColor = fabContainerColor,
                                contentColor = fabContentColor,
                                modifier =
                                        Modifier.size(56.dp)
                                                .border(fabBorder, FloatingActionButtonDefaults.shape)
                        ) { Icon(Icons.Default.Refresh, contentDescription = "Clear") }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Auto Mode Controls
                    if (uiState.mode == MeasureMode.AUTO) {
                        FloatingActionButton(
                                onClick = {
                                    if (uiState.isTracking) viewModel.stopTracking()
                                    else viewModel.startTracking()
                                },
                                containerColor = fabContainerColor,
                                contentColor = if (uiState.isTracking) Color.Red else fabContentColor,
                                modifier =
                                        Modifier.size(56.dp)
                                                .border(fabBorder, FloatingActionButtonDefaults.shape)
                        ) {
                            Icon(
                                    imageVector =
                                            if (uiState.isTracking) Icons.Default.Stop
                                            else Icons.Default.PlayArrow,
                                    contentDescription =
                                            if (uiState.isTracking) "Stop Tracking"
                                            else "Start Tracking"
                            )
                        }
                    }
                }
            } // End of Map Box
            
            // Banner Ad at the bottom
            BannerAdView()
        }
    }

    if (showMapTypeDialog) {
        MapTypeDialog(
                currentMapType = uiState.selectedMapType,
                onDismiss = { showMapTypeDialog = false },
                onApply = { type ->
                    viewModel.setMapType(type)
                    showMapTypeDialog = false
                }
        )
    }

    if (showRationale) {
        LocationRationaleDialog(
            onDismiss = { showRationale = false },
            onConfirm = {
                showRationale = false
                locationPermissionsState.launchMultiplePermissionRequest()
            }
        )
    }

    if (showSettingsRedirect) {
        SettingsRedirectDialog(
            onDismiss = { showSettingsRedirect = false },
            onGoToSettings = {
                showSettingsRedirect = false
                val intent = android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = android.net.Uri.fromParts("package", applicationContext.packageName, null)
                    flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                }
                applicationContext.startActivity(intent)
            }
        )
    }
}
