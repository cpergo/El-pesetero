package com.pesetas.domain.repository

import com.pesetas.domain.model.Category
import com.pesetas.domain.model.CategoryType
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun observeCategories(): Flow<List<Category>>
    fun observeCategories(type: CategoryType): Flow<List<Category>>
    suspend fun getCategory(id: Long): Category?
    suspend fun upsert(category: Category): Long
    suspend fun delete(category: Category)
    suspend fun reorder(orderedIds: List<Long>)
    suspend fun isInUse(categoryId: Long): Boolean
}
