package com.pesetas.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.pesetas.data.local.entity.AccountEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {

    @Query("SELECT * FROM accounts ORDER BY position ASC, id ASC")
    fun observeAll(): Flow<List<AccountEntity>>

    @Query(
        """
        SELECT a.*,
            a.initialBalance
            + COALESCE((SELECT SUM(amount) FROM transactions WHERE accountId = a.id AND type = 'INCOME'), 0)
            - COALESCE((SELECT SUM(amount) FROM transactions WHERE accountId = a.id AND type = 'EXPENSE'), 0)
            - COALESCE((SELECT SUM(amount) FROM transactions WHERE accountId = a.id AND type = 'TRANSFER'), 0)
            + COALESCE((SELECT SUM(amount) FROM transactions WHERE transferAccountId = a.id AND type = 'TRANSFER'), 0)
            AS balance
        FROM accounts a
        ORDER BY a.position ASC, a.id ASC
        """,
    )
    fun observeBalances(): Flow<List<AccountWithBalance>>

    @Query(
        """
        SELECT
            (SELECT COALESCE(SUM(initialBalance), 0) FROM accounts)
            + COALESCE((SELECT SUM(amount) FROM transactions WHERE type = 'INCOME'), 0)
            - COALESCE((SELECT SUM(amount) FROM transactions WHERE type = 'EXPENSE'), 0)
        """,
    )
    fun observeTotalBalance(): Flow<Double>

    @Query("SELECT * FROM accounts WHERE id = :id")
    suspend fun getById(id: Long): AccountEntity?

    @Query("SELECT COALESCE(MAX(position), -1) + 1 FROM accounts")
    suspend fun nextPosition(): Int

    @Query("SELECT COUNT(*) FROM transactions WHERE accountId = :accountId OR transferAccountId = :accountId")
    suspend fun transactionCount(accountId: Long): Int

    @Query("SELECT COUNT(*) FROM accounts")
    suspend fun count(): Int

    @Upsert
    suspend fun upsert(account: AccountEntity): Long

    @Delete
    suspend fun delete(account: AccountEntity)

    @Query("UPDATE accounts SET position = :position WHERE id = :id")
    suspend fun updatePosition(id: Long, position: Int)

    @Transaction
    suspend fun reorder(orderedIds: List<Long>) {
        orderedIds.forEachIndexed { index, id -> updatePosition(id, index) }
    }
}
