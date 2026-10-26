package com.example.oylfrutossecos.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.oylfrutossecos.data.model.AppNotification
import com.example.oylfrutossecos.data.model.NotificationTarget
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class NotificationViewModel : ViewModel() {

    private val _notifications = MutableStateFlow(initialNotifications)
    val notifications: StateFlow<List<AppNotification>> = _notifications.asStateFlow()

    fun getNotificationsForRole(isAdmin: Boolean): List<AppNotification> {
        return _notifications.value.filter {
            if (isAdmin) {
                it.target == NotificationTarget.ADMIN || it.target == NotificationTarget.ALL
            } else {
                it.target == NotificationTarget.CLIENTE || it.target == NotificationTarget.ALL
            }
        }
    }

    fun getUnreadCount(isAdmin: Boolean): Int {
        return getNotificationsForRole(isAdmin).count { !it.isRead }
    }

    fun markAsRead(id: String) {
        _notifications.update { list ->
            list.map { if (it.id == id) it.copy(isRead = true) else it }
        }
    }

    fun markAllAsRead(isAdmin: Boolean) {
        _notifications.update { list ->
            list.map { notif ->
                val belongs = if (isAdmin) {
                    notif.target == NotificationTarget.ADMIN || notif.target == NotificationTarget.ALL
                } else {
                    notif.target == NotificationTarget.CLIENTE || notif.target == NotificationTarget.ALL
                }
                if (belongs) notif.copy(isRead = true) else notif
            }
        }
    }

    companion object {
        val initialNotifications = listOf(
            // Notificaciones para ADMINISTRADOR
            AppNotification(
                id = "notif_admin_1",
                title = "Stock crítico en arándanos",
                message = "El stock de Arándanos Deshidratados ha bajado a 3 kg. Se sugiere reorden inmediata al proveedor de Coquimbo.",
                date = "Hace 15 min",
                isRead = false,
                target = NotificationTarget.ADMIN,
                type = "STOCK"
            ),
            AppNotification(
                id = "notif_admin_2",
                title = "Nuevo pedido recibido",
                message = "El cliente María González ha generado el pedido #1049 por $32.400 con despacho a Las Condes.",
                date = "Hace 45 min",
                isRead = false,
                target = NotificationTarget.ADMIN,
                type = "ORDER"
            ),
            AppNotification(
                id = "notif_admin_3",
                title = "Alerta de inventario: Castañas de Cajú",
                message = "Existencias en 8 kg (umbral crítico: 10 kg). Revisar lote en bodega central.",
                date = "Hoy, 09:30",
                isRead = false,
                target = NotificationTarget.ADMIN,
                type = "STOCK"
            ),
            AppNotification(
                id = "notif_admin_4",
                title = "Resumen diario de ventas",
                message = "Se completaron 12 despachos en el Gran Santiago con un total facturado de $210.500.",
                date = "Ayer",
                isRead = true,
                target = NotificationTarget.ADMIN,
                type = "INFO"
            ),

            // Notificaciones para CLIENTE
            AppNotification(
                id = "notif_client_1",
                title = "Tu pedido #1048 fue programado",
                message = "Tu pedido de Nueces y Almendras está programado para entrega hoy entre 14:00 y 18:00 hrs.",
                date = "Hace 20 min",
                isRead = false,
                target = NotificationTarget.CLIENTE,
                type = "ORDER"
            ),
            AppNotification(
                id = "notif_client_2",
                title = "Nueva oferta en almendras -20%",
                message = "¡Semana de frutos secos! Disfruta un 20% de descuento en Almendras Nonpareil Tostadas.",
                date = "Hoy, 10:15",
                isRead = false,
                target = NotificationTarget.CLIENTE,
                type = "PROMO"
            ),
            AppNotification(
                id = "notif_client_3",
                title = "Despacho en camino",
                message = "El repartidor OYL va en camino a tu dirección en Providencia con tu pedido #1045.",
                date = "Ayer",
                isRead = true,
                target = NotificationTarget.CLIENTE,
                type = "ORDER"
            ),
            AppNotification(
                id = "notif_client_4",
                title = "Bienvenido a OYL Frutos Secos",
                message = "Descubre nuestra selección de frutos secos, semillas y mixes saludables con despacho en todo Santiago.",
                date = "Hace 3 días",
                isRead = true,
                target = NotificationTarget.CLIENTE,
                type = "INFO"
            )
        )
    }
}
