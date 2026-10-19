# OYL Frutos Secos 🌰

Aplicación móvil Android para la **gestión de inventario y ventas** de una tienda de frutos secos, semillas y deshidratados.

## Problemática

OYL Frutos Secos es un pequeño negocio que vende a granel y despacha en Santiago. Hoy controla su stock y sus ventas en cuadernos y planillas, lo que provoca:

- Diferencias entre el stock real y el registrado.
- Demora al buscar productos y precios al momento de vender.
- Ventas sin un registro ordenado (cliente, dirección, zona de despacho, medio de pago).

La app permite al operador de la tienda **registrar productos con foto y código de barras, consultar el stock escaneando el envase y registrar ventas** que descuentan el inventario automáticamente. Todo queda guardado en el teléfono, sin necesidad de conexión a internet.

## Autor

**Martin Fariña** (trabajo individual) · GitHub: [@renemarambio](https://github.com/renemarambio)

**Asignatura:** DSY1105 – Desarrollo de Aplicaciones Móviles · **Sección / Equipo N°:** _(completar)_

**Planificación (ClickUp):** [Tablero de desarrollo](https://sharing.clickup.com/90131413814/b/h/6-901329233759-2/8962b8194791d64)

## Funcionalidades implementadas

| Requisito | Implementación |
|---|---|
| Diseño visual Material 3 | Jetpack Compose + `MaterialTheme` con esquema de color claro/oscuro propio (`ui/theme`). |
| Navegación funcional | `NavHost` centralizado con 15 rutas tipadas (`ui/navigation/Screen.kt`) y paso de argumentos (id de producto, categoría, modo del escáner). |
| Formularios validados | Login, registro/edición de producto y datos de despacho. Cada campo muestra ícono y mensaje de error animado. |
| Validaciones desde la lógica | Reglas centralizadas en `domain/validation/Validators.kt`, usadas por los ViewModels y cubiertas por tests unitarios (`ValidatorsTest`). |
| Gestión de estado | ViewModels con `StateFlow` y `collectAsState()`; la UI solo observa el estado y envía eventos. |
| Almacenamiento local | Base de datos **SQLite** (`AppDatabaseHelper`) con tablas de productos, ventas e ítems de venta; las fotos se guardan en el almacenamiento interno de la app. |
| Recursos nativos | **Cámara** (foto del producto, con solicitud de permiso en tiempo de ejecución), **escáner QR / código de barras** (ZXing) y **llamada / correo** a soporte mediante `Intent`. |
| Animaciones | Splash animado, transiciones entre pantallas, contador del carrito animado, cantidad animada en el detalle, lista del catálogo animada, mensajes de error y preguntas frecuentes desplegables. |

### Pantallas

Splash · Login · Inicio · Categorías · Catálogo · Detalle de producto · Nuevo/Editar producto · Foto del producto · Escáner y etiqueta QR · Carrito · Checkout · Zonas de reparto · Historial de ventas · Ajustes · Ayuda y contacto.

## Arquitectura (MVVM)

```
app/src/main/java/com/example/oylfrutossecos/
├── data/
│   ├── local/          # AppDatabaseHelper (SQLite), ImageStorage (fotos), datos iniciales
│   ├── model/          # Product, Sale, SaleItem, CartItem, Category, DeliveryZone, User
│   └── repository/     # ProductRepository, SalesRepository, CartRepository, AuthRepository
├── domain/
│   └── validation/     # Validators: reglas de validación de todos los formularios
├── ui/
│   ├── components/     # Componentes reutilizables (imagen de producto, feedback de formularios, carrito)
│   ├── navigation/     # Screen (rutas) y NavGraph (NavHost + transiciones)
│   ├── screens/        # Pantallas Compose (solo UI)
│   ├── theme/          # Colores, tipografía y tema Material 3
│   └── viewmodel/      # AuthViewModel, ProductViewModel, CartViewModel, SalesViewModel
└── MainActivity.kt
```

Flujo: **Pantalla (Compose)** → evento → **ViewModel** (valida con `Validators` y actualiza `StateFlow`) → **Repository** → **SQLite / almacenamiento interno**.

## Tecnologías

- Kotlin 2.0 · Jetpack Compose · Material 3
- Navigation Compose · Lifecycle ViewModel · Coroutines / StateFlow
- SQLite (`SQLiteOpenHelper`)
- ZXing Android Embedded 4.3.0 (lectura y generación de códigos QR)
- JUnit 4 (tests unitarios de validaciones)

## Cómo ejecutar

1. Clonar el repositorio:
   ```bash
   git clone https://github.com/renemarambio/OYL-Frutos-Secos.git
   ```
2. Abrir la carpeta del proyecto en **Android Studio** (Ladybug o superior).
3. En *Settings → Build Tools → Gradle*, usar **Gradle JDK 17 o 21** (por ejemplo, el `jbr-21` incluido en Android Studio).
4. Esperar la sincronización de Gradle.
5. Ejecutar en un emulador o dispositivo con **Android 7.0 (API 24)** o superior con el botón ▶ *Run 'app'*.

### Acceso

- Cualquier correo con formato válido y una contraseña de al menos 6 caracteres, **o**
- El botón **"Entrar como invitado"** en la pantalla de inicio de sesión.

### Probar los recursos nativos

- **Cámara:** Inicio → Nuevo producto → *Tomar foto*. La app pide el permiso de cámara la primera vez.
- **Escáner:** Inicio → Escanear código. Para probar en el emulador, en la pestaña *Etiqueta QR* se genera el QR de cada producto; también se puede escribir el código manualmente.
- **Llamada / correo:** Inicio → Ayuda → *Llamar* o *Correo*.

### Tests unitarios

```bash
./gradlew test
```
