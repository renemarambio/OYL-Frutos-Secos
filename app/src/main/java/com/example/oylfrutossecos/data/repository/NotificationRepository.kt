package com.example.oylfrutossecos.data.repository

import com.example.oylfrutossecos.data.local.NotificationLocalDataSource
import com.example.oylfrutossecos.data.model.AppNotification

/**
 * Repositorio para la gestión de notificaciones persistidas en SQLite.
 * Admite inyección por constructor para pruebas unitarias con MockK.
 */
class NotificationRepository(
    val local: NotificationLocalDataSource
) {
    fun getAllNotifications(): List<AppNotification> {
        return local.getAllNotifications()
    }

    fun insertNotification(notification: AppNotification): Long {
        return local.insertNotification(notification)
    }

    fun markAsRead(id: String): Boolean {
        return local.markAsRead(id)
    }

    fun markAllAsRead(isAdmin: Boolean): Int {
        return local.markAllAsRead(isAdmin)
    }

    fun deleteNotification(id: String): Boolean {
        return local.deleteNotification(id)
    }

    fun deleteAllNotifications(isAdmin: Boolean): Int {
        return local.deleteAllNotifications(isAdmin)
    }

    fun getUnreadCount(isAdmin: Boolean): Int {
        return local.getUnreadCount(isAdmin)
    }
}
