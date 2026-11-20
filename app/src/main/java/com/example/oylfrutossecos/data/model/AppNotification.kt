package com.example.oylfrutossecos.data.model

import java.util.UUID

enum class NotificationTarget {
    CLIENTE,
    ADMIN,
    ALL
}

data class AppNotification(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val message: String,
    val date: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val target: NotificationTarget = NotificationTarget.ALL,
    val type: String = "INFO", // "ORDER", "PROMO", "STOCK", "INFO"
    val targetScreen: String? = null // "MY_ORDERS", "INVENTORY"
)
