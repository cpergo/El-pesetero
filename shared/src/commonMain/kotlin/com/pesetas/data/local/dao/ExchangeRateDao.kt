package com.pesetas.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.pesetas.data.local.entity.ExchangeRateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExchangeRateDao {

    @Query("SELECT * FROM exchange_rates ORDER BY currencyCode ASC")
    fun observeAll(): Flow<List<ExchangeRateEntity>>

    @Upsert
    suspend fun upsert(rate: ExchangeRateEntity)

    @Query("DELETE FROM exchange_rates WHERE currencyCode = :code")
    suspend fun deleteByCode(code: String)
}
