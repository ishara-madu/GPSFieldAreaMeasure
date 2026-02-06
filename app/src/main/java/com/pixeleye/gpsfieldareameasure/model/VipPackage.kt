package com.pixeleye.gpsfieldareameasure.model

data class VipPackage(
    val id: String,
    val name: String,
    val price: String,
    val duration: String,
    val features: List<String>,
    val isBestValue: Boolean = false
)
