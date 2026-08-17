package com.pesetas.data.local

import com.pesetas.domain.model.CategoryType

object DefaultData {

    data class SeedAccount(
        val name: String,
        val iconKey: String,
        val colorArgb: Int,
        val initialBalance: Double,
        val position: Int,
    )

    data class SeedCategory(
        val name: String,
        val iconKey: String,
        val colorArgb: Int,
        val type: CategoryType,
        val position: Int,
    )

    val accounts = listOf(
        SeedAccount("Efectivo", "payments", 0xFF6FBF8C.toInt(), 0.0, 0),
        SeedAccount("Banco", "account_balance", 0xFF2D5F4C.toInt(), 0.0, 1),
        SeedAccount("Tarjeta", "credit_card", 0xFFC89B3C.toInt(), 0.0, 2),
    )

    val categories = listOf(
        SeedCategory("Comida", "restaurant", 0xFFB4432E.toInt(), CategoryType.EXPENSE, 0),
        SeedCategory("Transporte", "directions_car", 0xFF3F72AF.toInt(), CategoryType.EXPENSE, 1),
        SeedCategory("Hogar", "home", 0xFF8A6D3B.toInt(), CategoryType.EXPENSE, 2),
        SeedCategory("Ocio", "sports_esports", 0xFFC0619E.toInt(), CategoryType.EXPENSE, 3),
        SeedCategory("Salud", "medical_services", 0xFF2D9596.toInt(), CategoryType.EXPENSE, 4),
        SeedCategory("Compras", "shopping_cart", 0xFFE08B3B.toInt(), CategoryType.EXPENSE, 5),
        SeedCategory("Facturas", "receipt_long", 0xFF6D597A.toInt(), CategoryType.EXPENSE, 6),
        SeedCategory("Educación", "school", 0xFF4C7A5B.toInt(), CategoryType.EXPENSE, 7),
        SeedCategory("Nómina", "work", 0xFF6FBF8C.toInt(), CategoryType.INCOME, 8),
        SeedCategory("Regalos", "card_giftcard", 0xFFC89B3C.toInt(), CategoryType.INCOME, 9),
        SeedCategory("Ahorro", "savings", 0xFF2D5F4C.toInt(), CategoryType.INCOME, 10),
        SeedCategory("Otros", "attach_money", 0xFF8A6D3B.toInt(), CategoryType.INCOME, 11),
    )
}
