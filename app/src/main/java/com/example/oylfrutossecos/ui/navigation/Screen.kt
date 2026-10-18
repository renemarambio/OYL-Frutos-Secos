package com.example.oylfrutossecos.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Home : Screen("home")
    object CategoryList : Screen("category_list")
    object ProductCatalog : Screen("product_catalog?category={category}") {
        fun createRoute(category: String = "Todos") = "product_catalog?category=$category"
    }
    object ProductDetail : Screen("product_detail/{productId}") {
        fun createRoute(productId: Long) = "product_detail/$productId"
    }
    object AddEditProduct : Screen("add_edit_product?productId={productId}") {
        fun createRoute(productId: Long = 0L) = "add_edit_product?productId=$productId"
    }
    object Camera : Screen("camera")
    object BarcodeScanner : Screen("barcode_scanner?pickMode={pickMode}") {
        // pickMode = true: el código leído se devuelve al formulario de producto
        fun createRoute(pickMode: Boolean = false) = "barcode_scanner?pickMode=$pickMode"
    }
    object Cart : Screen("cart")
    object Checkout : Screen("checkout")
    object DeliveryMap : Screen("delivery_map")
    object SalesHistory : Screen("sales_history")
    object Settings : Screen("settings")
    object Support : Screen("support")
}
