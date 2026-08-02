package com.pesetas.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.pesetas.data.local.dao.ExchangeRateDao
import com.pesetas.data.local.entity.ExchangeRateEntity
import com.pesetas.domain.model.ExchangeRate
import com.pesetas.domain.repository.CurrencyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.pesetas.util.*

class CurrencyRepositoryImpl(
    private val exchangeRateDao: ExchangeRateDao,
    private val dataStore: DataStore<Preferences>,
) : CurrencyRepository {

    override val mainCurrency: Flow<String> = dataStore.data.map { preferences ->
        preferences[MAIN_CURRENCY] ?: "EUR"
    }

    override fun observeRates(): Flow<List<ExchangeRate>> =
        exchangeRateDao.observeAll().map { list ->
            list.map {
                ExchangeRate(
                    currencyCode = it.currencyCode,
                    rateToMain = it.rateToMain,
                    updatedDate = localDateFromEpochDay(it.updatedEpochDay),
                )
            }
        }

    override suspend fun setMainCurrency(code: String) {
        dataStore.edit { it[MAIN_CURRENCY] = code }
    }

    override suspend fun setRate(code: String, rateToMain: Double) {
        exchangeRateDao.upsert(
            ExchangeRateEntity(
                currencyCode = code,
                rateToMain = rateToMain,
                updatedEpochDay = currentDate().toEpochDay(),
            ),
        )
    }

    override suspend fun deleteRate(code: String) {
        exchangeRateDao.deleteByCode(code)
    }

    private companion object {
        val MAIN_CURRENCY = stringPreferencesKey("main_currency")
    }
}
