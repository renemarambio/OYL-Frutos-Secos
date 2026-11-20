package com.example.oylfrutossecos.data.local

import com.example.oylfrutossecos.data.model.AppNotification
import com.example.oylfrutossecos.data.model.NotificationTarget

/**
 * Interfaz de fuente de datos local para notificaciones.
 * Permite desacoplar el acceso a datos y mockear fácilmente en pruebas unitarias.
 */
interface NotificationLocalDataSource {
    fun getAllNotifications(): List<AppNotification>
    fun insertNotification(notification: AppNotification): Long
    fun markAsRead(id: String): Boolean
    fun markAllAsRead(isAdmin: Boolean): Int
    fun deleteNotification(id: String): Boolean
    fun deleteAllNotifications(isAdmin: Boolean): Int
    fun getUnreadCount(isAdmin: Boolean): Int
}

/**
 * Implementación de [NotificationLocalDataSource] delegada en [AppDatabaseHelper].
 */
class SqliteNotificationLocalDataSource(
    private val dbHelper: AppDatabaseHelper
) : NotificationLocalDataSource {

    override fun getAllNotifications(): List<AppNotification> {
        return dbHelper.getAllNotifications()
    }

    override fun insertNotification(notification: AppNotification): Long {
        return dbHelper.insertNotification(notification)
    }

    override fun markAsRead(id: String): Boolean {
        return dbHelper.markNotificationAsRead(id)
    }

    override fun markAllAsRead(isAdmin: Boolean): Int {
        val target = if (isAdmin) NotificationTarget.ADMIN else NotificationTarget.CLIENTE
        return dbHelper.markAllNotificationsAsRead(target)
    }

    override fun deleteNotification(id: String): Boolean {
        return dbHelper.deleteNotification(id)
    }

    override fun deleteAllNotifications(isAdmin: Boolean): Int {
        val target = if (isAdmin) NotificationTarget.ADMIN else NotificationTarget.CLIENTE
        return dbHelper.deleteAllNotifications(target)
    }

    override fun getUnreadCount(isAdmin: Boolean): Int {
        val target = if (isAdmin) NotificationTarget.ADMIN else NotificationTarget.CLIENTE
        return dbHelper.getUnreadNotificationsCount(target)
    }
}
