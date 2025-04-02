package com.example.ewaykollect

import com.google.type.LatLng

data class RecyclerItem(
    val id: String,
    val name: String,
    val location: String,
    val acceptedTypes: List<String>,
    val contact: String,
    val rating: Double,
    val coordinates: LatLng,
    val popularityScore: Int
)
