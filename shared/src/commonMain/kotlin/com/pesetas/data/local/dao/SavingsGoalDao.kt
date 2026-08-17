package com.pesetas.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.pesetas.data.local.entity.SavingsGoalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SavingsGoalDao {

    @Query("SELECT * FROM savings_goals ORDER BY id ASC")
    fun observeAll(): Flow<List<SavingsGoalEntity>>

    @Query("SELECT * FROM savings_goals WHERE id = :id")
    suspend fun getById(id: Long): SavingsGoalEntity?

    @Upsert
    suspend fun upsert(goal: SavingsGoalEntity): Long

    @Delete
    suspend fun delete(goal: SavingsGoalEntity)

    @Query("UPDATE savings_goals SET currentAmount = currentAmount + :amount WHERE id = :id")
    suspend fun addContribution(id: Long, amount: Double)
}
