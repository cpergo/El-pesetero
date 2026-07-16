package com.pesetas.ui.currencies

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pesetas.domain.model.ExchangeRate
import com.pesetas.domain.repository.AccountRepository
import com.pesetas.domain.repository.CurrencyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ForeignCurrencyItem(
    val code: String,
    val rate: ExchangeRate?,
    val accountCount: Int,
)

data class CurrenciesUiState(
    val isLoading: Boolean = true,
    val mainCurrency: String = "EUR",
    val foreignCurrencies: List<ForeignCurrencyItem> = emptyList(),
)

@HiltViewModel
class CurrenciesViewModel @Inject constructor(
    accountRepository: AccountRepository,
    private val currencyRepository: CurrencyRepository,
) : ViewModel() {

    val uiState = combine(
        accountRepository.observeAccounts(),
        currencyRepository.observeRates(),
        currencyRepository.mainCurrency,
    ) { accounts, rates, main ->
        val rateByCode = rates.associateBy { it.currencyCode }
        val usedForeign = accounts
            .groupBy { it.currency }
            .filterKeys { it != main }
        val codes = (usedForeign.keys + rateByCode.keys.filter { it != main }).distinct().sorted()
        CurrenciesUiState(
            isLoading = false,
            mainCurrency = main,
            foreignCurrencies = codes.map { code ->
                ForeignCurrencyItem(
                    code = code,
                    rate = rateByCode[code],
                    accountCount = usedForeign[code]?.size ?: 0,
                )
            },
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CurrenciesUiState(),
    )

    fun setMainCurrency(code: String) {
        viewModelScope.launch { currencyRepository.setMainCurrency(code) }
    }

    fun setRate(code: String, rate: Double) {
        if (rate <= 0) return
        viewModelScope.launch { currencyRepository.setRate(code, rate) }
    }
}
