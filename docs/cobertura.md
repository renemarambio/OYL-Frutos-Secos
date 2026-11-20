# Reporte de Cobertura de Pruebas Unitarias - OYL Frutos Secos

## Resumen Ejecutivo

En cumplimiento con los requisitos de la evaluación **DSY1105 (EP4/EP5)**, se implementó una suite integral de pruebas unitarias utilizando **JUnit 4**, **MockK** y **kotlinx-coroutines-test**, alcanzando una cobertura general del **82,74 %** en las capas de lógica de negocio y arquitectura limpia, superando el umbral exigido del **80 %**.

El reporte fue generado con la herramienta **JaCoCo (0.8.12)** mediante la tarea Gradle `jacocoTestReport`.

---

## Resultados Generales de JaCoCo

| Métrica | Cubierto / Total | Porcentaje |
| :--- | :---: | :---: |
| **Instrucciones (Coverage Principal)** | **12.492 / 15.097** | **82,74 %** |
| **Total de Pruebas Unitarias** | **184 pruebas** | **100 % Aprobadas (0 fallos)** |

---

## Cobertura por Paquetes de Lógica

Se midieron estrictamente los paquetes que contienen la lógica de negocio, validaciones, mapeos y flujos de datos, excluyendo componentes de UI Compose, vistas Android, temas y clases generadas:

| Paquete | Instrucciones Cubiertas / Total | Porcentaje de Cobertura |
| :--- | :---: | :---: |
| `com.example.oylfrutossecos.domain.validation` | 614 / 614 | **100 %** |
| `com.example.oylfrutossecos.data.remote.mapper` | 538 / 554 | **97,1 %** |
| `com.example.oylfrutossecos.data.repository` | 2.078 / 2.484 | **83,7 %** |
| `com.example.oylfrutossecos.ui.viewmodel` | 9.262 / 11.445 | **80,9 %** |
| **TOTAL GENERAL** | **12.492 / 15.097** | **82,74 %** |

---

## Detalle de la Suite de Pruebas Unitarias (184 Tests)

Todas las pruebas se ejecutan de manera aislada, sin depender de red externa ni bases de datos físicas:

### 1. Repositorios (`data/repository`)
- **`ProductRepositoryTest`**:
  - Obtención de catálogo remoto exitoso y persistencia automática en caché local SQLite.
  - Fallo de conexión de red -> fallback a la caché local SQLite con mensaje informativo.
  - Creación, actualización y eliminación de productos contra `ms-productos`.
  - Manejo de respuestas HTTP 409 (conflicto/código duplicado) y 404 (no encontrado).
  - Actualización de stock mediante PATCH.
- **`SalesRepositoryTest`**:
  - Creación de órdenes mapeando correctamente `OrderRequestDto` hacia `ms-pedidos`.
  - Filtrado de pedidos por correo del cliente (`customerEmail`).
  - Obtención de todas las órdenes para el rol administrador.
  - Actualización de estado del pedido (`PATCH api/orders/{id}/status`).
  - Manejo de error 409 (stock insuficiente) mapeado a `Result.failure` con mensaje descriptivo.
  - Eliminación de órdenes.
- **`NotificationRepositoryTest`**:
  - Delegación completa hacia `NotificationLocalDataSource` para listar, insertar, marcar como leídas, eliminar individual y masivamente, y contar no leídas.
- **`IndicatorRepositoryTest`**:
  - Consulta exitosa del valor del dólar y UF desde la API de mindicador.cl.
  - Manejo de excepciones de red y respuestas fallidas.
- **`CartRepositoryTest`**:
  - Estado del carrito reactivo (StateFlow), cálculo de subtotal, IVA (19 %), costo de envío según distancia y conteo de ítems.

### 2. ViewModels (`ui/viewmodel`)
- **`ProductViewModelTest`**:
  - Carga inicial de productos y filtrado por categoría o búsqueda de texto.
  - Validaciones de formulario (nombre, precio con o sin puntos de miles, stock, código de barras, categoría).
  - Conversión y sanitización de precio CLP ("12.000" -> 12000.0).
  - Bloqueo de acciones de mutación (crear/editar/eliminar) en modo sin conexión (caché).
  - Operaciones CRUD exitosas y propagación de estados de error/éxito en la UI.
- **`SalesViewModelTest`**:
  - Carga de pedidos de clientes y pedidos globales de administrador.
  - Validación de campos requeridos durante el checkout.
  - Creación de pedidos, actualización de estados de despacho y eliminación de pedidos.
- **`IndicatorViewModelTest`**:
  - Transición de estados `Loading` -> `Success` / `Error`.
  - Formateo legible de fecha y valor del dólar observado.
- **`CartViewModelTest`**:
  - Agregar productos al carrito, incrementar/decrementar cantidades y remover productos.
  - Cálculo dinámico de tarifa de despacho a partir de distancia de geolocalización.
  - Vaciado del carrito posterior a la compra.
- **`AuthViewModelTest`**:
  - Validación de credenciales de inicio de sesión y registro de usuarios.
  - Persistencia de sesión en memoria / sesión activa.
  - Actualización de datos de perfil y cierre de sesión.
- **`ForgotPasswordViewModelTest`**:
  - Flujo de recuperación de contraseña: validación de correo, selección de método (código SMS/Email o pregunta de seguridad).
  - Validación y establecimiento de nueva contraseña.
- **`NotificationViewModelTest`**:
  - Filtrado de notificaciones por rol de usuario (cliente vs admin).
  - Marcado de notificaciones como leídas y conteo de no leídas para cliente y admin.
  - Detección de cambios de estado en pedidos locales vs remotos ("Tu pedido ORD-XXXX está EN_CAMINO").
  - Alerta de stock crítico para admin ("X productos con stock crítico") con control anti-spam de sesión.
  - Generación de notificación de compra confirmada para cliente ("Pedido ORD-XXXX recibido").
  - Confirmación de actualización de estado para admin.
  - Eliminación individual y masiva con persistencia en SQLite.

### 3. Mapeadores y Utilitarios (`data/remote/mapper`, `domain/validation`)
- **`MappersTest`**:
  - Mapeo bidireccional de DTOs (`ProductDto`, `OrderDto`, `OrderItemDto`, `CategoryDto`) a modelos de dominio (`Product`, `Sale`, `SaleItem`).
  - Mapeo de estados legibles en español: `PENDING` -> "Pendiente", `PROCESSING` -> "En preparación", `SHIPPED` -> "En camino", `DELIVERED` -> "Entregado", `CANCELLED` -> "Cancelado".
- **`ValidatorsTest`**:
  - Cobertura del 100 % de reglas de validación de correo, RUT chileno (módulo 11), contraseñas seguras, teléfonos, nombres y sanitización de precios CLP.
- **`ApiErrorParserTest`**:
  - Extracción y parseo de errores estándar de Spring Boot (códigos 400 con mapa de campos y 409 con mensaje de negocio).

---

## Instrucciones para Reproducir el Reporte

Para compilar, ejecutar las 184 pruebas unitarias y regenerar el reporte JaCoCo:

```bash
# En Windows (PowerShell o CMD)
.\gradlew.bat testDebugUnitTest jacocoTestReport

# En Linux / macOS
./gradlew testDebugUnitTest jacocoTestReport
```

El reporte interactivo en HTML se encuentra disponible en:
`app/build/reports/jacoco/jacocoTestReport/html/index.html`
