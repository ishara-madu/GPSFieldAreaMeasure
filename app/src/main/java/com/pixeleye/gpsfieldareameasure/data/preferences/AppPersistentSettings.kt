package com.pixeleye.gpsfieldareameasure.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.maps.android.compose.MapType
import com.pixeleye.gpsfieldareameasure.model.MeasureMode
import com.pixeleye.gpsfieldareameasure.model.MeasurementUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Extension property for DataStore instance
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_settings")

class AppPersistentSettings(private val context: Context) {

    companion object {
        private val MAP_TYPE_KEY = stringPreferencesKey("last_map_type")
        private val UNIT_KEY = stringPreferencesKey("last_unit")
        private val MODE_KEY = stringPreferencesKey("last_mode")
        private val SHEET_STATE_KEY = stringPreferencesKey("bottom_sheet_state")
    }

    // Save map type
    suspend fun saveMapType(mapType: MapType) {
        context.dataStore.edit { prefs -> prefs[MAP_TYPE_KEY] = mapType.name }
    }

    // Get map type flow
    val mapTypeFlow: Flow<MapType> =
            context.dataStore.data.map { prefs ->
                val typeName = prefs[MAP_TYPE_KEY] ?: MapType.NORMAL.name
                try {
                    MapType.valueOf(typeName)
                } catch (e: IllegalArgumentException) {
                    MapType.NORMAL
                }
            }

    // Save unit
    suspend fun saveUnit(unit: MeasurementUnit) {
        context.dataStore.edit { prefs -> prefs[UNIT_KEY] = unit.name }
    }

    // Get unit flow
    val unitFlow: Flow<MeasurementUnit> =
            context.dataStore.data.map { prefs ->
                val unitName = prefs[UNIT_KEY] ?: MeasurementUnit.HECTARE.name
                try {
                    MeasurementUnit.valueOf(unitName)
                } catch (e: IllegalArgumentException) {
                    MeasurementUnit.HECTARE
                }
            }

    // Save mode
    suspend fun saveMode(mode: MeasureMode) {
        context.dataStore.edit { prefs -> prefs[MODE_KEY] = mode.name }
    }

    // Get mode flow
    val modeFlow: Flow<MeasureMode> =
            context.dataStore.data.map { prefs ->
                val modeName = prefs[MODE_KEY] ?: MeasureMode.MANUAL.name
                try {
                    MeasureMode.valueOf(modeName)
                } catch (e: IllegalArgumentException) {
                    MeasureMode.MANUAL
                }
            }

    // Save Sheet State
    suspend fun saveSheetState(state: String) {
        context.dataStore.edit { prefs -> prefs[SHEET_STATE_KEY] = state }
    }

    // Get Sheet State Flow
    val sheetStateFlow: Flow<String> =
        context.dataStore.data.map { prefs ->
            prefs[SHEET_STATE_KEY] ?: "Expanded" // Default to Expanded for first launch
        }
}
