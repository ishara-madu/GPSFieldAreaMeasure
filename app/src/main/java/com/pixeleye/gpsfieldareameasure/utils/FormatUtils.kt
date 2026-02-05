package com.pixeleye.gpsfieldareameasure.utils

import com.pixeleye.gpsfieldareameasure.model.MeasurementUnit
import java.text.DecimalFormat

object FormatUtils {
    private val decimalFormat = DecimalFormat("#,##0.00")
    private val integerFormat = DecimalFormat("#,##0")

    fun formatArea(areaSqMeters: Double, unit: MeasurementUnit): String {
        val value = when (unit) {
            MeasurementUnit.SQUARE_METER -> areaSqMeters
            MeasurementUnit.HECTARE -> areaSqMeters / 10000.0
            MeasurementUnit.ACRE -> areaSqMeters * 0.000247105
            MeasurementUnit.SQUARE_KILOMETER -> areaSqMeters / 1_000_000.0
            MeasurementUnit.SQUARE_FEET -> areaSqMeters * 10.7639
        }
        return "${decimalFormat.format(value)} ${unit.shortName}"
    }

    fun formatDistance(distanceMeters: Double): String {
        return if (distanceMeters >= 1000) {
            "${decimalFormat.format(distanceMeters / 1000)} km"
        } else {
            "${integerFormat.format(distanceMeters)} m"
        }
    }
}
