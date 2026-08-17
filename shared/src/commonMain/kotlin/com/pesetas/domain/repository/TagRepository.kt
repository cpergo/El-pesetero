package com.pesetas.domain.repository

import com.pesetas.domain.model.CategorySpending
import com.pesetas.domain.model.Tag
import com.pesetas.domain.model.TagSpending
import kotlinx.coroutines.flow.Flow
import com.pesetas.util.*

interface TagRepository {
    fun observeTags(): Flow<List<Tag>>
    fun observeTransactionIdsByTag(): Flow<Map<Long, Set<Long>>>
    fun observeTagExpenseTotals(from: YearMonth, to: YearMonth): Flow<List<TagSpending>>
    fun observeTagCategoryBreakdown(tagId: Long, from: YearMonth, to: YearMonth): Flow<List<CategorySpending>>
    suspend fun getTagIdsForTransaction(transactionId: Long): List<Long>
    suspend fun setTagsForTransaction(transactionId: Long, tagIds: List<Long>)
    suspend fun createTag(name: String, colorArgb: Int): Long
    suspend fun delete(tag: Tag)
}
