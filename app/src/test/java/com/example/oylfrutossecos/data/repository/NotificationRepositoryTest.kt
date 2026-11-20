package com.example.oylfrutossecos.data.repository

import com.example.oylfrutossecos.data.local.NotificationLocalDataSource
import com.example.oylfrutossecos.data.model.AppNotification
import com.example.oylfrutossecos.data.model.NotificationTarget
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class NotificationRepositoryTest {

    private lateinit var localDataSource: NotificationLocalDataSource
    private lateinit var repository: NotificationRepository

    private val sampleNotification = AppNotification(
        id = "test_1",
        title = "Test",
        message = "Test message",
        date = "Hoy",
        isRead = false,
        target = NotificationTarget.ADMIN,
        type = "STOCK"
    )

    @Before
    fun setUp() {
        localDataSource = mockk(relaxed = true)
        repository = NotificationRepository(local = localDataSource)
    }

    @Test
    fun getAllNotifications_delegaEnLocalDataSource() {
        every { localDataSource.getAllNotifications() } returns listOf(sampleNotification)

        val result = repository.getAllNotifications()

        assertEquals(1, result.size)
        assertEquals("test_1", result.first().id)
        verify { localDataSource.getAllNotifications() }
    }

    @Test
    fun insertNotification_delegaEnLocalDataSource() {
        every { localDataSource.insertNotification(sampleNotification) } returns 1L

        val id = repository.insertNotification(sampleNotification)

        assertEquals(1L, id)
        verify { localDataSource.insertNotification(sampleNotification) }
    }

    @Test
    fun markAsRead_delegaEnLocalDataSource() {
        every { localDataSource.markAsRead("test_1") } returns true

        val success = repository.markAsRead("test_1")

        assertTrue(success)
        verify { localDataSource.markAsRead("test_1") }
    }

    @Test
    fun markAllAsRead_delegaEnLocalDataSource() {
        every { localDataSource.markAllAsRead(true) } returns 3

        val count = repository.markAllAsRead(true)

        assertEquals(3, count)
        verify { localDataSource.markAllAsRead(true) }
    }

    @Test
    fun deleteNotification_delegaEnLocalDataSource() {
        every { localDataSource.deleteNotification("test_1") } returns true

        val success = repository.deleteNotification("test_1")

        assertTrue(success)
        verify { localDataSource.deleteNotification("test_1") }
    }

    @Test
    fun deleteAllNotifications_delegaEnLocalDataSource() {
        every { localDataSource.deleteAllNotifications(false) } returns 5

        val count = repository.deleteAllNotifications(false)

        assertEquals(5, count)
        verify { localDataSource.deleteAllNotifications(false) }
    }

    @Test
    fun getUnreadCount_delegaEnLocalDataSource() {
        every { localDataSource.getUnreadCount(true) } returns 2

        val count = repository.getUnreadCount(true)

        assertEquals(2, count)
        verify { localDataSource.getUnreadCount(true) }
    }
}
