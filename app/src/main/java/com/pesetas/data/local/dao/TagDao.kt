package com.pesetas.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.pesetas.data.local.entity.TagEntity
import com.pesetas.data.local.entity.TransactionTagCrossRef
import kotlinx.coroutines.flow.Flow

data class TagTotalRow(
    val tagId: Long,
    val total: Double,
)

data class CategoryTotalRow(
    val categoryId: Long?,
    val total: Double,
)

@Dao
interface TagDao {

    @Query("SELECT * FROM tags ORDER BY name COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<TagEntity>>

    @Upsert
    suspend fun upsert(tag: TagEntity): Long

    @Delete
    suspend fun delete(tag: TagEntity)

    @Query("SELECT tagId FROM transaction_tags WHERE transactionId = :transactionId")
    suspend fun getTagIdsForTransaction(transactionId: Long): List<Long>

    @Query("SELECT * FROM transaction_tags")
    fun observeAllRefs(): Flow<List<TransactionTagCrossRef>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertRefs(refs: List<TransactionTagCrossRef>)

    @Query("DELETE FROM transaction_tags WHERE transactionId = :transactionId")
    suspend fun deleteRefsForTransaction(transactionId: Long)

    @Transaction
    suspend fun setTagsForTransaction(transactionId: Long, tagIds: List<Long>) {
        deleteRefsForTransaction(transactionId)
        if (tagIds.isNotEmpty()) {
            insertRefs(tagIds.map { TransactionTagCrossRef(transactionId, it) })
        }
    }

    @Query(
        """
        SELECT tt.tagId AS tagId, SUM(tr.amount) AS total
        FROM transaction_tags tt
        JOIN transactions tr ON tr.id = tt.transactionId
        WHERE tr.type = 'EXPENSE' AND tr.dateEpochDay BETWEEN :from AND :to
        GROUP BY tt.tagId
        ORDER BY total DESC
        """,
    )
    fun observeTagExpenseTotals(from: Long, to: Long): Flow<List<TagTotalRow>>

    @Query(
        """
        SELECT tr.categoryId AS categoryId, SUM(tr.amount) AS total
        FROM transaction_tags tt
        JOIN transactions tr ON tr.id = tt.transactionId
        WHERE tt.tagId = :tagId AND tr.type = 'EXPENSE' AND tr.dateEpochDay BETWEEN :from AND :to
        GROUP BY tr.categoryId
        ORDER BY total DESC
        """,
    )
    fun observeTagCategoryTotals(tagId: Long, from: Long, to: Long): Flow<List<CategoryTotalRow>>
}
