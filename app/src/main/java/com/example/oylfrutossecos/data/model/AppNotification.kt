package com.example.oylfrutossecos.data.model

enum class NotificationTarget {
    CLIENTE,
    ADMIN,
    ALL
}

data class AppNotification(
    val id: String,
    val title: String,
    val message: String,
    val date: String,
    val isRead: Boolean = false,
    val target: NotificationTarget = NotificationTarget.ALL,
    val type: String = "INFO" // "ORDER", "PROMO", "STOCK", "INFO"
)
