package com.example.ewaykollect

data class RecyclerItem(
    val id: String,
    val name: String,
    val location: String,
    val acceptedTypes: List<String>,
    val contact: String,
    val rating: Double,
    val coordinates: Pair<Double, Double>,
    val popularityScore: Int,
    val logoUrl: String? = null
)
