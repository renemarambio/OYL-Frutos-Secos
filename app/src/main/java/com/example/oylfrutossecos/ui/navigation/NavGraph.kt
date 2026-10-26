package com.example.oylfrutossecos.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.oylfrutossecos.data.model.UserRole
import com.example.oylfrutossecos.ui.screens.AddEditProductScreen
import com.example.oylfrutossecos.ui.screens.BarcodeScannerScreen
import com.example.oylfrutossecos.ui.screens.CameraScreen
import com.example.oylfrutossecos.ui.screens.CartScreen
import com.example.oylfrutossecos.ui.screens.CategoryListScreen
import com.example.oylfrutossecos.ui.screens.CheckoutScreen
import com.example.oylfrutossecos.ui.screens.DeliveryMapScreen
import com.example.oylfrutossecos.ui.screens.ForgotPasswordScreen
import com.example.oylfrutossecos.ui.screens.HomeScreen
import com.example.oylfrutossecos.ui.screens.InventoryScreen
import com.example.oylfrutossecos.ui.screens.LocalesMapScreen
import com.example.oylfrutossecos.ui.screens.LoginScreen
import com.example.oylfrutossecos.ui.screens.MyOrdersScreen
import com.example.oylfrutossecos.ui.screens.NotificationsScreen
import com.example.oylfrutossecos.ui.screens.ProductCatalogScreen
import com.example.oylfrutossecos.ui.screens.ProductDetailScreen
import com.example.oylfrutossecos.ui.screens.ProfileScreen
import com.example.oylfrutossecos.ui.screens.RegisterScreen
import com.example.oylfrutossecos.ui.screens.SalesHistoryScreen
import com.example.oylfrutossecos.ui.screens.SettingsScreen
import com.example.oylfrutossecos.ui.screens.SplashScreen
import com.example.oylfrutossecos.ui.screens.SupportScreen
import com.example.oylfrutossecos.ui.screens.WelcomeScreen
import com.example.oylfrutossecos.ui.viewmodel.AuthViewModel
import com.example.oylfrutossecos.ui.viewmodel.CartViewModel
import com.example.oylfrutossecos.ui.viewmodel.ForgotPasswordViewModel
import com.example.oylfrutossecos.ui.viewmodel.NotificationViewModel
import com.example.oylfrutossecos.ui.viewmodel.ProductViewModel
import com.example.oylfrutossecos.ui.viewmodel.SalesViewModel

private const val TRANSITION_MS = 350

@Composable
fun AppNavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel = viewModel(),
    productViewModel: ProductViewModel = viewModel(),
    cartViewModel: CartViewModel = viewModel(),
    salesViewModel: SalesViewModel = viewModel(),
    notificationViewModel: NotificationViewModel = viewModel()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        enterTransition = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(TRANSITION_MS)) +
                    fadeIn(tween(TRANSITION_MS))
        },
        exitTransition = {
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(TRANSITION_MS)) { it / 4 } +
                    fadeOut(tween(TRANSITION_MS))
        },
        popEnterTransition = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(TRANSITION_MS)) { it / 4 } +
                    fadeIn(tween(TRANSITION_MS))
        },
        popExitTransition = {
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(TRANSITION_MS)) +
                    fadeOut(tween(TRANSITION_MS))
        }
    ) {
        // 1. Splash Screen -> Redirige a Welcome Screen
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        // 2. Welcome Screen (Primera pantalla interactiva de la app)
        composable(Screen.Welcome.route) {
            WelcomeScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route)
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }

        // 3. Register Screen (Registro para perfil CLIENTE)
        composable(Screen.Register.route) {
            RegisterScreen(
                authViewModel = authViewModel,
                onRegisterSuccess = {
                    navController.navigate(Screen.ProductCatalog.createRoute("Todos")) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 4. Login Screen (Redirección según rol: ADMIN -> Home, CLIENTE -> Catálogo)
        composable(Screen.Login.route) {
            LoginScreen(
                authViewModel = authViewModel,
                onLoginSuccess = { role ->
                    if (role == UserRole.ADMIN) {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Welcome.route) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Screen.ProductCatalog.createRoute("Todos")) {
                            popUpTo(Screen.Welcome.route) { inclusive = true }
                        }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onNavigateToForgotPassword = {
                    navController.navigate(Screen.ForgotPassword.route)
                }
            )
        }

        // 4.1 Forgot Password Screen (Flujo de recuperación de contraseña de 4 pasos)
        composable(Screen.ForgotPassword.route) {
            val forgotPasswordViewModel: ForgotPasswordViewModel = viewModel()
            ForgotPasswordScreen(
                viewModel = forgotPasswordViewModel,
                onNavigateBack = { navController.popBackStack() },
                onPasswordResetSuccess = { resetEmail ->
                    authViewModel.onEmailChanged(resetEmail)
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // 5. Home Screen (Dashboard de Administración exclusivo para ADMIN)
        composable(Screen.Home.route) {
            val currentUser by authViewModel.currentUser.collectAsState()
            if (currentUser?.role == UserRole.CLIENTE) {
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.ProductCatalog.createRoute("Todos")) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            }

            HomeScreen(
                authViewModel = authViewModel,
                productViewModel = productViewModel,
                salesViewModel = salesViewModel,
                notificationViewModel = notificationViewModel,
                onNavigateToCatalog = { navController.navigate(Screen.ProductCatalog.createRoute("Todos")) },
                onNavigateToCategories = { navController.navigate(Screen.CategoryList.route) },
                onNavigateToAddProduct = { navController.navigate(Screen.AddEditProduct.createRoute(0L)) },
                onNavigateToInventory = { navController.navigate(Screen.Inventory.route) },
                onNavigateToScanner = { navController.navigate(Screen.BarcodeScanner.createRoute()) },
                onNavigateToDeliveryMap = { navController.navigate(Screen.DeliveryMap.route) },
                onNavigateToLocalesMap = { navController.navigate(Screen.LocalesMap.route) },
                onNavigateToNotifications = { navController.navigate(Screen.Notifications.route) },
                onNavigateToSalesHistory = { navController.navigate(Screen.SalesHistory.route) },
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToSupport = { navController.navigate(Screen.Support.route) },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // 6. Category List Screen
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

        // 7. Product Catalog Screen (Tienda / Catálogo)
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
            LaunchedEffect(category) { productViewModel.onCategorySelected(category) }

            ProductCatalogScreen(
                authViewModel = authViewModel,
                productViewModel = productViewModel,
                cartViewModel = cartViewModel,
                notificationViewModel = notificationViewModel,
                onNavigateToDetail = { productId ->
                    navController.navigate(Screen.ProductDetail.createRoute(productId))
                },
                onNavigateToAddProduct = { navController.navigate(Screen.AddEditProduct.createRoute(0L)) },
                onNavigateToCart = { navController.navigate(Screen.Cart.route) },
                onNavigateToMyOrders = { navController.navigate(Screen.MyOrders.route) },
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                onNavigateToScanner = { navController.navigate(Screen.BarcodeScanner.createRoute()) },
                onNavigateToLocalesMap = { navController.navigate(Screen.LocalesMap.route) },
                onNavigateToNotifications = { navController.navigate(Screen.Notifications.route) },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 8. Product Detail Screen
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
                authViewModel = authViewModel,
                productViewModel = productViewModel,
                cartViewModel = cartViewModel,
                onNavigateToEdit = { id ->
                    navController.navigate(Screen.AddEditProduct.createRoute(id))
                },
                onNavigateToCart = {
                    navController.navigate(Screen.Cart.route)
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 9. Add / Edit Product Screen (Solo ADMIN)
        composable(
            route = Screen.AddEditProduct.route,
            arguments = listOf(
                navArgument("productId") {
                    type = NavType.LongType
                    defaultValue = 0L
                }
            )
        ) { backStackEntry ->
            val currentUser by authViewModel.currentUser.collectAsState()
            if (currentUser?.role == UserRole.CLIENTE) {
                LaunchedEffect(Unit) {
                    navController.popBackStack()
                }
            }
            val productId = backStackEntry.arguments?.getLong("productId") ?: 0L
            AddEditProductScreen(
                productId = productId,
                productViewModel = productViewModel,
                onNavigateToCamera = { navController.navigate(Screen.Camera.route) },
                onNavigateToScanner = { navController.navigate(Screen.BarcodeScanner.createRoute(pickMode = true)) },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 10. Camera Screen (Solo ADMIN)
        composable(Screen.Camera.route) {
            val currentUser by authViewModel.currentUser.collectAsState()
            if (currentUser?.role == UserRole.CLIENTE) {
                LaunchedEffect(Unit) {
                    navController.popBackStack()
                }
            }
            CameraScreen(
                productViewModel = productViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 11. Barcode Scanner Screen
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
                authViewModel = authViewModel,
                pickMode = pickMode,
                onNavigateToProductDetail = { prodId ->
                    navController.navigate(Screen.ProductDetail.createRoute(prodId))
                },
                onNavigateToEditProduct = { prodId ->
                    navController.navigate(Screen.AddEditProduct.createRoute(prodId))
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 12. Cart Screen (Solo CLIENTE; ADMIN no puede comprar ni pagar)
        composable(Screen.Cart.route) {
            val currentUser by authViewModel.currentUser.collectAsState()
            if (currentUser?.role == UserRole.ADMIN) {
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Cart.route) { inclusive = true }
                    }
                }
            }

            CartScreen(
                cartViewModel = cartViewModel,
                onNavigateToCheckout = { navController.navigate(Screen.Checkout.route) },
                onNavigateToCatalog = { navController.navigate(Screen.ProductCatalog.createRoute("Todos")) },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 13. Checkout Screen (Confirmación de Pedido con Mapa; Solo CLIENTE)
        composable(Screen.Checkout.route) {
            val currentUser by authViewModel.currentUser.collectAsState()
            if (currentUser?.role == UserRole.ADMIN) {
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Checkout.route) { inclusive = true }
                    }
                }
            }

            CheckoutScreen(
                cartViewModel = cartViewModel,
                salesViewModel = salesViewModel,
                authViewModel = authViewModel,
                onNavigateToMap = { navController.navigate(Screen.DeliveryMap.route) },
                onNavigateToMyOrders = {
                    navController.navigate(Screen.MyOrders.route) {
                        popUpTo(Screen.Cart.route) { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 14. Delivery Map Screen (OpenStreetMap interactivo con osmdroid)
        composable(Screen.DeliveryMap.route) {
            DeliveryMapScreen(
                authViewModel = authViewModel,
                cartViewModel = cartViewModel,
                salesViewModel = salesViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 15. Inventory Screen (Gestión de Stock y Precios; Solo ADMIN)
        composable(Screen.Inventory.route) {
            val currentUser by authViewModel.currentUser.collectAsState()
            if (currentUser?.role == UserRole.CLIENTE) {
                LaunchedEffect(Unit) {
                    navController.popBackStack()
                }
            }

            InventoryScreen(
                productViewModel = productViewModel,
                onNavigateToEditProduct = { prodId ->
                    navController.navigate(Screen.AddEditProduct.createRoute(prodId))
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 16. Sales History Screen (Historial de Ventas con pedidos reales; Solo ADMIN)
        composable(Screen.SalesHistory.route) {
            val currentUser by authViewModel.currentUser.collectAsState()
            if (currentUser?.role == UserRole.CLIENTE) {
                LaunchedEffect(Unit) {
                    navController.popBackStack()
                }
            }

            SalesHistoryScreen(
                salesViewModel = salesViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 17. My Orders Screen (Historial de pedidos del cliente en sesión; Solo CLIENTE)
        composable(Screen.MyOrders.route) {
            MyOrdersScreen(
                authViewModel = authViewModel,
                salesViewModel = salesViewModel,
                onNavigateToCatalog = { navController.navigate(Screen.ProductCatalog.createRoute("Todos")) },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 18. Profile Screen (Ver y editar datos personales y contraseña; Ambos roles)
        composable(Screen.Profile.route) {
            ProfileScreen(
                authViewModel = authViewModel,
                onLogoutSuccess = {
                    authViewModel.logout()
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 19. Settings Screen
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

        // 20. Support Screen
        composable(Screen.Support.route) {
            SupportScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 21. Nuestros Locales Screen (OpenStreetMap interactivo con sucursales Santiago)
        composable(Screen.LocalesMap.route) {
            LocalesMapScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 22. Notifications Screen (Simulación con contador y leído/no leído)
        composable(Screen.Notifications.route) {
            NotificationsScreen(
                authViewModel = authViewModel,
                notificationViewModel = notificationViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
