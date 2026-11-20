package com.example.oylfrutossecos.data

import android.content.Context
import com.example.oylfrutossecos.data.local.AppDatabaseHelper
import com.example.oylfrutossecos.data.local.ProductLocalDataSource
import com.example.oylfrutossecos.data.local.SqliteProductLocalDataSource
import com.example.oylfrutossecos.data.remote.RetrofitClient
import com.example.oylfrutossecos.data.repository.IndicatorRepository
import com.example.oylfrutossecos.data.repository.ProductRepository
import com.example.oylfrutossecos.data.repository.SalesRepository

/**
 * Localizador de servicios para inyección de dependencias en repositorios y ViewModels.
 */
object ServiceLocator {

    @Volatile
    private var productLocalDataSource: ProductLocalDataSource? = null

    @Volatile
    private var productRepository: ProductRepository? = null

    @Volatile
    private var salesRepository: SalesRepository? = null

    @Volatile
    private var indicatorRepository: IndicatorRepository? = null

    fun provideProductLocalDataSource(context: Context): ProductLocalDataSource {
        return productLocalDataSource ?: synchronized(this) {
            val dbHelper = AppDatabaseHelper.getInstance(context)
            val ds = SqliteProductLocalDataSource(dbHelper)
            productLocalDataSource = ds
            ds
        }
    }

    fun provideProductRepository(context: Context): ProductRepository {
        return productRepository ?: synchronized(this) {
            val repo = ProductRepository(
                api = RetrofitClient.productApiService,
                local = provideProductLocalDataSource(context)
            )
            productRepository = repo
            repo
        }
    }

    fun provideSalesRepository(context: Context? = null): SalesRepository {
        return salesRepository ?: synchronized(this) {
            val repo = SalesRepository(
                api = RetrofitClient.orderApiService
            )
            salesRepository = repo
            repo
        }
    }

    fun provideIndicatorRepository(): IndicatorRepository {
        return indicatorRepository ?: synchronized(this) {
            val repo = IndicatorRepository(
                api = RetrofitClient.indicatorApiService
            )
            indicatorRepository = repo
            repo
        }
    }

    @Volatile
    private var notificationLocalDataSource: com.example.oylfrutossecos.data.local.NotificationLocalDataSource? = null

    @Volatile
    private var notificationRepository: com.example.oylfrutossecos.data.repository.NotificationRepository? = null

    fun provideNotificationLocalDataSource(context: Context): com.example.oylfrutossecos.data.local.NotificationLocalDataSource {
        return notificationLocalDataSource ?: synchronized(this) {
            val dbHelper = AppDatabaseHelper.getInstance(context)
            val ds = com.example.oylfrutossecos.data.local.SqliteNotificationLocalDataSource(dbHelper)
            notificationLocalDataSource = ds
            ds
        }
    }

    fun provideNotificationRepository(context: Context): com.example.oylfrutossecos.data.repository.NotificationRepository {
        return notificationRepository ?: synchronized(this) {
            val repo = com.example.oylfrutossecos.data.repository.NotificationRepository(
                local = provideNotificationLocalDataSource(context)
            )
            notificationRepository = repo
            repo
        }
    }

    fun reset() {
        productLocalDataSource = null
        productRepository = null
        salesRepository = null
        indicatorRepository = null
        notificationLocalDataSource = null
        notificationRepository = null
    }
}
