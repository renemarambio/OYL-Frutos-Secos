# Backend OYL Frutos Secos — Microservicios Spring Boot

Dos microservicios independientes, cada uno con su propia base de datos MySQL:

| Microservicio | Puerto | Base de datos | Responsabilidad |
|---|---|---|---|
| `ms-productos` | 8081 | `oyl_productos` | Catálogo e inventario (CRUD de productos, stock, categorías) |
| `ms-pedidos` | 8082 | `oyl_pedidos` | Pedidos/ventas. Llama a `ms-productos` para leer precios y descontar stock |

Tecnologías: Java 17, Spring Boot 3.5, Spring Web, Spring Data JPA, Bean Validation, MySQL 8, springdoc (Swagger), JUnit 5 + Mockito.

## Estructura (igual en ambos)

```
controller/   -> endpoints REST (@RestController)
service/      -> lógica de negocio (@Service, @Transactional)
repository/   -> acceso a datos (Spring Data JPA)
model/        -> entidades JPA (tablas)
dto/          -> datos de entrada validados (records)
exception/    -> errores de negocio + @RestControllerAdvice (respuestas JSON)
client/       -> (solo ms-pedidos) cliente HTTP hacia ms-productos (RestClient)
config/       -> seed inicial / beans
```

## Endpoints

### ms-productos (`http://localhost:8081`)
| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/products?category=&q=` | Lista, filtra por categoría o busca por nombre |
| GET | `/api/products/{id}` | Detalle |
| GET | `/api/products/barcode/{codigo}` | Busca por código de barras (escáner) |
| GET | `/api/products/categories` | Categorías con cantidad de productos |
| POST | `/api/products` | Crea producto (valida nombre, precio > 0, stock ≥ 0, código 8-14 dígitos) |
| PUT | `/api/products/{id}` | Edita producto |
| DELETE | `/api/products/{id}` | Elimina producto |
| PATCH | `/api/products/{id}/stock` | Cambia stock `{ "stock": 30 }` |
| POST | `/api/products/{id}/decrease-stock` | Descuenta stock `{ "quantity": 2 }` (409 si no alcanza) |

### ms-pedidos (`http://localhost:8082`)
| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/orders` | Todas las ventas (administrador) |
| GET | `/api/orders?customerEmail=x` | "Mis pedidos" del cliente |
| GET | `/api/orders/{id}` | Detalle de un pedido |
| POST | `/api/orders` | Crea pedido: valida stock, calcula totales y descuenta stock en ms-productos |
| PATCH | `/api/orders/{id}/status` | Cambia estado `{ "status": "EN_CAMINO" }` |
| DELETE | `/api/orders/{id}` | Elimina pedido |

Estados: `PENDIENTE`, `PREPARANDO`, `EN_CAMINO`, `ENTREGADO`, `CANCELADO`.

Documentación interactiva (Swagger): `http://localhost:8081/swagger-ui.html` y `http://localhost:8082/swagger-ui.html`.

Códigos de error: 400 (datos inválidos, con detalle por campo), 404 (no existe), 409 (regla de negocio: stock insuficiente, código duplicado), 503 (ms-productos caído).

## Cómo ejecutar

### Opción A — Docker (todo junto)
```bash
cd backend
docker compose up --build
```
MySQL queda en el puerto **3307** (usuario `root`, clave `oyl1234`) para no chocar con XAMPP.

### Opción B — XAMPP + IDE
1. Iniciar **MySQL** en XAMPP (usuario `root`, sin contraseña). Las bases se crean solas.
2. Ejecutar `MsProductosApplication` y luego `MsPedidosApplication` (IntelliJ / VS Code), o:
```bash
cd backend/ms-productos && ./mvnw spring-boot:run
cd backend/ms-pedidos   && ./mvnw spring-boot:run
```
Al iniciar por primera vez, `ms-productos` carga los 14 productos del catálogo (`seed-products.json`).

## Pruebas
```bash
cd backend/ms-productos && ./mvnw test
cd backend/ms-pedidos   && ./mvnw test
```
Las pruebas usan Mockito (sin base de datos real) y MockMvc para la capa web.

## Desde el emulador Android
El emulador ve el PC como `10.0.2.2`: la app usa `http://10.0.2.2:8081/` y `http://10.0.2.2:8082/`.
