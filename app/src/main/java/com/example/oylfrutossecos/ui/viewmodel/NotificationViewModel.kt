package com.example.oylfrutossecos.ui.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.oylfrutossecos.data.ServiceLocator
import com.example.oylfrutossecos.data.model.AppNotification
import com.example.oylfrutossecos.data.model.NotificationTarget
import com.example.oylfrutossecos.data.model.Product
import com.example.oylfrutossecos.data.model.Sale
import com.example.oylfrutossecos.data.notification.NotificationHelper
import com.example.oylfrutossecos.data.notification.NotificationSender
import com.example.oylfrutossecos.data.repository.NotificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class NotificationViewModel(
    private val repository: NotificationRepository? = null,
    private val notificationSender: NotificationSender? = null
) : ViewModel() {

    private val _notifications = MutableStateFlow<List<AppNotification>>(emptyList())
    val notifications: StateFlow<List<AppNotification>> = _notifications.asStateFlow()

    // Memoria de estados de pedidos para detectar transiciones
    val knownOrderStatuses = mutableMapOf<String, String>()

    // Evitar spam de alertas de stock en la misma sesión
    var hasNotifiedLowStockThisSession = false

    init {
        loadNotifications()
    }

    fun loadNotifications() {
        val loaded = repository?.getAllNotifications()
        if (!loaded.isNullOrEmpty()) {
            _notifications.value = loaded
        } else {
            _notifications.value = initialNotifications
        }
    }

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
        repository?.markAsRead(id)
        _notifications.update { list ->
            list.map { if (it.id == id) it.copy(isRead = true) else it }
        }
    }

    fun markAllAsRead(isAdmin: Boolean) {
        repository?.markAllAsRead(isAdmin)
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

    fun deleteNotification(id: String) {
        repository?.deleteNotification(id)
        _notifications.update { list ->
            list.filterNot { it.id == id }
        }
    }

    fun deleteAllNotifications(isAdmin: Boolean) {
        repository?.deleteAllNotifications(isAdmin)
        _notifications.update { list ->
            list.filterNot { notif ->
                if (isAdmin) {
                    notif.target == NotificationTarget.ADMIN || notif.target == NotificationTarget.ALL
                } else {
                    notif.target == NotificationTarget.CLIENTE || notif.target == NotificationTarget.ALL
                }
            }
        }
    }

    /**
     * Guarda la notificación en SQLite, actualiza StateFlow y envía notificación del sistema Android.
     */
    fun sendAndSaveNotification(
        title: String,
        message: String,
        target: NotificationTarget,
        type: String,
        targetScreen: String? = null
    ): AppNotification {
        val now = Date()
        val sdf = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
        val dateFormatted = "Hoy, " + sdf.format(now).split(" ").getOrElse(1) { "00:00" }

        val notification = AppNotification(
            id = "notif_" + UUID.randomUUID().toString().take(8),
            title = title,
            message = message,
            date = dateFormatted,
            timestamp = System.currentTimeMillis(),
            isRead = false,
            target = target,
            type = type,
            targetScreen = targetScreen
        )

        repository?.insertNotification(notification)
        _notifications.update { listOf(notification) + it }

        // Notificación de sistema Android (NotificationCompat)
        notificationSender?.sendNotification(
            id = (System.currentTimeMillis() % Int.MAX_VALUE).toInt(),
            title = title,
            message = message,
            targetScreen = targetScreen
        )

        return notification
    }

    /**
     * Disparador 1 (Cliente): al confirmar una compra → "Pedido ORD-XXXX recibido".
     */
    fun notifyOrderPlaced(orderNumber: String, total: Double): AppNotification {
        val formattedTotal = "$" + total.toInt().toString().reversed().chunked(3).joinToString(".").reversed()
        val title = "Pedido $orderNumber recibido"
        val message = "Hemos recibido tu compra por $formattedTotal. Estamos preparando tu pedido."
        return sendAndSaveNotification(
            title = title,
            message = message,
            target = NotificationTarget.CLIENTE,
            type = "ORDER",
            targetScreen = "MY_ORDERS"
        )
    }

    /**
     * Disparador 2 (Cliente): al abrir o refrescar "Mis pedidos", si un pedido cambió de estado respecto a lo guardado localmente.
     * Ejemplo: "Tu pedido ORD-XXXX está EN_CAMINO".
     */
    fun detectOrderStatusChanges(orders: List<Sale>): List<AppNotification> {
        val newNotifications = mutableListOf<AppNotification>()
        for (order in orders) {
            val prevStatus = knownOrderStatuses[order.orderNumber]
            if (prevStatus != null && prevStatus != order.status) {
                val title = "Actualización de pedido"
                val message = "Tu pedido ${order.orderNumber} está ${order.status}"
                val notif = sendAndSaveNotification(
                    title = title,
                    message = message,
                    target = NotificationTarget.CLIENTE,
                    type = "ORDER",
                    targetScreen = "MY_ORDERS"
                )
                newNotifications.add(notif)
            }
            knownOrderStatuses[order.orderNumber] = order.status
        }
        return newNotifications
    }

    /**
     * Disparador 3 (Admin): al entrar al panel, si hay productos con stock bajo → "X productos con stock crítico".
     */
    fun checkLowStockProducts(products: List<Product>): Boolean {
        val lowStockCount = products.count { it.isLowStock }
        if (lowStockCount > 0 && !hasNotifiedLowStockThisSession) {
            hasNotifiedLowStockThisSession = true
            val title = "Alerta de inventario"
            val message = "$lowStockCount producto${if (lowStockCount > 1) "s" else ""} con stock crítico"
            sendAndSaveNotification(
                title = title,
                message = message,
                target = NotificationTarget.ADMIN,
                type = "STOCK",
                targetScreen = "INVENTORY"
            )
            return true
        }
        return false
    }

    /**
     * Disparador 4 (Admin): al cambiar el estado de un pedido → confirmación.
     */
    fun notifyOrderStatusUpdatedByAdmin(orderNumber: String, newStatus: String): AppNotification {
        val title = "Estado de pedido actualizado"
        val message = "El pedido $orderNumber ahora está en estado $newStatus"
        return sendAndSaveNotification(
            title = title,
            message = message,
            target = NotificationTarget.ADMIN,
            type = "ORDER",
            targetScreen = "MY_ORDERS"
        )
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Application
                val repo = ServiceLocator.provideNotificationRepository(application)
                val helper = NotificationHelper.getInstance(application)
                NotificationViewModel(
                    repository = repo,
                    notificationSender = helper
                )
            }
        }

        val initialNotifications = listOf(
            // Notificaciones para ADMINISTRADOR
            AppNotification(
                id = "notif_admin_1",
                title = "Stock crítico en arándanos",
                message = "El stock de Arándanos Deshidratados ha bajado a 3 kg. Se sugiere reorden inmediata al proveedor de Coquimbo.",
                date = "Hace 15 min",
                isRead = false,
                target = NotificationTarget.ADMIN,
                type = "STOCK",
                targetScreen = "INVENTORY"
            ),
            AppNotification(
                id = "notif_admin_2",
                title = "Nuevo pedido recibido",
                message = "El cliente María González ha generado el pedido #1049 por $32.400 con despacho a Las Condes.",
                date = "Hace 45 min",
                isRead = false,
                target = NotificationTarget.ADMIN,
                type = "ORDER",
                targetScreen = "MY_ORDERS"
            ),
            AppNotification(
                id = "notif_admin_3",
                title = "Alerta de inventario: Castañas de Cajú",
                message = "Existencias en 8 kg (umbral crítico: 10 kg). Revisar lote en bodega central.",
                date = "Hoy, 09:30",
                isRead = false,
                target = NotificationTarget.ADMIN,
                type = "STOCK",
                targetScreen = "INVENTORY"
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
                type = "ORDER",
                targetScreen = "MY_ORDERS"
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
                type = "ORDER",
                targetScreen = "MY_ORDERS"
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
