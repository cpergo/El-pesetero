package com.pesetas.data.repository

import com.pesetas.data.local.dao.CategoryDao
import com.pesetas.data.mapper.toDomain
import com.pesetas.data.mapper.toEntity
import com.pesetas.domain.model.Category
import com.pesetas.domain.model.CategoryType
import com.pesetas.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CategoryRepositoryImpl(
    private val categoryDao: CategoryDao,
) : CategoryRepository {

    override fun observeCategories(): Flow<List<Category>> =
        categoryDao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeCategories(type: CategoryType): Flow<List<Category>> =
        categoryDao.observeByType(type.name).map { list -> list.map { it.toDomain() } }

    override suspend fun getCategory(id: Long): Category? = categoryDao.getById(id)?.toDomain()

    override suspend fun upsert(category: Category): Long {
        val prepared = if (category.id == 0L) {
            category.copy(position = categoryDao.nextPosition())
        } else {
            category
        }
        return categoryDao.upsert(prepared.toEntity())
    }

    override suspend fun delete(category: Category) = categoryDao.delete(category.toEntity())

    override suspend fun reorder(orderedIds: List<Long>) = categoryDao.reorder(orderedIds)

    override suspend fun isInUse(categoryId: Long): Boolean =
        categoryDao.transactionCount(categoryId) > 0
}
