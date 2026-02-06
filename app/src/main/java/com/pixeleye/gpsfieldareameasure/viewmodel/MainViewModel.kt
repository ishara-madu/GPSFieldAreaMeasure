package com.pixeleye.gpsfieldareameasure.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import androidx.compose.material3.SheetValue
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.maps.android.SphericalUtil
import com.google.maps.android.compose.MapType
import com.pixeleye.gpsfieldareameasure.data.local.AppDatabase
import com.pixeleye.gpsfieldareameasure.data.local.MeasurementEntity
import com.pixeleye.gpsfieldareameasure.data.preferences.AppPersistentSettings
import com.pixeleye.gpsfieldareameasure.model.MeasureMode
import com.pixeleye.gpsfieldareameasure.model.MeasurementUnit
import com.pixeleye.gpsfieldareameasure.model.Point
import com.pixeleye.gpsfieldareameasure.utils.FormatUtils
import com.pixeleye.gpsfieldareameasure.utils.LocationClient
import com.pixeleye.gpsfieldareameasure.billing.BillingManager
import com.pixeleye.gpsfieldareameasure.model.VipPackage
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class GpsStatus {
    NONE,
    SEARCHING,
    POOR, // >20m
    WEAK, // 15-20m
    GOOD, // 10-15m
    EXCELLENT // <10m
}


@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
data class MainUiState(
        val points: List<Point> = emptyList(),
        val area: String = "0.00 m²",
        val perimeter: String = "0 m",
        val areaValue: Double = 0.0,
        val perimeterValue: Double = 0.0,
        val mode: MeasureMode = MeasureMode.MANUAL,
        val unit: MeasurementUnit = MeasurementUnit.HECTARE,
        val selectedMapType: MapType = MapType.NORMAL,
        val gpsStatus: GpsStatus = GpsStatus.NONE,
        val currentAccuracy: Float? = null,
        val isTracking: Boolean = false,
        val isComplete: Boolean = false,
        val isModified: Boolean = false,
        val originalPoints: List<Point> = emptyList(),
        val selectedPointIndex: Int? = null,
        val sheetState: SheetValue = SheetValue.Expanded
)

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
class MainViewModel(application: Application) : AndroidViewModel(application) {
    
    private val billingManager = BillingManager(application)
    val vipPackages = billingManager.vipPackages

    private val locationClient by lazy { LocationClient(application) }

    // Room Database
    private val db by lazy {
        Room.databaseBuilder(application, AppDatabase::class.java, "gps-measure-db").build()
    }
    private val measurementDao by lazy { db.measurementDao() }
    private val gson = Gson()
    private val settings = AppPersistentSettings(application)

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        // Load saved preferences
        viewModelScope.launch {
            settings.mapTypeFlow.collect { savedMapType ->
                _uiState.update { it.copy(selectedMapType = savedMapType) }
            }
        }
        viewModelScope.launch {
            settings.unitFlow.collect { savedUnit ->
                _uiState.update { it.copy(unit = savedUnit) }
                recalculate()
            }
        }
        viewModelScope.launch {
            settings.modeFlow.collect { savedMode ->
                if (!_uiState.value.isTracking) {
                    _uiState.update { it.copy(mode = savedMode) }
                }
            }
        }
        viewModelScope.launch {
            settings.sheetStateFlow.collect { stateString ->
                val state = try {
                    SheetValue.valueOf(stateString)
                } catch (e: Exception) {
                    SheetValue.Expanded
                }
                _uiState.update { it.copy(sheetState = state) }
            }
        }
    }

    // History Flow
    val history = measurementDao.getAllMeasurements().asStateFlow(emptyList())

    private var trackingJob: Job? = null

    // Helper to convert Flow to StateFlow
    private fun <T> kotlinx.coroutines.flow.Flow<T>.asStateFlow(initialValue: T): StateFlow<T> {
        val state = MutableStateFlow(initialValue)
        viewModelScope.launch { collect { state.value = it } }
        return state.asStateFlow()
    }

    fun addPoint(latLng: LatLng) {
        if (_uiState.value.isComplete) return

        val currentPoints = _uiState.value.points.toMutableList()
        val newPoint = Point(latLng, currentPoints.size + 1)
        currentPoints.add(newPoint)

        // Clear selection when adding new point
        _uiState.update { it.copy(selectedPointIndex = null) }
        updateStateWithPoints(currentPoints)
    }

    fun undo() {
        val currentState = _uiState.value
        if (currentState.isComplete && currentState.isModified) {
            // Revert to original for loaded measurements
            _uiState.update { it.copy(points = it.originalPoints, isModified = false, selectedPointIndex = null) }
            recalculate()
        } else if (!currentState.isComplete && currentState.points.isNotEmpty()) {
            val currentPoints = currentState.points.toMutableList()
            currentPoints.removeLast()
            _uiState.update { it.copy(selectedPointIndex = null) }
            updateStateWithPoints(currentPoints)
        }
    }

    fun clearPoints() {
        _uiState.update { it.copy(points = emptyList(), originalPoints = emptyList(), isModified = false, isComplete = false, selectedPointIndex = null) }
        recalculate()
    }

    fun selectPoint(index: Int?) {
        _uiState.update { it.copy(selectedPointIndex = index) }
    }

    fun deleteSelectedPoint() {
        val currentIndex = _uiState.value.selectedPointIndex ?: return
        val currentPoints = _uiState.value.points.toMutableList()

        if (currentIndex in currentPoints.indices) {
            currentPoints.removeAt(currentIndex)
            // Renumber points? Maybe redundant but good for consistency
             val renumberedPoints = currentPoints.mapIndexed { i, point ->
                point.copy(sequenceNumber = i + 1)
            }
            _uiState.update { it.copy(selectedPointIndex = null) }
            updateStateWithPoints(renumberedPoints)
        }
    }

    fun setMode(mode: MeasureMode) {
        val currentMode = _uiState.value.mode
        if (mode != currentMode) {
            // Clear points when switching modes
            clearPoints()
        }
        _uiState.update { it.copy(mode = mode, selectedPointIndex = null) }
        viewModelScope.launch { settings.saveMode(mode) }
        if (mode == MeasureMode.MANUAL && _uiState.value.isTracking) {
            stopTracking()
        }
    }

    fun setUnit(unit: MeasurementUnit) {
        _uiState.update { it.copy(unit = unit) }
        recalculate()
        viewModelScope.launch { settings.saveUnit(unit) }
    }

    fun setMapType(mapType: MapType) {
        _uiState.update { it.copy(selectedMapType = mapType) }
        viewModelScope.launch { settings.saveMapType(mapType) }
    }

    fun saveSheetState(state: SheetValue) {
        // Do not update UI state here to avoid loops, just save to prefs
        // But we DO want to update UI state if it came from drag.
        // Actually, if it came from drag, validation happens in UI.
        // If we update _uiState here, it might trigger the LaunchedEffect in UI again.
        // UI is truth for drag. DataStore is truth for persistence.
        viewModelScope.launch { settings.saveSheetState(state.name) }
    }

    fun saveMeasurement(name: String) {
        val currentState = _uiState.value
        if (currentState.points.size < 3) return

        viewModelScope.launch {
            val pointsJson = gson.toJson(currentState.points)
            val entity =
                    MeasurementEntity(
                            name = name.ifBlank { "Untitled Measurement" },
                            area = currentState.areaValue,
                            perimeter = currentState.perimeterValue,
                            unit = currentState.unit.name,
                            pointsJson = pointsJson,
                            timestamp = System.currentTimeMillis()
                    )
            measurementDao.insertMeasurement(entity)
            
            // After saving, if it was a modified loaded measurement, update internal state
            _uiState.update { 
                it.copy(
                    originalPoints = it.points,
                    isModified = false
                )
            }
        }
    }

    fun deleteMeasurement(measurement: MeasurementEntity) {
        viewModelScope.launch { measurementDao.deleteMeasurement(measurement) }
    }

    fun loadMeasurement(measurement: MeasurementEntity) {
        try {
            val pointListType = object : com.google.gson.reflect.TypeToken<List<Point>>() {}.type
            val points: List<Point> = gson.fromJson(measurement.pointsJson, pointListType)

            _uiState.update {
                it.copy(
                        points = points,
                        originalPoints = points,
                        isModified = false,
                        mode = MeasureMode.MANUAL,
                        isTracking = false,
                        isComplete = true,
                        selectedPointIndex = null
                )
            }
            recalculate()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun startTracking() {
        if (_uiState.value.isTracking) return

        _uiState.update {
            it.copy(isTracking = true, isComplete = false, gpsStatus = GpsStatus.SEARCHING, selectedPointIndex = null)
        }

        trackingJob =
                locationClient
                        .getLocationUpdates(3000L)
                        .onEach { location ->
                            val accuracy = location.accuracy

                            // Update GPS status based on accuracy
                            val gpsStatus = when {
                                accuracy > 20f -> GpsStatus.POOR
                                accuracy > 15f -> GpsStatus.WEAK
                                accuracy > 10f -> GpsStatus.GOOD
                                else -> GpsStatus.EXCELLENT
                            }
                            
                            _uiState.update {
                                it.copy(gpsStatus = gpsStatus, currentAccuracy = accuracy)
                            }

                            // Reject poor accuracy readings completely
                            if (accuracy > 20f) {
                                return@onEach
                            }

                            val newLatLng = LatLng(location.latitude, location.longitude)
                            val currentPoints = _uiState.value.points

                            // First point: always add
                            if (currentPoints.isEmpty()) {
                                addTrackingPoint(newLatLng, currentPoints)
                                return@onEach
                            }

                            val lastPoint = currentPoints.last().latLng
                            val distance = SphericalUtil.computeDistanceBetween(lastPoint, newLatLng)

                            // Minimum distance filter to reduce jitter
                            if (distance < 3.0) {
                                return@onEach
                            }

                            // Second point: add if minimum distance met
                            if (currentPoints.size == 1) {
                                if (distance >= 5.0) {
                                    addTrackingPoint(newLatLng, currentPoints)
                                }
                                return@onEach
                            }

                            // Calculate bearing change for smart point addition
                            val secondLastPoint = currentPoints[currentPoints.size - 2].latLng
                            val previousBearing = SphericalUtil.computeHeading(secondLastPoint, lastPoint)
                            val currentBearing = SphericalUtil.computeHeading(lastPoint, newLatLng)
                            
                            // Normalize bearing difference to 0-180 range
                            var bearingDiff = kotlin.math.abs(currentBearing - previousBearing)
                            if (bearingDiff > 180) bearingDiff = 360 - bearingDiff

                            // Add point if:
                            // 1. Significant direction change (turn detected) OR
                            // 2. Long distance traveled (straight line needs points too)
                            val significantTurn = bearingDiff > 15.0
                            val longDistance = distance > 20.0

                            if (significantTurn || longDistance) {
                                addTrackingPoint(newLatLng, currentPoints)
                            }
                        }
                        .launchIn(viewModelScope)
    }

    private fun addTrackingPoint(latLng: LatLng, currentPoints: List<Point>) {
        val newPoint = Point(latLng, currentPoints.size + 1)
        val newPoints = currentPoints + newPoint
        updateStateWithPoints(newPoints)
    }

    fun stopTracking() {
        trackingJob?.cancel()
        _uiState.update {
            it.copy(isTracking = false, gpsStatus = GpsStatus.NONE, currentAccuracy = null)
        }
    }

    fun updatePointPosition(index: Int, newLatLng: LatLng) {
        val currentPoints = _uiState.value.points.toMutableList()
        if (index in currentPoints.indices) {
            currentPoints[index] = currentPoints[index].copy(latLng = newLatLng)
            updateStateWithPoints(currentPoints)
        }
    }

    private fun updateStateWithPoints(points: List<Point>) {
        _uiState.update { 
            val wasModified = if (it.isComplete) it.originalPoints != points else it.isModified
            it.copy(points = points, isModified = wasModified) 
        }
        recalculate()
    }

    private fun recalculate() {
        val points = _uiState.value.points
        if (points.size < 3) {
            _uiState.update {
                it.copy(
                        area = "0.00 ${it.unit.shortName}",
                        perimeter = "0 m",
                        areaValue = 0.0,
                        perimeterValue = 0.0
                )
            }
            return
        }

        val path = points.map { it.latLng }

        // Compute Area
        val areaSqMeters = SphericalUtil.computeArea(path)
        val perimeterMeters = SphericalUtil.computeLength(path)

        _uiState.update {
            it.copy(
                    area = FormatUtils.formatArea(areaSqMeters, it.unit),
                    perimeter = FormatUtils.formatDistance(perimeterMeters),
                    areaValue = areaSqMeters,
                    perimeterValue = perimeterMeters
            )
        }
    }
}
