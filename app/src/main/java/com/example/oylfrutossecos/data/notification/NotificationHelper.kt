package com.example.oylfrutossecos.data.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.oylfrutossecos.MainActivity
import com.example.oylfrutossecos.R

interface NotificationSender {
    fun sendNotification(
        id: Int,
        title: String,
        message: String,
        targetScreen: String? = null
    )
}

/**
 * Gestor de notificaciones de sistema de Android (NotificationCompat).
 * Configura canales de notificación en Android 8.0+ y respeta el permiso POST_NOTIFICATIONS en Android 13+.
 */
class NotificationHelper(
    private val context: Context
) : NotificationSender {

    companion object {
        const val CHANNEL_ID = "oyl_frutos_secos_channel"
        const val CHANNEL_NAME = "Notificaciones OYL Frutos Secos"
        const val CHANNEL_DESCRIPTION = "Avisos de pedidos, estado de despachos y alertas de inventario crítico"
        const val EXTRA_TARGET_SCREEN = "TARGET_SCREEN"

        @Volatile
        private var INSTANCE: NotificationHelper? = null

        fun getInstance(context: Context): NotificationHelper {
            return INSTANCE ?: synchronized(this) {
                val instance = NotificationHelper(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }

    init {
        createNotificationChannel()
    }

    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun sendNotification(
        id: Int,
        title: String,
        message: String,
        targetScreen: String?
    ) {
        // En Android 13+ (TIRAMISU) verificar permiso POST_NOTIFICATIONS
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionStatus = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            )
            if (permissionStatus != PackageManager.PERMISSION_GRANTED) {
                return
            }
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            if (!targetScreen.isNullOrBlank()) {
                putExtra(EXTRA_TARGET_SCREEN, targetScreen)
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(id, notification)
        } catch (_: SecurityException) {
            // Permiso denegado por el usuario
        }
    }
}
