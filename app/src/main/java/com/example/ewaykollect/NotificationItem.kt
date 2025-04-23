package com.example.ewaykollect

data class NotificationItem(
    val id: String = "",
    val userId: String = "",
    val type: String = "",
    val message: String = "",
    val timestamp: Long = 0,
    val read: Boolean = false
)
