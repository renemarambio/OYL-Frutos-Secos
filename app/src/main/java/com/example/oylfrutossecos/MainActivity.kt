package com.example.oylfrutossecos

import android.os.Bundle
import android.preference.PreferenceManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.oylfrutossecos.ui.navigation.AppNavGraph
import com.example.oylfrutossecos.ui.theme.OYLFrutosSecosTheme
import org.osmdroid.config.Configuration

class MainActivity : ComponentActivity() {
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

        enableEdgeToEdge()
        setContent {
            OYLFrutosSecosTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    AppNavGraph(navController = navController)
                }
            }
        }
    }
}