package com.example.oylfrutossecos.data.local

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.oylfrutossecos.data.model.Product
import com.example.oylfrutossecos.data.model.Sale
import com.example.oylfrutossecos.data.model.SaleItem

class AppDatabaseHelper(context: Context) : SQLiteOpenHelper(
    context,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
) {

    companion object {
        const val DATABASE_NAME = "oyl_frutos_secos.db"
        const val DATABASE_VERSION = 1

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

        // Tabla Ventas
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

        // Tabla Ítems de Venta
        const val TABLE_SALE_ITEMS = "sale_items"
        const val COL_ITEM_ID = "id"
        const val COL_ITEM_SALE_ID = "saleId"
        const val COL_ITEM_PROD_ID = "productId"
        const val COL_ITEM_PROD_NAME = "productName"
        const val COL_ITEM_UNIT_PRICE = "unitPrice"
        const val COL_ITEM_QUANTITY = "quantity"
        const val COL_ITEM_SUBTOTAL = "subtotal"

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
                $COL_SALE_STATUS TEXT NOT NULL
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

        db.execSQL(createProductsTable)
        db.execSQL(createSalesTable)
        db.execSQL(createSaleItemsTable)

        // Precarga de productos Mock iniciales
        populateMockProducts(db)
        // Precarga de ventas de ejemplo para el historial
        populateMockSales(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_SALE_ITEMS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_SALES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PRODUCTS")
        onCreate(db)
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
            put(COL_SALE_CUSTOMER_EMAIL, "c.morales@duocuc.cl")
            put(COL_SALE_CUSTOMER_ADDRESS, "Av. Providencia 1240, Dpto 802")
            put(COL_SALE_DELIVERY_ZONE, "Santiago Centro & Providencia")
            put(COL_SALE_PAYMENT_METHOD, "Tarjeta Débito/Crédito")
            put(COL_SALE_SUBTOTAL, 21400.0)
            put(COL_SALE_DELIVERY_FEE, 2500.0)
            put(COL_SALE_TOTAL, 23900.0)
            put(COL_SALE_STATUS, "Entregado")
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
        // 1. Buscar por código de barras / SKU exacto
        val byBarcode = getProductByBarcode(cleanCode)
        if (byBarcode != null) return byBarcode

        // 2. Si viene formato PROD-001 o similar, extraer número y buscar por ID
        val numericId = cleanCode.filter { it.isDigit() }.toLongOrNull()
        if (numericId != null) {
            val byId = getProductById(numericId)
            if (byId != null) return byId
        }

        // 3. Búsqueda por coincidencia
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
                val saleId = it.getLong(it.getColumnIndexOrThrow(COL_SALE_ID))
                val items = getSaleItems(saleId)
                salesList.add(
                    Sale(
                        id = saleId,
                        orderNumber = it.getString(it.getColumnIndexOrThrow(COL_SALE_ORDER_NUMBER)),
                        timestamp = it.getLong(it.getColumnIndexOrThrow(COL_SALE_TIMESTAMP)),
                        dateFormatted = it.getString(it.getColumnIndexOrThrow(COL_SALE_DATE)),
                        customerName = it.getString(it.getColumnIndexOrThrow(COL_SALE_CUSTOMER_NAME)),
                        customerEmail = it.getString(it.getColumnIndexOrThrow(COL_SALE_CUSTOMER_EMAIL)),
                        customerAddress = it.getString(it.getColumnIndexOrThrow(COL_SALE_CUSTOMER_ADDRESS)),
                        deliveryZone = it.getString(it.getColumnIndexOrThrow(COL_SALE_DELIVERY_ZONE)),
                        paymentMethod = it.getString(it.getColumnIndexOrThrow(COL_SALE_PAYMENT_METHOD)),
                        items = items,
                        subtotal = it.getDouble(it.getColumnIndexOrThrow(COL_SALE_SUBTOTAL)),
                        deliveryFee = it.getDouble(it.getColumnIndexOrThrow(COL_SALE_DELIVERY_FEE)),
                        total = it.getDouble(it.getColumnIndexOrThrow(COL_SALE_TOTAL)),
                        status = it.getString(it.getColumnIndexOrThrow(COL_SALE_STATUS))
                    )
                )
            }
        }
        return salesList
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
        populateMockProducts(db)
        populateMockSales(db)
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
