package com.example.ewaykollect

import java.io.Serializable

data class RecyclerItem(
    val id: String,
    val name: String,
    val location: String,
    val acceptedTypes: List<String>,
    val contact: String,
    val rating: Double,
    val coordinates: Coordinates,
    val popularityScore: Int,
    val logoUrl: String? = null
) : Serializable

data class Coordinates(
    val longitude: Double,
    val latitude: Double
) : Serializable