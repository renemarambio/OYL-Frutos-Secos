package com.example.oylfrutossecos

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import com.example.oylfrutossecos.data.notification.NotificationHelper
import com.example.oylfrutossecos.ui.navigation.AppNavGraph
import com.example.oylfrutossecos.ui.navigation.Screen
import com.example.oylfrutossecos.ui.theme.OYLFrutosSecosTheme
import org.osmdroid.config.Configuration

class MainActivity : ComponentActivity() {

    private val targetScreenState = mutableStateOf<String?>(null)

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configuración requerida de osmdroid antes de instanciar cualquier MapView
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        Configuration.getInstance().load(this, prefs)
        Configuration.getInstance().userAgentValue = "OYLFrutosSecos/1.0 (app academica Duoc UC)"

        // Purgar caché de tiles en disco UNA sola vez para descartar errores previos (403) y permitir reutilización
        val cacheClearedKey = "osm_cache_cleared_v2"
        if (!prefs.getBoolean(cacheClearedKey, false)) {
            runCatching {
                val tileCache = Configuration.getInstance().osmdroidTileCache
                if (tileCache != null && tileCache.exists()) {
                    tileCache.listFiles()?.forEach { it.deleteRecursively() } ?: tileCache.deleteRecursively()
                }
            }
            prefs.edit().putBoolean(cacheClearedKey, true).apply()
        }

        // Crear canal de notificaciones y solicitar permiso en Android 13+ (POST_NOTIFICATIONS)
        NotificationHelper.getInstance(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        targetScreenState.value = intent?.getStringExtra(NotificationHelper.EXTRA_TARGET_SCREEN)

        enableEdgeToEdge()
        setContent {
            OYLFrutosSecosTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val targetScreen by targetScreenState

                    LaunchedEffect(targetScreen) {
                        targetScreen?.let { target ->
                            when (target) {
                                "MY_ORDERS" -> navController.navigate(Screen.MyOrders.route)
                                "INVENTORY" -> navController.navigate(Screen.Inventory.route)
                                "NOTIFICATIONS" -> navController.navigate(Screen.Notifications.route)
                            }
                            targetScreenState.value = null
                        }
                    }

                    AppNavGraph(navController = navController)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent.getStringExtra(NotificationHelper.EXTRA_TARGET_SCREEN)?.let {
            targetScreenState.value = it
        }
    }
}