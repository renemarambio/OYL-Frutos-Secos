package com.example.oylfrutossecos.ui.viewmodel

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class NotificationViewModelTest {

    private lateinit var viewModel: NotificationViewModel

    @Before
    fun setUp() {
        viewModel = NotificationViewModel()
    }

    @Test
    fun getNotificationsForRole_admin_retornaNotificacionesDeAdminYAll() {
        val adminNotifs = viewModel.getNotificationsForRole(isAdmin = true)
        assertTrue(adminNotifs.isNotEmpty())
        adminNotifs.forEach {
            assertTrue(it.target == com.example.oylfrutossecos.data.model.NotificationTarget.ADMIN ||
                    it.target == com.example.oylfrutossecos.data.model.NotificationTarget.ALL)
        }
    }

    @Test
    fun getNotificationsForRole_cliente_retornaNotificacionesDeClienteYAll() {
        val clientNotifs = viewModel.getNotificationsForRole(isAdmin = false)
        assertTrue(clientNotifs.isNotEmpty())
        clientNotifs.forEach {
            assertTrue(it.target == com.example.oylfrutossecos.data.model.NotificationTarget.CLIENTE ||
                    it.target == com.example.oylfrutossecos.data.model.NotificationTarget.ALL)
        }
    }

    @Test
    fun getUnreadCount_cuentaSoloNoLeidas() {
        val unreadAdmin = viewModel.getUnreadCount(isAdmin = true)
        assertTrue(unreadAdmin > 0)
    }

    @Test
    fun markAsRead_marcaNotificacionEspecificaComoLeida() {
        val firstUnread = viewModel.notifications.value.first { !it.isRead }
        viewModel.markAsRead(firstUnread.id)

        val updated = viewModel.notifications.value.find { it.id == firstUnread.id }
        assertTrue(updated?.isRead == true)
    }

    @Test
    fun markAllAsRead_marcaTodasLasDeAdminComoLeidas() {
        viewModel.markAllAsRead(isAdmin = true)
        val unreadAdmin = viewModel.getUnreadCount(isAdmin = true)
        assertEquals(0, unreadAdmin)
    }

    @Test
    fun markAllAsRead_marcaTodasLasDeClienteComoLeidas() {
        viewModel.markAllAsRead(isAdmin = false)
        val unreadClient = viewModel.getUnreadCount(isAdmin = false)
        assertEquals(0, unreadClient)
    }
}
