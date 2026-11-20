package com.example.oylfrutossecos.ui.viewmodel

import com.example.oylfrutossecos.data.model.AppNotification
import com.example.oylfrutossecos.data.model.NotificationTarget
import com.example.oylfrutossecos.data.model.Product
import com.example.oylfrutossecos.data.model.Sale
import com.example.oylfrutossecos.data.notification.NotificationSender
import com.example.oylfrutossecos.data.repository.NotificationRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class NotificationViewModelTest {

    private lateinit var repository: NotificationRepository
    private lateinit var notificationSender: NotificationSender
    private lateinit var viewModel: NotificationViewModel

    private val mockNotificationList = mutableListOf(
        AppNotification(
            id = "notif_admin_1",
            title = "Stock crítico en arándanos",
            message = "El stock de Arándanos ha bajado a 3 kg.",
            date = "Hace 15 min",
            isRead = false,
            target = NotificationTarget.ADMIN,
            type = "STOCK",
            targetScreen = "INVENTORY"
        ),
        AppNotification(
            id = "notif_client_1",
            title = "Tu pedido #1048 fue programado",
            message = "Tu pedido de Nueces y Almendras está programado.",
            date = "Hace 20 min",
            isRead = false,
            target = NotificationTarget.CLIENTE,
            type = "ORDER",
            targetScreen = "MY_ORDERS"
        ),
        AppNotification(
            id = "notif_all_1",
            title = "Bienvenido a OYL Frutos Secos",
            message = "Descubre nuestra selección de frutos secos.",
            date = "Hace 3 días",
            isRead = true,
            target = NotificationTarget.ALL,
            type = "INFO"
        )
    )

    @Before
    fun setUp() {
        repository = mockk(relaxed = true)
        notificationSender = mockk(relaxed = true)

        every { repository.getAllNotifications() } returns mockNotificationList.toList()

        viewModel = NotificationViewModel(
            repository = repository,
            notificationSender = notificationSender
        )
    }

    @Test
    fun getNotificationsForRole_admin_retornaNotificacionesDeAdminYAll() {
        val adminNotifs = viewModel.getNotificationsForRole(isAdmin = true)
        assertTrue(adminNotifs.isNotEmpty())
        adminNotifs.forEach {
            assertTrue(it.target == NotificationTarget.ADMIN || it.target == NotificationTarget.ALL)
        }
    }

    @Test
    fun getNotificationsForRole_cliente_retornaNotificacionesDeClienteYAll() {
        val clientNotifs = viewModel.getNotificationsForRole(isAdmin = false)
        assertTrue(clientNotifs.isNotEmpty())
        clientNotifs.forEach {
            assertTrue(it.target == NotificationTarget.CLIENTE || it.target == NotificationTarget.ALL)
        }
    }

    @Test
    fun getUnreadCount_cuentaSoloNoLeidasParaCadaRol() {
        // En mockNotificationList:
        // Admin: notif_admin_1 (no leída) + notif_all_1 (leída) -> total unread = 1
        // Cliente: notif_client_1 (no leída) + notif_all_1 (leída) -> total unread = 1
        val unreadAdmin = viewModel.getUnreadCount(isAdmin = true)
        assertEquals(1, unreadAdmin)

        val unreadClient = viewModel.getUnreadCount(isAdmin = false)
        assertEquals(1, unreadClient)
    }

    @Test
    fun markAsRead_marcaNotificacionEspecificaComoLeida_yPersisteEnRepositorio() {
        viewModel.markAsRead("notif_admin_1")

        verify { repository.markAsRead("notif_admin_1") }
        val updated = viewModel.notifications.value.find { it.id == "notif_admin_1" }
        assertTrue(updated?.isRead == true)
        assertEquals(0, viewModel.getUnreadCount(isAdmin = true))
    }

    @Test
    fun markAllAsRead_marcaTodasLasDeAdminComoLeidas() {
        viewModel.markAllAsRead(isAdmin = true)

        verify { repository.markAllAsRead(isAdmin = true) }
        val unreadAdmin = viewModel.getUnreadCount(isAdmin = true)
        assertEquals(0, unreadAdmin)
        // La del cliente no leída debe mantenerse intacta
        val unreadClient = viewModel.getUnreadCount(isAdmin = false)
        assertEquals(1, unreadClient)
    }

    @Test
    fun markAllAsRead_marcaTodasLasDeClienteComoLeidas() {
        viewModel.markAllAsRead(isAdmin = false)

        verify { repository.markAllAsRead(isAdmin = false) }
        val unreadClient = viewModel.getUnreadCount(isAdmin = false)
        assertEquals(0, unreadClient)
        // La de admin no leída debe mantenerse intacta
        val unreadAdmin = viewModel.getUnreadCount(isAdmin = true)
        assertEquals(1, unreadAdmin)
    }

    @Test
    fun deleteNotification_eliminaNotificacionDeListaYPersiste() {
        viewModel.deleteNotification("notif_admin_1")

        verify { repository.deleteNotification("notif_admin_1") }
        val exists = viewModel.notifications.value.any { it.id == "notif_admin_1" }
        assertFalse(exists)
    }

    @Test
    fun deleteAllNotifications_eliminaNotificacionesDeRolYPersiste() {
        viewModel.deleteAllNotifications(isAdmin = false)

        verify { repository.deleteAllNotifications(isAdmin = false) }
        val clientNotifs = viewModel.getNotificationsForRole(isAdmin = false)
        assertTrue(clientNotifs.isEmpty())
    }

    @Test
    fun notifyOrderPlaced_generaNotificacionYEnviaNotificacionDeSistema() {
        val notif = viewModel.notifyOrderPlaced("ORD-9999", 25000.0)

        assertEquals("Pedido ORD-9999 recibido", notif.title)
        assertTrue(notif.message.contains("25.000"))
        assertEquals(NotificationTarget.CLIENTE, notif.target)
        assertEquals("MY_ORDERS", notif.targetScreen)
        assertEquals("ORDER", notif.type)

        verify { repository.insertNotification(any()) }
        verify {
            notificationSender.sendNotification(
                id = any(),
                title = "Pedido ORD-9999 recibido",
                message = any(),
                targetScreen = "MY_ORDERS"
            )
        }
    }

    @Test
    fun detectOrderStatusChanges_detectaCambioDeEstadoYEmiteNotificacion() {
        // Primera pasada: registra estado conocido sin notificar
        val initialOrder = Sale(
            id = 10L,
            orderNumber = "ORD-2024-001",
            dateFormatted = "20/11/2026",
            customerName = "Juan Pérez",
            customerEmail = "juan@example.com",
            customerAddress = "Providencia 123",
            deliveryZone = "Providencia",
            paymentMethod = "Tarjeta",
            subtotal = 10000.0,
            deliveryFee = 2000.0,
            total = 12000.0,
            status = "PENDIENTE",
            items = emptyList()
        )
        val firstCheck = viewModel.detectOrderStatusChanges(listOf(initialOrder))
        assertTrue(firstCheck.isEmpty())

        // Segunda pasada con el mismo estado: no debe notificar
        val secondCheck = viewModel.detectOrderStatusChanges(listOf(initialOrder))
        assertTrue(secondCheck.isEmpty())

        // Tercera pasada con estado cambiado a EN_CAMINO: debe emitir notificación
        val updatedOrder = initialOrder.copy(status = "EN_CAMINO")
        val thirdCheck = viewModel.detectOrderStatusChanges(listOf(updatedOrder))

        assertEquals(1, thirdCheck.size)
        val notif = thirdCheck.first()
        assertEquals("Actualización de pedido", notif.title)
        assertEquals("Tu pedido ORD-2024-001 está EN_CAMINO", notif.message)
        assertEquals("MY_ORDERS", notif.targetScreen)
        assertEquals(NotificationTarget.CLIENTE, notif.target)

        verify {
            notificationSender.sendNotification(
                id = any(),
                title = "Actualización de pedido",
                message = "Tu pedido ORD-2024-001 está EN_CAMINO",
                targetScreen = "MY_ORDERS"
            )
        }
    }

    @Test
    fun checkLowStockProducts_conProductosBajoStock_emiteAlertaDeStockCritico() {
        val products = listOf(
            Product(id = 1, name = "Nueces", category = "Frutos Secos", price = 10000.0, stock = 3), // Bajo stock (<=10)
            Product(id = 2, name = "Almendras", category = "Frutos Secos", price = 8000.0, stock = 5), // Bajo stock
            Product(id = 3, name = "Cajú", category = "Frutos Secos", price = 12000.0, stock = 2), // Bajo stock
            Product(id = 4, name = "Maní", category = "Frutos Secos", price = 4000.0, stock = 50)  // Stock OK
        )

        val result = viewModel.checkLowStockProducts(products)

        assertTrue(result)
        verify {
            notificationSender.sendNotification(
                id = any(),
                title = "Alerta de inventario",
                message = "3 productos con stock crítico",
                targetScreen = "INVENTORY"
            )
        }
        verify { repository.insertNotification(any()) }

        // En la misma sesión no debe re-notificar (evita spam)
        val secondResult = viewModel.checkLowStockProducts(products)
        assertFalse(secondResult)
    }

    @Test
    fun checkLowStockProducts_sinProductosBajoStock_noEmiteNotificacion() {
        val products = listOf(
            Product(id = 1, name = "Nueces", category = "Frutos Secos", price = 10000.0, stock = 20),
            Product(id = 2, name = "Almendras", category = "Frutos Secos", price = 8000.0, stock = 30)
        )

        val result = viewModel.checkLowStockProducts(products)

        assertFalse(result)
        verify(exactly = 0) {
            notificationSender.sendNotification(any(), any(), any(), any())
        }
    }

    @Test
    fun notifyOrderStatusUpdatedByAdmin_emiteConfirmacionDeAdmin() {
        val notif = viewModel.notifyOrderStatusUpdatedByAdmin("ORD-777", "Entregado")

        assertEquals("Estado de pedido actualizado", notif.title)
        assertEquals("El pedido ORD-777 ahora está en estado Entregado", notif.message)
        assertEquals(NotificationTarget.ADMIN, notif.target)
        assertEquals("MY_ORDERS", notif.targetScreen)

        verify {
            notificationSender.sendNotification(
                id = any(),
                title = "Estado de pedido actualizado",
                message = "El pedido ORD-777 ahora está en estado Entregado",
                targetScreen = "MY_ORDERS"
            )
        }
    }
}
