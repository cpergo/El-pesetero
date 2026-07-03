package com.pesetas.data.local

import androidx.room.TypeConverter
import com.pesetas.domain.model.CategoryType
import com.pesetas.domain.model.TransactionType

class Converters {
    @TypeConverter
    fun transactionTypeToString(type: TransactionType): String = type.name

    @TypeConverter
    fun stringToTransactionType(value: String): TransactionType = TransactionType.valueOf(value)

    @TypeConverter
    fun categoryTypeToString(type: CategoryType): String = type.name

    @TypeConverter
    fun stringToCategoryType(value: String): CategoryType = CategoryType.valueOf(value)
}
