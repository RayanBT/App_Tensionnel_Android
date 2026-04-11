package com.example.apptensionnel.data.models

import java.util.UUID

data class Profile(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val iconColor: Int, // Color as Int
    val age: Int = 0,
    val weight: Float = 0f,
    val height: Int = 0
)
