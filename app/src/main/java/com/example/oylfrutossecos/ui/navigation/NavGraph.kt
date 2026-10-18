package com.example.oylfrutossecos.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.oylfrutossecos.ui.screens.AddEditProductScreen
import com.example.oylfrutossecos.ui.screens.BarcodeScannerScreen
import com.example.oylfrutossecos.ui.screens.CameraScreen
import com.example.oylfrutossecos.ui.screens.CartScreen
import com.example.oylfrutossecos.ui.screens.CategoryListScreen
import com.example.oylfrutossecos.ui.screens.CheckoutScreen
import com.example.oylfrutossecos.ui.screens.DeliveryMapScreen
import com.example.oylfrutossecos.ui.screens.HomeScreen
import com.example.oylfrutossecos.ui.screens.LoginScreen
import com.example.oylfrutossecos.ui.screens.ProductCatalogScreen
import com.example.oylfrutossecos.ui.screens.ProductDetailScreen
import com.example.oylfrutossecos.ui.screens.SalesHistoryScreen
import com.example.oylfrutossecos.ui.screens.SettingsScreen
import com.example.oylfrutossecos.ui.screens.SplashScreen
import com.example.oylfrutossecos.ui.screens.SupportScreen
import com.example.oylfrutossecos.ui.viewmodel.AuthViewModel
import com.example.oylfrutossecos.ui.viewmodel.CartViewModel
import com.example.oylfrutossecos.ui.viewmodel.ProductViewModel
import com.example.oylfrutossecos.ui.viewmodel.SalesViewModel

@Composable
fun AppNavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel = viewModel(),
    productViewModel: ProductViewModel = viewModel(),
    cartViewModel: CartViewModel = viewModel(),
    salesViewModel: SalesViewModel = viewModel()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        // 1. Splash Screen
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        // 2. Login Screen
        composable(Screen.Login.route) {
            LoginScreen(
                authViewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // 3. Home Screen
        composable(Screen.Home.route) {
            HomeScreen(
                authViewModel = authViewModel,
                productViewModel = productViewModel,
                cartViewModel = cartViewModel,
                salesViewModel = salesViewModel,
                onNavigateToCatalog = { navController.navigate(Screen.ProductCatalog.createRoute("Todos")) },
                onNavigateToCategories = { navController.navigate(Screen.CategoryList.route) },
                onNavigateToAddProduct = { navController.navigate(Screen.AddEditProduct.createRoute(0L)) },
                onNavigateToCamera = { navController.navigate(Screen.Camera.route) },
                onNavigateToScanner = { navController.navigate(Screen.BarcodeScanner.createRoute()) },
                onNavigateToCart = { navController.navigate(Screen.Cart.route) },
                onNavigateToDeliveryMap = { navController.navigate(Screen.DeliveryMap.route) },
                onNavigateToSalesHistory = { navController.navigate(Screen.SalesHistory.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToSupport = { navController.navigate(Screen.Support.route) }
            )
        }

        // 4. Category List Screen
        composable(Screen.CategoryList.route) {
            CategoryListScreen(
                productViewModel = productViewModel,
                onCategorySelected = { categoryName ->
                    productViewModel.onCategorySelected(categoryName)
                    navController.navigate(Screen.ProductCatalog.createRoute(categoryName))
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 5. Product Catalog Screen
        composable(
            route = Screen.ProductCatalog.route,
            arguments = listOf(
                navArgument("category") {
                    type = NavType.StringType
                    defaultValue = "Todos"
                }
            )
        ) { backStackEntry ->
            val category = backStackEntry.arguments?.getString("category") ?: "Todos"
            productViewModel.onCategorySelected(category)

            ProductCatalogScreen(
                productViewModel = productViewModel,
                cartViewModel = cartViewModel,
                onNavigateToDetail = { productId ->
                    navController.navigate(Screen.ProductDetail.createRoute(productId))
                },
                onNavigateToAddProduct = { navController.navigate(Screen.AddEditProduct.createRoute(0L)) },
                onNavigateToCart = { navController.navigate(Screen.Cart.route) },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 6. Product Detail Screen
        composable(
            route = Screen.ProductDetail.route,
            arguments = listOf(
                navArgument("productId") {
                    type = NavType.LongType
                    defaultValue = 1L
                }
            )
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getLong("productId") ?: 1L
            ProductDetailScreen(
                productId = productId,
                productViewModel = productViewModel,
                cartViewModel = cartViewModel,
                onNavigateToEdit = { id ->
                    navController.navigate(Screen.AddEditProduct.createRoute(id))
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 7. Add / Edit Product Screen
        composable(
            route = Screen.AddEditProduct.route,
            arguments = listOf(
                navArgument("productId") {
                    type = NavType.LongType
                    defaultValue = 0L
                }
            )
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getLong("productId") ?: 0L
            AddEditProductScreen(
                productId = productId,
                productViewModel = productViewModel,
                onNavigateToCamera = { navController.navigate(Screen.Camera.route) },
                onNavigateToScanner = { navController.navigate(Screen.BarcodeScanner.createRoute(pickMode = true)) },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 8. Camera Screen
        composable(Screen.Camera.route) {
            CameraScreen(
                productViewModel = productViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 9. Barcode Scanner Screen
        composable(
            route = Screen.BarcodeScanner.route,
            arguments = listOf(
                navArgument("pickMode") {
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) { backStackEntry ->
            val pickMode = backStackEntry.arguments?.getBoolean("pickMode") ?: false
            BarcodeScannerScreen(
                productViewModel = productViewModel,
                pickMode = pickMode,
                onNavigateToProductDetail = { prodId ->
                    navController.navigate(Screen.ProductDetail.createRoute(prodId))
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 10. Cart Screen
        composable(Screen.Cart.route) {
            CartScreen(
                cartViewModel = cartViewModel,
                onNavigateToCheckout = { navController.navigate(Screen.Checkout.route) },
                onNavigateToCatalog = { navController.navigate(Screen.ProductCatalog.createRoute("Todos")) },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 11. Checkout Screen
        composable(Screen.Checkout.route) {
            CheckoutScreen(
                cartViewModel = cartViewModel,
                salesViewModel = salesViewModel,
                onNavigateToSalesHistory = {
                    navController.navigate(Screen.SalesHistory.route) {
                        popUpTo(Screen.Home.route)
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 12. Delivery Map Screen
        composable(Screen.DeliveryMap.route) {
            DeliveryMapScreen(
                cartViewModel = cartViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 13. Sales History Screen
        composable(Screen.SalesHistory.route) {
            SalesHistoryScreen(
                salesViewModel = salesViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 14. Settings Screen
        composable(Screen.Settings.route) {
            SettingsScreen(
                authViewModel = authViewModel,
                productViewModel = productViewModel,
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 15. Support Screen
        composable(Screen.Support.route) {
            SupportScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
