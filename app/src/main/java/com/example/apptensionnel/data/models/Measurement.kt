package com.example.apptensionnel.data.models

import java.util.UUID

data class Measurement(
    val id: String = UUID.randomUUID().toString(),
    val profileId: String,
    val systolic: Int,
    val diastolic: Int,
    val pulse: Int,
    val date: Long = System.currentTimeMillis(),
    val notes: String = ""
)
