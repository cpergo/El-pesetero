package com.pesetas.data.repository

import com.pesetas.data.local.dao.CategoryDao
import com.pesetas.data.local.dao.TagDao
import com.pesetas.data.local.entity.TagEntity
import com.pesetas.data.mapper.toDomain
import com.pesetas.domain.model.CategorySpending
import com.pesetas.domain.model.Tag
import com.pesetas.domain.model.TagSpending
import com.pesetas.domain.repository.TagRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import com.pesetas.util.*

class TagRepositoryImpl(
    private val tagDao: TagDao,
    private val categoryDao: CategoryDao,
) : TagRepository {

    override fun observeTags(): Flow<List<Tag>> =
        tagDao.observeAll().map { list -> list.map { Tag(it.id, it.name, it.colorArgb) } }

    override fun observeTransactionIdsByTag(): Flow<Map<Long, Set<Long>>> =
        tagDao.observeAllRefs().map { refs ->
            refs.groupBy({ it.tagId }, { it.transactionId }).mapValues { it.value.toSet() }
        }

    override fun observeTagExpenseTotals(from: YearMonth, to: YearMonth): Flow<List<TagSpending>> =
        combine(
            tagDao.observeTagExpenseTotals(
                from.atDay(1).toEpochDay(),
                to.atEndOfMonth().toEpochDay(),
            ),
            tagDao.observeAll(),
        ) { totals, tags ->
            val tagById = tags.associateBy { it.id }
            totals.mapNotNull { row ->
                tagById[row.tagId]?.let { tag ->
                    TagSpending(Tag(tag.id, tag.name, tag.colorArgb), row.total)
                }
            }
        }

    override fun observeTagCategoryBreakdown(
        tagId: Long,
        from: YearMonth,
        to: YearMonth,
    ): Flow<List<CategorySpending>> =
        combine(
            tagDao.observeTagCategoryTotals(
                tagId,
                from.atDay(1).toEpochDay(),
                to.atEndOfMonth().toEpochDay(),
            ),
            categoryDao.observeAll(),
        ) { totals, categories ->
            val categoryById = categories.associateBy { it.id }
            totals.mapNotNull { row ->
                row.categoryId?.let { categoryById[it] }?.let { category ->
                    CategorySpending(category.toDomain(), row.total)
                }
            }
        }

    override suspend fun getTagIdsForTransaction(transactionId: Long): List<Long> =
        tagDao.getTagIdsForTransaction(transactionId)

    override suspend fun setTagsForTransaction(transactionId: Long, tagIds: List<Long>) =
        tagDao.setTagsForTransaction(transactionId, tagIds)

    override suspend fun createTag(name: String, colorArgb: Int): Long =
        tagDao.upsert(TagEntity(name = name.trim(), colorArgb = colorArgb))

    override suspend fun delete(tag: Tag) =
        tagDao.delete(TagEntity(tag.id, tag.name, tag.colorArgb))
}
