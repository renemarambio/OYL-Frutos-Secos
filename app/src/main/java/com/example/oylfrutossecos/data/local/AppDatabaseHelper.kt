package com.example.oylfrutossecos.data.local

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.oylfrutossecos.data.model.Product
import com.example.oylfrutossecos.data.model.Sale
import com.example.oylfrutossecos.data.model.SaleItem
import com.example.oylfrutossecos.data.model.User
import com.example.oylfrutossecos.data.model.UserRole

class AppDatabaseHelper(context: Context) : SQLiteOpenHelper(
    context,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
) {

    companion object {
        const val DATABASE_NAME = "oyl_frutos_secos.db"
        const val DATABASE_VERSION = 3

        // Tabla Productos
        const val TABLE_PRODUCTS = "products"
        const val COL_PROD_ID = "id"
        const val COL_PROD_NAME = "name"
        const val COL_PROD_CATEGORY = "category"
        const val COL_PROD_PRICE = "price"
        const val COL_PROD_STOCK = "stock"
        const val COL_PROD_UNIT = "unit"
        const val COL_PROD_DESCRIPTION = "description"
        const val COL_PROD_INGREDIENTS = "ingredients"
        const val COL_PROD_ORIGIN = "origin"
        const val COL_PROD_BARCODE = "barcode"
        const val COL_PROD_IMAGE_URL = "imageUrl"
        const val COL_PROD_FAVORITE = "isFavorite"

        // Tabla Ventas / Pedidos
        const val TABLE_SALES = "sales"
        const val COL_SALE_ID = "id"
        const val COL_SALE_ORDER_NUMBER = "orderNumber"
        const val COL_SALE_TIMESTAMP = "timestamp"
        const val COL_SALE_DATE = "dateFormatted"
        const val COL_SALE_CUSTOMER_NAME = "customerName"
        const val COL_SALE_CUSTOMER_EMAIL = "customerEmail"
        const val COL_SALE_CUSTOMER_ADDRESS = "customerAddress"
        const val COL_SALE_DELIVERY_ZONE = "deliveryZone"
        const val COL_SALE_PAYMENT_METHOD = "paymentMethod"
        const val COL_SALE_SUBTOTAL = "subtotal"
        const val COL_SALE_DELIVERY_FEE = "deliveryFee"
        const val COL_SALE_TOTAL = "total"
        const val COL_SALE_STATUS = "status"
        const val COL_SALE_LATITUDE = "latitude"
        const val COL_SALE_LONGITUDE = "longitude"

        // Tabla Ítems de Venta
        const val TABLE_SALE_ITEMS = "sale_items"
        const val COL_ITEM_ID = "id"
        const val COL_ITEM_SALE_ID = "saleId"
        const val COL_ITEM_PROD_ID = "productId"
        const val COL_ITEM_PROD_NAME = "productName"
        const val COL_ITEM_UNIT_PRICE = "unitPrice"
        const val COL_ITEM_QUANTITY = "quantity"
        const val COL_ITEM_SUBTOTAL = "subtotal"

        // Tabla Usuarios
        const val TABLE_USERS = "users"
        const val COL_USER_ID = "id"
        const val COL_USER_NAME = "name"
        const val COL_USER_EMAIL = "email"
        const val COL_USER_PASSWORD = "password"
        const val COL_USER_ROLE = "role"
        const val COL_USER_PHONE = "phone"
        const val COL_USER_ADDRESS = "address"
        const val COL_USER_SECURITY_QUESTION = "security_question"
        const val COL_USER_SECURITY_ANSWER_HASH = "security_answer_hash"

        // Tabla Recuperación de Contraseña
        const val TABLE_PASSWORD_RESETS = "password_resets"
        const val COL_RESET_ID = "id"
        const val COL_RESET_EMAIL = "email"
        const val COL_RESET_CODE = "code"
        const val COL_RESET_EXPIRES_AT = "expires_at"
        const val COL_RESET_ATTEMPTS = "attempts"
        const val COL_RESET_LOCKED_UNTIL = "locked_until"
        const val COL_RESET_CREATED_AT = "created_at"

        @Volatile
        private var INSTANCE: AppDatabaseHelper? = null

        fun getInstance(context: Context): AppDatabaseHelper {
            return INSTANCE ?: synchronized(this) {
                val instance = AppDatabaseHelper(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createProductsTable = """
            CREATE TABLE $TABLE_PRODUCTS (
                $COL_PROD_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_PROD_NAME TEXT NOT NULL,
                $COL_PROD_CATEGORY TEXT NOT NULL,
                $COL_PROD_PRICE REAL NOT NULL,
                $COL_PROD_STOCK INTEGER NOT NULL,
                $COL_PROD_UNIT TEXT NOT NULL,
                $COL_PROD_DESCRIPTION TEXT,
                $COL_PROD_INGREDIENTS TEXT,
                $COL_PROD_ORIGIN TEXT,
                $COL_PROD_BARCODE TEXT,
                $COL_PROD_IMAGE_URL TEXT,
                $COL_PROD_FAVORITE INTEGER DEFAULT 0
            )
        """.trimIndent()

        val createSalesTable = """
            CREATE TABLE $TABLE_SALES (
                $COL_SALE_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_SALE_ORDER_NUMBER TEXT NOT NULL UNIQUE,
                $COL_SALE_TIMESTAMP INTEGER NOT NULL,
                $COL_SALE_DATE TEXT NOT NULL,
                $COL_SALE_CUSTOMER_NAME TEXT NOT NULL,
                $COL_SALE_CUSTOMER_EMAIL TEXT NOT NULL,
                $COL_SALE_CUSTOMER_ADDRESS TEXT NOT NULL,
                $COL_SALE_DELIVERY_ZONE TEXT NOT NULL,
                $COL_SALE_PAYMENT_METHOD TEXT NOT NULL,
                $COL_SALE_SUBTOTAL REAL NOT NULL,
                $COL_SALE_DELIVERY_FEE REAL NOT NULL,
                $COL_SALE_TOTAL REAL NOT NULL,
                $COL_SALE_STATUS TEXT NOT NULL,
                $COL_SALE_LATITUDE REAL DEFAULT -33.42628,
                $COL_SALE_LONGITUDE REAL DEFAULT -70.61254
            )
        """.trimIndent()

        val createSaleItemsTable = """
            CREATE TABLE $TABLE_SALE_ITEMS (
                $COL_ITEM_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_ITEM_SALE_ID INTEGER NOT NULL,
                $COL_ITEM_PROD_ID INTEGER NOT NULL,
                $COL_ITEM_PROD_NAME TEXT NOT NULL,
                $COL_ITEM_UNIT_PRICE REAL NOT NULL,
                $COL_ITEM_QUANTITY INTEGER NOT NULL,
                $COL_ITEM_SUBTOTAL REAL NOT NULL,
                FOREIGN KEY ($COL_ITEM_SALE_ID) REFERENCES $TABLE_SALES($COL_SALE_ID) ON DELETE CASCADE
            )
        """.trimIndent()

        val createUsersTable = """
            CREATE TABLE $TABLE_USERS (
                $COL_USER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_USER_NAME TEXT NOT NULL,
                $COL_USER_EMAIL TEXT NOT NULL UNIQUE,
                $COL_USER_PASSWORD TEXT NOT NULL,
                $COL_USER_ROLE TEXT NOT NULL,
                $COL_USER_PHONE TEXT NOT NULL,
                $COL_USER_ADDRESS TEXT NOT NULL,
                $COL_USER_SECURITY_QUESTION TEXT DEFAULT '',
                $COL_USER_SECURITY_ANSWER_HASH TEXT DEFAULT ''
            )
        """.trimIndent()

        val createPasswordResetsTable = """
            CREATE TABLE $TABLE_PASSWORD_RESETS (
                $COL_RESET_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_RESET_EMAIL TEXT NOT NULL,
                $COL_RESET_CODE TEXT NOT NULL,
                $COL_RESET_EXPIRES_AT INTEGER NOT NULL,
                $COL_RESET_ATTEMPTS INTEGER DEFAULT 0,
                $COL_RESET_LOCKED_UNTIL INTEGER DEFAULT 0,
                $COL_RESET_CREATED_AT INTEGER NOT NULL
            )
        """.trimIndent()

        db.execSQL(createProductsTable)
        db.execSQL(createSalesTable)
        db.execSQL(createSaleItemsTable)
        db.execSQL(createUsersTable)
        db.execSQL(createPasswordResetsTable)

        // Precarga de datos iniciales
        populateMockProducts(db)
        populateMockSales(db)
        populateMockUsers(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            // Crear tabla de usuarios si no existe
            val createUsersTable = """
                CREATE TABLE IF NOT EXISTS $TABLE_USERS (
                    $COL_USER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    $COL_USER_NAME TEXT NOT NULL,
                    $COL_USER_EMAIL TEXT NOT NULL UNIQUE,
                    $COL_USER_PASSWORD TEXT NOT NULL,
                    $COL_USER_ROLE TEXT NOT NULL,
                    $COL_USER_PHONE TEXT NOT NULL,
                    $COL_USER_ADDRESS TEXT NOT NULL
                )
            """.trimIndent()
            db.execSQL(createUsersTable)
            populateMockUsers(db)

            // Agregar columnas latitude y longitude a sales de forma segura
            try {
                db.execSQL("ALTER TABLE $TABLE_SALES ADD COLUMN $COL_SALE_LATITUDE REAL DEFAULT -33.42628")
            } catch (_: Exception) {}
            try {
                db.execSQL("ALTER TABLE $TABLE_SALES ADD COLUMN $COL_SALE_LONGITUDE REAL DEFAULT -70.61254")
            } catch (_: Exception) {}
        }

        if (oldVersion < 3) {
            try {
                db.execSQL("ALTER TABLE $TABLE_USERS ADD COLUMN $COL_USER_SECURITY_QUESTION TEXT DEFAULT ''")
            } catch (_: Exception) {}
            try {
                db.execSQL("ALTER TABLE $TABLE_USERS ADD COLUMN $COL_USER_SECURITY_ANSWER_HASH TEXT DEFAULT ''")
            } catch (_: Exception) {}

            val createPasswordResetsTable = """
                CREATE TABLE IF NOT EXISTS $TABLE_PASSWORD_RESETS (
                    $COL_RESET_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    $COL_RESET_EMAIL TEXT NOT NULL,
                    $COL_RESET_CODE TEXT NOT NULL,
                    $COL_RESET_EXPIRES_AT INTEGER NOT NULL,
                    $COL_RESET_ATTEMPTS INTEGER DEFAULT 0,
                    $COL_RESET_LOCKED_UNTIL INTEGER DEFAULT 0,
                    $COL_RESET_CREATED_AT INTEGER NOT NULL
                )
            """.trimIndent()
            db.execSQL(createPasswordResetsTable)

            updateMockUsersSecurity(db)
        }
    }

    private fun updateMockUsersSecurity(db: SQLiteDatabase) {
        val adminAnswerHash = com.example.oylfrutossecos.domain.validation.Validators.hashSecurityAnswer("Almendra")
        val clientAnswerHash = com.example.oylfrutossecos.domain.validation.Validators.hashSecurityAnswer("Santiago")

        val adminEmails = listOf("admin@oyl.cl", "oscar.yanez@oylfrutossecos.cl", "demo.vendedor@oylfrutossecos.cl")
        for (email in adminEmails) {
            db.execSQL(
                "UPDATE $TABLE_USERS SET $COL_USER_SECURITY_QUESTION = ?, $COL_USER_SECURITY_ANSWER_HASH = ? WHERE LOWER($COL_USER_EMAIL) = ? AND ($COL_USER_SECURITY_QUESTION IS NULL OR $COL_USER_SECURITY_QUESTION = '')",
                arrayOf("¿Nombre de tu primera mascota?", adminAnswerHash, email)
            )
        }

        db.execSQL(
            "UPDATE $TABLE_USERS SET $COL_USER_SECURITY_QUESTION = ?, $COL_USER_SECURITY_ANSWER_HASH = ? WHERE LOWER($COL_USER_EMAIL) = ? AND ($COL_USER_SECURITY_QUESTION IS NULL OR $COL_USER_SECURITY_QUESTION = '')",
            arrayOf("¿Ciudad donde naciste?", clientAnswerHash, "cliente@oyl.cl")
        )
    }

    private fun populateMockUsers(db: SQLiteDatabase) {
        val adminAnswerHash = com.example.oylfrutossecos.domain.validation.Validators.hashSecurityAnswer("Almendra")
        val clientAnswerHash = com.example.oylfrutossecos.domain.validation.Validators.hashSecurityAnswer("Santiago")

        // 1. Administrador oficial precargado
        insertOrIgnoreUser(
            db,
            name = "Oscar Yáñez",
            email = "admin@oyl.cl",
            password = "Admin123!",
            role = UserRole.ADMIN.name,
            phone = "+56 9 9123 4567",
            address = "Av. Providencia #1820, Oficina 402, Providencia",
            securityQuestion = "¿Nombre de tu primera mascota?",
            securityAnswerHash = adminAnswerHash
        )

        // Administrador secundario/compatible con el login anterior
        insertOrIgnoreUser(
            db,
            name = "Oscar Yáñez",
            email = "oscar.yanez@oylfrutossecos.cl",
            password = "Admin123!",
            role = UserRole.ADMIN.name,
            phone = "+56 9 9123 4567",
            address = "Av. Providencia #1820, Oficina 402, Providencia",
            securityQuestion = "¿Nombre de tu primera mascota?",
            securityAnswerHash = adminAnswerHash
        )
        insertOrIgnoreUser(
            db,
            name = "Vendedor Demo",
            email = "demo.vendedor@oylfrutossecos.cl",
            password = "DemoUser2026!",
            role = UserRole.ADMIN.name,
            phone = "+56 9 9123 4567",
            address = "Av. Providencia #1820, Oficina 402, Providencia",
            securityQuestion = "¿Nombre de tu primera mascota?",
            securityAnswerHash = adminAnswerHash
        )

        // 2. Cliente de prueba precargado
        insertOrIgnoreUser(
            db,
            name = "Cliente Demo OYL",
            email = "cliente@oyl.cl",
            password = "Cliente123",
            role = UserRole.CLIENTE.name,
            phone = "+56 9 8765 4321",
            address = "Av. Providencia 1240, Dpto 802, Providencia",
            securityQuestion = "¿Ciudad donde naciste?",
            securityAnswerHash = clientAnswerHash
        )
    }

    private fun insertOrIgnoreUser(
        db: SQLiteDatabase,
        name: String,
        email: String,
        password: String,
        role: String,
        phone: String,
        address: String,
        securityQuestion: String = "",
        securityAnswerHash: String = ""
    ) {
        val values = ContentValues().apply {
            put(COL_USER_NAME, name)
            put(COL_USER_EMAIL, email.trim().lowercase())
            put(COL_USER_PASSWORD, password)
            put(COL_USER_ROLE, role)
            put(COL_USER_PHONE, phone)
            put(COL_USER_ADDRESS, address)
            put(COL_USER_SECURITY_QUESTION, securityQuestion)
            put(COL_USER_SECURITY_ANSWER_HASH, securityAnswerHash)
        }
        db.insertWithOnConflict(TABLE_USERS, null, values, SQLiteDatabase.CONFLICT_IGNORE)
    }

    private fun populateMockProducts(db: SQLiteDatabase) {
        MockDataProvider.initialProducts.forEach { product ->
            val values = ContentValues().apply {
                put(COL_PROD_NAME, product.name)
                put(COL_PROD_CATEGORY, product.category)
                put(COL_PROD_PRICE, product.price)
                put(COL_PROD_STOCK, product.stock)
                put(COL_PROD_UNIT, product.unit)
                put(COL_PROD_DESCRIPTION, product.description)
                put(COL_PROD_INGREDIENTS, product.ingredients)
                put(COL_PROD_ORIGIN, product.origin)
                put(COL_PROD_BARCODE, product.barcode)
                put(COL_PROD_IMAGE_URL, product.imageUrl)
                put(COL_PROD_FAVORITE, if (product.isFavorite) 1 else 0)
            }
            db.insert(TABLE_PRODUCTS, null, values)
        }
    }

    private fun populateMockSales(db: SQLiteDatabase) {
        // Venta histórica 1
        val saleValues1 = ContentValues().apply {
            put(COL_SALE_ORDER_NUMBER, "ORD-2026-1001")
            put(COL_SALE_TIMESTAMP, System.currentTimeMillis() - 86400000L * 2)
            put(COL_SALE_DATE, "12/10/2026 14:35")
            put(COL_SALE_CUSTOMER_NAME, "Constanza Morales")
            put(COL_SALE_CUSTOMER_EMAIL, "cliente@oyl.cl")
            put(COL_SALE_CUSTOMER_ADDRESS, "Av. Providencia 1240, Dpto 802, Providencia")
            put(COL_SALE_DELIVERY_ZONE, "Santiago Centro & Providencia")
            put(COL_SALE_PAYMENT_METHOD, "Tarjeta Débito/Crédito")
            put(COL_SALE_SUBTOTAL, 21400.0)
            put(COL_SALE_DELIVERY_FEE, 2500.0)
            put(COL_SALE_TOTAL, 23900.0)
            put(COL_SALE_STATUS, "Entregado")
            put(COL_SALE_LATITUDE, -33.4278)
            put(COL_SALE_LONGITUDE, -70.6152)
        }
        val saleId1 = db.insert(TABLE_SALES, null, saleValues1)

        val item1 = ContentValues().apply {
            put(COL_ITEM_SALE_ID, saleId1)
            put(COL_ITEM_PROD_ID, 1)
            put(COL_ITEM_PROD_NAME, "Nueces Mariposa Selección")
            put(COL_ITEM_UNIT_PRICE, 9900.0)
            put(COL_ITEM_QUANTITY, 1)
            put(COL_ITEM_SUBTOTAL, 9900.0)
        }
        val item2 = ContentValues().apply {
            put(COL_ITEM_SALE_ID, saleId1)
            put(COL_ITEM_PROD_ID, 2)
            put(COL_ITEM_PROD_NAME, "Almendras Nonpareil Tostadas")
            put(COL_ITEM_UNIT_PRICE, 11500.0)
            put(COL_ITEM_QUANTITY, 1)
            put(COL_ITEM_SUBTOTAL, 11500.0)
        }
        db.insert(TABLE_SALE_ITEMS, null, item1)
        db.insert(TABLE_SALE_ITEMS, null, item2)

        // Venta histórica 2
        val saleValues2 = ContentValues().apply {
            put(COL_SALE_ORDER_NUMBER, "ORD-2026-1002")
            put(COL_SALE_TIMESTAMP, System.currentTimeMillis() - 86400000L)
            put(COL_SALE_DATE, "13/10/2026 11:15")
            put(COL_SALE_CUSTOMER_NAME, "Rodrigo Valenzuela")
            put(COL_SALE_CUSTOMER_EMAIL, "r.valenzuela@gmail.com")
            put(COL_SALE_CUSTOMER_ADDRESS, "Apoquindo 4800, Las Condes")
            put(COL_SALE_DELIVERY_ZONE, "Sector Oriente")
            put(COL_SALE_PAYMENT_METHOD, "Transferencia Bancaria")
            put(COL_SALE_SUBTOTAL, 13200.0)
            put(COL_SALE_DELIVERY_FEE, 3500.0)
            put(COL_SALE_TOTAL, 16700.0)
            put(COL_SALE_STATUS, "Completado")
            put(COL_SALE_LATITUDE, -33.4112)
            put(COL_SALE_LONGITUDE, -70.5780)
        }
        val saleId2 = db.insert(TABLE_SALES, null, saleValues2)

        val item3 = ContentValues().apply {
            put(COL_ITEM_SALE_ID, saleId2)
            put(COL_ITEM_PROD_ID, 4)
            put(COL_ITEM_PROD_NAME, "Pistachos con Cáscara Sal de Mar")
            put(COL_ITEM_UNIT_PRICE, 13200.0)
            put(COL_ITEM_QUANTITY, 1)
            put(COL_ITEM_SUBTOTAL, 13200.0)
        }
        db.insert(TABLE_SALE_ITEMS, null, item3)
    }

    // --- OPERACIONES DE USUARIOS ---

    fun insertUser(user: User): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_USER_NAME, user.name.trim())
            put(COL_USER_EMAIL, user.email.trim().lowercase())
            put(COL_USER_PASSWORD, user.password)
            put(COL_USER_ROLE, user.role.name)
            put(COL_USER_PHONE, user.phone.trim())
            put(COL_USER_ADDRESS, user.address.trim())
            put(COL_USER_SECURITY_QUESTION, user.securityQuestion.trim())
            put(COL_USER_SECURITY_ANSWER_HASH, user.securityAnswerHash.trim())
        }
        return db.insert(TABLE_USERS, null, values)
    }

    fun getUserByEmail(email: String): User? {
        val cleanEmail = email.trim().lowercase()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            null,
            "$COL_USER_EMAIL = ?",
            arrayOf(cleanEmail),
            null,
            null,
            null
        )
        cursor.use {
            if (it.moveToFirst()) {
                return cursorToUser(it)
            }
        }
        return null
    }

    fun authenticateUser(email: String, password: String): User? {
        val cleanEmail = email.trim().lowercase()
        val user = getUserByEmail(cleanEmail)
        return if (user != null && user.password == password) {
            user
        } else {
            null
        }
    }

    fun updateUser(user: User): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_USER_NAME, user.name.trim())
            put(COL_USER_PHONE, user.phone.trim())
            put(COL_USER_ADDRESS, user.address.trim())
            if (user.password.isNotBlank()) {
                put(COL_USER_PASSWORD, user.password)
            }
            if (user.securityQuestion.isNotBlank()) {
                put(COL_USER_SECURITY_QUESTION, user.securityQuestion.trim())
            }
            if (user.securityAnswerHash.isNotBlank()) {
                put(COL_USER_SECURITY_ANSWER_HASH, user.securityAnswerHash.trim())
            }
        }
        return db.update(TABLE_USERS, values, "$COL_USER_ID = ?", arrayOf(user.id.toString()))
    }

    private fun cursorToUser(cursor: Cursor): User {
        val roleStr = cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_ROLE))
        val qIdx = cursor.getColumnIndex(COL_USER_SECURITY_QUESTION)
        val aIdx = cursor.getColumnIndex(COL_USER_SECURITY_ANSWER_HASH)
        val secQ = if (qIdx >= 0 && !cursor.isNull(qIdx)) cursor.getString(qIdx) else ""
        val secA = if (aIdx >= 0 && !cursor.isNull(aIdx)) cursor.getString(aIdx) else ""

        return User(
            id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_USER_ID)),
            name = cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_NAME)),
            email = cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_EMAIL)),
            password = cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_PASSWORD)),
            role = UserRole.fromString(roleStr),
            phone = cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_PHONE)),
            address = cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_ADDRESS)),
            securityQuestion = secQ,
            securityAnswerHash = secA
        )
    }

    // --- OPERACIONES DE RECUPERACIÓN DE CONTRASEÑA ---

    fun savePasswordResetCode(email: String, code: String): Long {
        val cleanEmail = email.trim().lowercase()
        val db = writableDatabase
        val now = System.currentTimeMillis()
        val expiresAt = now + (5 * 60 * 1000L) // 5 minutos

        db.delete(TABLE_PASSWORD_RESETS, "LOWER($COL_RESET_EMAIL) = ?", arrayOf(cleanEmail))

        val values = ContentValues().apply {
            put(COL_RESET_EMAIL, cleanEmail)
            put(COL_RESET_CODE, code.trim())
            put(COL_RESET_EXPIRES_AT, expiresAt)
            put(COL_RESET_ATTEMPTS, 0)
            put(COL_RESET_LOCKED_UNTIL, 0L)
            put(COL_RESET_CREATED_AT, now)
        }
        return db.insert(TABLE_PASSWORD_RESETS, null, values)
    }

    fun getPasswordReset(email: String): PasswordResetRecord? {
        val cleanEmail = email.trim().lowercase()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_PASSWORD_RESETS,
            null,
            "LOWER($COL_RESET_EMAIL) = ?",
            arrayOf(cleanEmail),
            null,
            null,
            "$COL_RESET_ID DESC",
            "1"
        )
        cursor.use {
            if (it.moveToFirst()) {
                return PasswordResetRecord(
                    id = it.getLong(it.getColumnIndexOrThrow(COL_RESET_ID)),
                    email = it.getString(it.getColumnIndexOrThrow(COL_RESET_EMAIL)),
                    code = it.getString(it.getColumnIndexOrThrow(COL_RESET_CODE)),
                    expiresAt = it.getLong(it.getColumnIndexOrThrow(COL_RESET_EXPIRES_AT)),
                    attempts = it.getInt(it.getColumnIndexOrThrow(COL_RESET_ATTEMPTS)),
                    lockedUntil = it.getLong(it.getColumnIndexOrThrow(COL_RESET_LOCKED_UNTIL)),
                    createdAt = it.getLong(it.getColumnIndexOrThrow(COL_RESET_CREATED_AT))
                )
            }
        }
        return null
    }

    fun verifyResetCode(email: String, inputCode: String): VerifyCodeResult {
        val cleanEmail = email.trim().lowercase()
        val db = writableDatabase
        val record = getPasswordReset(cleanEmail)
            ?: return VerifyCodeResult.Error("No hay una solicitud de recuperación activa para este correo.")

        val now = System.currentTimeMillis()

        // 1. Verificar si está bloqueado por intentos excesivos (5 minutos)
        if (record.lockedUntil > now) {
            val remainingSec = ((record.lockedUntil - now) / 1000).coerceAtLeast(1)
            val min = remainingSec / 60
            val sec = remainingSec % 60
            val timeText = if (min > 0) "${min}m ${sec}s" else "${sec}s"
            return VerifyCodeResult.Error("Recuperación bloqueada temporalmente por superar los 3 intentos permitidos. Espera $timeText.", isLocked = true)
        }

        // 2. Verificar si el código expiró (5 minutos)
        if (now > record.expiresAt) {
            return VerifyCodeResult.Error("El código de verificación ha expirado (válido por 5 minutos). Solicita uno nuevo.")
        }

        // 3. Verificar código
        if (record.code == inputCode.trim()) {
            return VerifyCodeResult.Success
        } else {
            val newAttempts = record.attempts + 1
            if (newAttempts >= 3) {
                val lockUntil = now + (5 * 60 * 1000L) // Bloqueo durante 5 minutos
                val cv = ContentValues().apply {
                    put(COL_RESET_ATTEMPTS, newAttempts)
                    put(COL_RESET_LOCKED_UNTIL, lockUntil)
                }
                db.update(TABLE_PASSWORD_RESETS, cv, "$COL_RESET_ID = ?", arrayOf(record.id.toString()))
                return VerifyCodeResult.Error("Código incorrecto. Has superado los 3 intentos permitidos. Recuperación bloqueada durante 5 minutos.", isLocked = true)
            } else {
                val cv = ContentValues().apply {
                    put(COL_RESET_ATTEMPTS, newAttempts)
                }
                db.update(TABLE_PASSWORD_RESETS, cv, "$COL_RESET_ID = ?", arrayOf(record.id.toString()))
                val remaining = 3 - newAttempts
                return VerifyCodeResult.Error("Código incorrecto. Te quedan $remaining intento(s).", attemptsLeft = remaining)
            }
        }
    }

    fun updateUserPassword(email: String, newPassword: String): Boolean {
        val cleanEmail = email.trim().lowercase()
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_USER_PASSWORD, newPassword)
        }
        val rows = db.update(TABLE_USERS, values, "LOWER($COL_USER_EMAIL) = ?", arrayOf(cleanEmail))
        if (rows > 0) {
            db.delete(TABLE_PASSWORD_RESETS, "LOWER($COL_RESET_EMAIL) = ?", arrayOf(cleanEmail))
            return true
        }
        return false
    }

    // --- OPERACIONES DE PRODUCTOS ---

    fun getAllProducts(): List<Product> {
        val list = mutableListOf<Product>()
        val db = readableDatabase
        val cursor = db.query(TABLE_PRODUCTS, null, null, null, null, null, "$COL_PROD_NAME ASC")
        cursor.use {
            while (it.moveToNext()) {
                list.add(cursorToProduct(it))
            }
        }
        return list
    }

    fun getProductById(id: Long): Product? {
        val db = readableDatabase
        val cursor = db.query(TABLE_PRODUCTS, null, "$COL_PROD_ID = ?", arrayOf(id.toString()), null, null, null)
        cursor.use {
            if (it.moveToFirst()) {
                return cursorToProduct(it)
            }
        }
        return null
    }

    fun getProductByBarcode(barcode: String): Product? {
        val db = readableDatabase
        val cursor = db.query(TABLE_PRODUCTS, null, "$COL_PROD_BARCODE = ?", arrayOf(barcode), null, null, null)
        cursor.use {
            if (it.moveToFirst()) {
                return cursorToProduct(it)
            }
        }
        return null
    }

    fun getProductByCodeOrSku(code: String): Product? {
        val cleanCode = code.trim()
        val byBarcode = getProductByBarcode(cleanCode)
        if (byBarcode != null) return byBarcode

        val numericId = cleanCode.filter { it.isDigit() }.toLongOrNull()
        if (numericId != null) {
            val byId = getProductById(numericId)
            if (byId != null) return byId
        }

        val matches = searchProducts(cleanCode)
        return matches.firstOrNull()
    }

    fun searchProducts(query: String): List<Product> {
        val list = mutableListOf<Product>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_PRODUCTS,
            null,
            "$COL_PROD_NAME LIKE ? OR $COL_PROD_CATEGORY LIKE ? OR $COL_PROD_BARCODE LIKE ?",
            arrayOf("%$query%", "%$query%", "%$query%"),
            null,
            null,
            "$COL_PROD_NAME ASC"
        )
        cursor.use {
            while (it.moveToNext()) {
                list.add(cursorToProduct(it))
            }
        }
        return list
    }

    fun getProductsByCategory(category: String): List<Product> {
        val list = mutableListOf<Product>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_PRODUCTS,
            null,
            "$COL_PROD_CATEGORY = ?",
            arrayOf(category),
            null,
            null,
            "$COL_PROD_NAME ASC"
        )
        cursor.use {
            while (it.moveToNext()) {
                list.add(cursorToProduct(it))
            }
        }
        return list
    }

    fun insertProduct(product: Product): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_PROD_NAME, product.name)
            put(COL_PROD_CATEGORY, product.category)
            put(COL_PROD_PRICE, product.price)
            put(COL_PROD_STOCK, product.stock)
            put(COL_PROD_UNIT, product.unit)
            put(COL_PROD_DESCRIPTION, product.description)
            put(COL_PROD_INGREDIENTS, product.ingredients)
            put(COL_PROD_ORIGIN, product.origin)
            put(COL_PROD_BARCODE, product.barcode)
            put(COL_PROD_IMAGE_URL, product.imageUrl)
            put(COL_PROD_FAVORITE, if (product.isFavorite) 1 else 0)
        }
        return db.insert(TABLE_PRODUCTS, null, values)
    }

    fun updateProduct(product: Product): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_PROD_NAME, product.name)
            put(COL_PROD_CATEGORY, product.category)
            put(COL_PROD_PRICE, product.price)
            put(COL_PROD_STOCK, product.stock)
            put(COL_PROD_UNIT, product.unit)
            put(COL_PROD_DESCRIPTION, product.description)
            put(COL_PROD_INGREDIENTS, product.ingredients)
            put(COL_PROD_ORIGIN, product.origin)
            put(COL_PROD_BARCODE, product.barcode)
            put(COL_PROD_IMAGE_URL, product.imageUrl)
            put(COL_PROD_FAVORITE, if (product.isFavorite) 1 else 0)
        }
        return db.update(TABLE_PRODUCTS, values, "$COL_PROD_ID = ?", arrayOf(product.id.toString()))
    }

    fun deleteProduct(id: Long): Int {
        val db = writableDatabase
        return db.delete(TABLE_PRODUCTS, "$COL_PROD_ID = ?", arrayOf(id.toString()))
    }

    fun updateProductStock(id: Long, newStock: Int): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_PROD_STOCK, newStock)
        }
        return db.update(TABLE_PRODUCTS, values, "$COL_PROD_ID = ?", arrayOf(id.toString()))
    }

    fun updateProductPrice(id: Long, newPrice: Double): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_PROD_PRICE, newPrice)
        }
        return db.update(TABLE_PRODUCTS, values, "$COL_PROD_ID = ?", arrayOf(id.toString()))
    }

    // --- OPERACIONES DE VENTAS ---

    fun insertSaleWithItems(sale: Sale, items: List<SaleItem>): Long {
        val db = writableDatabase
        db.beginTransaction()
        try {
            val saleValues = ContentValues().apply {
                put(COL_SALE_ORDER_NUMBER, sale.orderNumber)
                put(COL_SALE_TIMESTAMP, sale.timestamp)
                put(COL_SALE_DATE, sale.dateFormatted)
                put(COL_SALE_CUSTOMER_NAME, sale.customerName)
                put(COL_SALE_CUSTOMER_EMAIL, sale.customerEmail)
                put(COL_SALE_CUSTOMER_ADDRESS, sale.customerAddress)
                put(COL_SALE_DELIVERY_ZONE, sale.deliveryZone)
                put(COL_SALE_PAYMENT_METHOD, sale.paymentMethod)
                put(COL_SALE_SUBTOTAL, sale.subtotal)
                put(COL_SALE_DELIVERY_FEE, sale.deliveryFee)
                put(COL_SALE_TOTAL, sale.total)
                put(COL_SALE_STATUS, sale.status)
                put(COL_SALE_LATITUDE, sale.latitude)
                put(COL_SALE_LONGITUDE, sale.longitude)
            }
            val saleId = db.insert(TABLE_SALES, null, saleValues)

            for (item in items) {
                val itemValues = ContentValues().apply {
                    put(COL_ITEM_SALE_ID, saleId)
                    put(COL_ITEM_PROD_ID, item.productId)
                    put(COL_ITEM_PROD_NAME, item.productName)
                    put(COL_ITEM_UNIT_PRICE, item.unitPrice)
                    put(COL_ITEM_QUANTITY, item.quantity)
                    put(COL_ITEM_SUBTOTAL, item.subtotal)
                }
                db.insert(TABLE_SALE_ITEMS, null, itemValues)

                // Descontar stock atómicamente
                db.execSQL(
                    "UPDATE $TABLE_PRODUCTS SET $COL_PROD_STOCK = MAX(0, $COL_PROD_STOCK - ?) WHERE $COL_PROD_ID = ?",
                    arrayOf(item.quantity, item.productId)
                )
            }

            db.setTransactionSuccessful()
            return saleId
        } finally {
            db.endTransaction()
        }
    }

    fun getAllSales(): List<Sale> {
        val salesList = mutableListOf<Sale>()
        val db = readableDatabase
        val cursor = db.query(TABLE_SALES, null, null, null, null, null, "$COL_SALE_TIMESTAMP DESC")
        cursor.use {
            while (it.moveToNext()) {
                salesList.add(cursorToSale(it))
            }
        }
        return salesList
    }

    fun getSalesByCustomerEmail(email: String): List<Sale> {
        val cleanEmail = email.trim().lowercase()
        val salesList = mutableListOf<Sale>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_SALES,
            null,
            "LOWER($COL_SALE_CUSTOMER_EMAIL) = ?",
            arrayOf(cleanEmail),
            null,
            null,
            "$COL_SALE_TIMESTAMP DESC"
        )
        cursor.use {
            while (it.moveToNext()) {
                salesList.add(cursorToSale(it))
            }
        }
        return salesList
    }

    private fun cursorToSale(cursor: Cursor): Sale {
        val saleId = cursor.getLong(cursor.getColumnIndexOrThrow(COL_SALE_ID))
        val items = getSaleItems(saleId)

        val latIndex = cursor.getColumnIndex(COL_SALE_LATITUDE)
        val lonIndex = cursor.getColumnIndex(COL_SALE_LONGITUDE)
        val lat = if (latIndex >= 0 && !cursor.isNull(latIndex)) cursor.getDouble(latIndex) else -33.42628
        val lon = if (lonIndex >= 0 && !cursor.isNull(lonIndex)) cursor.getDouble(lonIndex) else -70.61254

        return Sale(
            id = saleId,
            orderNumber = cursor.getString(cursor.getColumnIndexOrThrow(COL_SALE_ORDER_NUMBER)),
            timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(COL_SALE_TIMESTAMP)),
            dateFormatted = cursor.getString(cursor.getColumnIndexOrThrow(COL_SALE_DATE)),
            customerName = cursor.getString(cursor.getColumnIndexOrThrow(COL_SALE_CUSTOMER_NAME)),
            customerEmail = cursor.getString(cursor.getColumnIndexOrThrow(COL_SALE_CUSTOMER_EMAIL)),
            customerAddress = cursor.getString(cursor.getColumnIndexOrThrow(COL_SALE_CUSTOMER_ADDRESS)),
            deliveryZone = cursor.getString(cursor.getColumnIndexOrThrow(COL_SALE_DELIVERY_ZONE)),
            paymentMethod = cursor.getString(cursor.getColumnIndexOrThrow(COL_SALE_PAYMENT_METHOD)),
            items = items,
            subtotal = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_SALE_SUBTOTAL)),
            deliveryFee = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_SALE_DELIVERY_FEE)),
            total = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_SALE_TOTAL)),
            status = cursor.getString(cursor.getColumnIndexOrThrow(COL_SALE_STATUS)),
            latitude = lat,
            longitude = lon
        )
    }

    fun getSaleItems(saleId: Long): List<SaleItem> {
        val list = mutableListOf<SaleItem>()
        val db = readableDatabase
        val cursor = db.query(TABLE_SALE_ITEMS, null, "$COL_ITEM_SALE_ID = ?", arrayOf(saleId.toString()), null, null, null)
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    SaleItem(
                        id = it.getLong(it.getColumnIndexOrThrow(COL_ITEM_ID)),
                        saleId = it.getLong(it.getColumnIndexOrThrow(COL_ITEM_SALE_ID)),
                        productId = it.getLong(it.getColumnIndexOrThrow(COL_ITEM_PROD_ID)),
                        productName = it.getString(it.getColumnIndexOrThrow(COL_ITEM_PROD_NAME)),
                        unitPrice = it.getDouble(it.getColumnIndexOrThrow(COL_ITEM_UNIT_PRICE)),
                        quantity = it.getInt(it.getColumnIndexOrThrow(COL_ITEM_QUANTITY)),
                        subtotal = it.getDouble(it.getColumnIndexOrThrow(COL_ITEM_SUBTOTAL))
                    )
                )
            }
        }
        return list
    }

    fun resetDatabaseToDefaults() {
        val db = writableDatabase
        db.execSQL("DELETE FROM $TABLE_SALE_ITEMS")
        db.execSQL("DELETE FROM $TABLE_SALES")
        db.execSQL("DELETE FROM $TABLE_PRODUCTS")
        db.execSQL("DELETE FROM $TABLE_USERS")
        db.execSQL("DELETE FROM $TABLE_PASSWORD_RESETS")
        populateMockProducts(db)
        populateMockSales(db)
        populateMockUsers(db)
    }

    private fun cursorToProduct(cursor: Cursor): Product {
        return Product(
            id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_PROD_ID)),
            name = cursor.getString(cursor.getColumnIndexOrThrow(COL_PROD_NAME)),
            category = cursor.getString(cursor.getColumnIndexOrThrow(COL_PROD_CATEGORY)),
            price = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_PROD_PRICE)),
            stock = cursor.getInt(cursor.getColumnIndexOrThrow(COL_PROD_STOCK)),
            unit = cursor.getString(cursor.getColumnIndexOrThrow(COL_PROD_UNIT)),
            description = cursor.getString(cursor.getColumnIndexOrThrow(COL_PROD_DESCRIPTION)) ?: "",
            ingredients = cursor.getString(cursor.getColumnIndexOrThrow(COL_PROD_INGREDIENTS)) ?: "",
            origin = cursor.getString(cursor.getColumnIndexOrThrow(COL_PROD_ORIGIN)) ?: "",
            barcode = cursor.getString(cursor.getColumnIndexOrThrow(COL_PROD_BARCODE)) ?: "",
            imageUrl = cursor.getString(cursor.getColumnIndexOrThrow(COL_PROD_IMAGE_URL)) ?: "",
            isFavorite = cursor.getInt(cursor.getColumnIndexOrThrow(COL_PROD_FAVORITE)) == 1
        )
    }
}

data class PasswordResetRecord(
    val id: Long,
    val email: String,
    val code: String,
    val expiresAt: Long,
    val attempts: Int,
    val lockedUntil: Long,
    val createdAt: Long
)

sealed class VerifyCodeResult {
    object Success : VerifyCodeResult()
    data class Error(val message: String, val isLocked: Boolean = false, val attemptsLeft: Int = 0) : VerifyCodeResult()
}

