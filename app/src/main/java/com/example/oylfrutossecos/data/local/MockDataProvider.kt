package com.example.oylfrutossecos.data.local

import com.example.oylfrutossecos.data.model.Category
import com.example.oylfrutossecos.data.model.DeliveryZone
import com.example.oylfrutossecos.data.model.Product

object MockDataProvider {

    val categories = listOf(
        Category(
            id = "frutos_secos",
            name = "Frutos Secos",
            description = "Nueces, almendras, castañas y pistachos seleccionados.",
            iconName = "Spa",
            productCount = 5
        ),
        Category(
            id = "semillas",
            name = "Semillas",
            description = "Chía, sésamo, linaza, maravilla y calabaza 100% naturales.",
            iconName = "Grain",
            productCount = 4
        ),
        Category(
            id = "deshidratados",
            name = "Deshidratados",
            description = "Frutas deshidratadas, arándanos, dátiles y pasas premium.",
            iconName = "FilterVintage",
            productCount = 4
        ),
        Category(
            id = "mixes",
            name = "Mixes Especiales",
            description = "Combinaciones energéticas ideales para deportistas y colaciones.",
            iconName = "AutoAwesome",
            productCount = 2
        )
    )

    val initialProducts = listOf(
        Product(
            id = 1,
            name = "Nueces Mariposa Selección",
            category = "Frutos Secos",
            price = 9900.0,
            stock = 25,
            unit = "kg",
            description = "Nueces mariposa de cosecha reciente, seleccionadas a mano. Textura crujiente y sabor suave sin amargor. Ricas en ácidos grasos Omega-3 y antioxidantes.",
            ingredients = "100% Nuez mariposa variedad Chandler partida en mitades. Libre de aditivos y conservantes.",
            origin = "Valle de Limarí, Región de Coquimbo, Chile",
            barcode = "780998810001",
            imageUrl = "",
            isFavorite = true
        ),
        Product(
            id = 2,
            name = "Almendras Nonpareil Tostadas",
            category = "Frutos Secos",
            price = 11500.0,
            stock = 30,
            unit = "kg",
            description = "Almendra entera variedad Nonpareil con tostado lento al horno a temperatura controlada. Excelente fuente de vitamina E, magnesio y proteína vegetal.",
            ingredients = "Almendra chilena seleccionada grado Supreme, sin sal agregada ni aceites añadidos.",
            origin = "Valle Central de Chile",
            barcode = "780998810002",
            imageUrl = "",
            isFavorite = true
        ),
        Product(
            id = 3,
            name = "Castañas de Cajú Tostadas",
            category = "Frutos Secos",
            price = 14900.0,
            stock = 8, // Stock bajo de prueba
            unit = "kg",
            description = "Castañas de cajú tostadas con textura cremosa y sabor mantecoso irresistible. Perfectas para aperitivos o como ingrediente en repostería saludable.",
            ingredients = "Cajú entero grado W320 tostado, sin sal.",
            origin = "Ceará, Brasil (Envasado por OYL Frutos Secos)",
            barcode = "780998810003",
            imageUrl = "",
            isFavorite = false
        ),
        Product(
            id = 4,
            name = "Pistachos con Cáscara Sal de Mar",
            category = "Frutos Secos",
            price = 13200.0,
            stock = 15,
            unit = "kg",
            description = "Pistachos seleccionados abiertos de forma natural en el árbol, tostados con una pizca de sal marina de Cahuil.",
            ingredients = "Pistachos con cáscara (98.5%), sal de mar artesanal de Cahuil (1.5%).",
            origin = "Valle de Colchagua, Chile",
            barcode = "780998810004",
            imageUrl = "",
            isFavorite = true
        ),
        Product(
            id = 5,
            name = "Maní Tostado sin Sal",
            category = "Frutos Secos",
            price = 4200.0,
            stock = 45,
            unit = "kg",
            description = "Maní tipo Runner sin cáscara, tostado seco sin aceites. Ideal para elaboración de mantequilla de maní casera o snack diario.",
            ingredients = "100% Maní sin piel tostado.",
            origin = "Córdoba, Argentina (Procesado en Chile)",
            barcode = "780998810005",
            imageUrl = "",
            isFavorite = false
        ),
        Product(
            id = 6,
            name = "Semillas de Chía Negra",
            category = "Semillas",
            price = 5900.0,
            stock = 50,
            unit = "kg",
            description = "Semillas de chía negra ricas en fibra soluble, calcio y ácidos grasos esenciales. Ideales para pudines, batidos y panificación.",
            ingredients = "100% Semilla de chía negra (Salvia hispánica) limpia y tamizada.",
            origin = "Salta, Argentina",
            barcode = "780998810006",
            imageUrl = "",
            isFavorite = false
        ),
        Product(
            id = 7,
            name = "Semillas de Calabaza (Zapallo)",
            category = "Semillas",
            price = 9800.0,
            stock = 6, // Stock bajo
            unit = "kg",
            description = "Pepitas de calabaza peladas, crudas y seleccionadas. Alto contenido de zinc, hierro y aminoácidos como el triptófano.",
            ingredients = "100% Semillas de calabaza sin cáscara.",
            origin = "Valle del Aconcagua, Chile",
            barcode = "780998810007",
            imageUrl = "",
            isFavorite = true
        ),
        Product(
            id = 8,
            name = "Semillas de Girasol Peladas",
            category = "Semillas",
            price = 3800.0,
            stock = 40,
            unit = "kg",
            description = "Semillas de maravilla peladas y limpias. Aporte concentrado de vitamina E, fósforo y grasas saludables cardioprotectoras.",
            ingredients = "Semillas de girasol peladas.",
            origin = "Rancagua, Chile",
            barcode = "780998810008",
            imageUrl = "",
            isFavorite = false
        ),
        Product(
            id = 9,
            name = "Semillas de Sésamo Integral Tostado",
            category = "Semillas",
            price = 4500.0,
            stock = 20,
            unit = "kg",
            description = "Semillas de sésamo con ligero tostado que potencia su aroma a nuez. Excelente para ensaladas, panes y cocina oriental.",
            ingredients = "100% Semillas de sésamo integral tostado.",
            origin = "Chile",
            barcode = "780998810009",
            imageUrl = "",
            isFavorite = false
        ),
        Product(
            id = 10,
            name = "Arándanos Deshidratados",
            category = "Deshidratados",
            price = 12800.0,
            stock = 18,
            unit = "kg",
            description = "Arándanos rojos/azules enteros deshidratados con infusión de jugo de manzana natural para conservar su frescura y suavidad.",
            ingredients = "Arándanos seleccionados (70%), concentrado de jugo de manzana (29%), aceite de girasol (1%).",
            origin = "Región de Los Lagos, Chile",
            barcode = "780998810010",
            imageUrl = "",
            isFavorite = true
        ),
        Product(
            id = 11,
            name = "Pasas Morenas Sultanas",
            category = "Deshidratados",
            price = 3900.0,
            stock = 35,
            unit = "kg",
            description = "Pasas morenas grandes y carnosas sin pepa. Dulzura concentrada natural para repostería, cereales o consumo directo.",
            ingredients = "Uvas sultanas secadas al sol, aceite vegetal de recubrimiento (<0.5%).",
            origin = "Copiapó, Valle de Huasco, Chile",
            barcode = "780998810011",
            imageUrl = "",
            isFavorite = false
        ),
        Product(
            id = 12,
            name = "Dátiles Medjool Premium",
            category = "Deshidratados",
            price = 15900.0,
            stock = 5, // Stock bajo
            unit = "kg",
            description = "Dátiles tipo Medjool gigantes, conocidos como el rey de los dátiles. Dulzor similar al caramelo, textura jugosa y suave.",
            ingredients = "100% Dátiles Medjool con carozo sin azúcar añadida.",
            origin = "Valle de Arava (Importación exclusiva OYL)",
            barcode = "780998810012",
            imageUrl = "",
            isFavorite = true
        ),
        Product(
            id = 13,
            name = "Mix Energía & Fitness OYL",
            category = "Mixes Especiales",
            price = 10900.0,
            stock = 28,
            unit = "kg",
            description = "Fórmula exclusiva desarrollada por nutricionistas de OYL Frutos Secos. Proporción balanceada para antes o después de entrenar.",
            ingredients = "Almendras tostadas (25%), Nueces mariposa (25%), Castañas de cajú (20%), Arándanos deshidratados (15%), Pasas rubias (15%).",
            origin = "Elaborado en Santiago de Chile",
            barcode = "780998810013",
            imageUrl = "",
            isFavorite = true
        ),
        Product(
            id = 14,
            name = "Mix Estudiante & Memoria",
            category = "Mixes Especiales",
            price = 9500.0,
            stock = 22,
            unit = "kg",
            description = "Mix equilibrado con alta concentración de antioxidantes y fósforo para potenciar la concentración durante jornadas de estudio.",
            ingredients = "Nueces Chandler (30%), Almendras enteras (30%), Semillas de calabaza (20%), Maní tostado (20%).",
            origin = "Elaborado en Santiago de Chile",
            barcode = "780998810014",
            imageUrl = "",
            isFavorite = false
        )
    )

    val deliveryZones = listOf(
        DeliveryZone(
            id = "stgo_centro",
            name = "Santiago Centro & Providencia",
            fee = 2500.0,
            estimatedTime = "Mismo día (2 - 4 horas)",
            description = "Despacho prioritario motorizado para Santiago Centro, Providencia y Recoleta Sur.",
            status = "Operativo"
        ),
        DeliveryZone(
            id = "oriente",
            name = "Sector Oriente (Las Condes, Vitacura, Lo Barnechea)",
            fee = 3500.0,
            estimatedTime = "Mismo día (3 - 5 horas)",
            description = "Entregas en Las Condes, Vitacura, La Reina y Lo Barnechea.",
            status = "Operativo"
        ),
        DeliveryZone(
            id = "sur_poniente",
            name = "Sector Sur y Poniente (Maipú, Pudahuel, San Miguel)",
            fee = 3900.0,
            estimatedTime = "24 horas hábiles",
            description = "Despacho express a comunas del cordón Américo Vespucio Sur y Poniente.",
            status = "Operativo"
        ),
        DeliveryZone(
            id = "retiro_local",
            name = "Retiro en Casa Matriz OYL (Providencia)",
            fee = 0.0,
            estimatedTime = "Inmediato (Lunes a Viernes 09:00 a 19:00 hrs)",
            description = "Retiro gratuito sin costo en Av. Providencia #1820, Oficina 402.",
            status = "Gratuito"
        )
    )
}
