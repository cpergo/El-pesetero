package com.pesetas.domain.model

data class Category(
    val id: Long = 0,
    val name: String,
    val iconKey: String,
    val colorArgb: Int,
    val type: CategoryType,
    val position: Int = 0,
)
