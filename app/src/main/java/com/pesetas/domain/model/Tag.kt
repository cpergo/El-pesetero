package com.pesetas.domain.model

data class Tag(
    val id: Long = 0,
    val name: String,
    val colorArgb: Int,
)

data class TagSpending(
    val tag: Tag,
    val total: Double,
)
