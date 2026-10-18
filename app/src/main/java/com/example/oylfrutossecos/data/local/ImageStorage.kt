package com.example.oylfrutossecos.data.local

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.io.FileOutputStream

/**
 * Guarda y lee las fotos de productos en el almacenamiento interno privado de la app.
 * Las imágenes quedan en /data/data/<paquete>/files/product_images y solo esta app puede leerlas.
 * En la base de datos se guarda únicamente la ruta absoluta del archivo (columna imageUrl).
 */
object ImageStorage {

    private const val FOLDER = "product_images"

    fun saveBitmap(context: Context, bitmap: Bitmap): String {
        val dir = File(context.filesDir, FOLDER).apply { if (!exists()) mkdirs() }
        val file = File(dir, "IMG_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
        return file.absolutePath
    }

    fun loadBitmap(path: String): Bitmap? {
        if (path.isBlank()) return null
        val file = File(path)
        return if (file.exists()) BitmapFactory.decodeFile(file.absolutePath) else null
    }

    fun deleteImage(path: String) {
        if (path.isBlank()) return
        val file = File(path)
        if (file.exists() && file.parentFile?.name == FOLDER) file.delete()
    }
}
