package com.pesetas.domain.model

data class Account(
    val id: Long = 0,
    val name: String,
    val iconKey: String,
    val colorArgb: Int,
    val initialBalance: Double,
    val position: Int = 0,
)
