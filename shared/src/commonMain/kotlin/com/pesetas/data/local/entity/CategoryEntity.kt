package com.pesetas.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.pesetas.domain.model.CategoryType

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val iconKey: String,
    val colorArgb: Int,
    val type: CategoryType,
    val position: Int,
)
