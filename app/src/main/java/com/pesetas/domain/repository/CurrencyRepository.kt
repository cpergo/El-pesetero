package com.pesetas.domain.repository

import com.pesetas.domain.model.ExchangeRate
import kotlinx.coroutines.flow.Flow

interface CurrencyRepository {
    val mainCurrency: Flow<String>
    fun observeRates(): Flow<List<ExchangeRate>>
    suspend fun setMainCurrency(code: String)
    suspend fun setRate(code: String, rateToMain: Double)
    suspend fun deleteRate(code: String)
}
