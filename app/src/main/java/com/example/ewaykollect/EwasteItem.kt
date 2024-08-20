package com.example.ewaykollect

data class EwasteItem(
    val imageUrl: String,       // URL of the e-waste item's image
    val name: String,           // Name of the e-waste item
    val number: String,            // Number of the e-waste item
    val state: String,     // Description of the e-waste item
    val type: String      //Type of Ewaste
)