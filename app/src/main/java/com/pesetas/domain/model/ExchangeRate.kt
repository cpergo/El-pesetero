package com.pesetas.domain.model

import java.time.LocalDate

data class ExchangeRate(
    val currencyCode: String,
    val rateToMain: Double,
    val updatedDate: LocalDate,
)

data class CombinedBalance(
    val total: Double,
    val mainCurrency: String,
    val isApproximate: Boolean,
    val oldestRateDate: LocalDate?,
    val hasMissingRates: Boolean,
)

fun combineBalances(
    balancesByCurrency: List<Pair<String, Double>>,
    mainCurrency: String,
    rates: Map<String, ExchangeRate>,
): CombinedBalance {
    var total = 0.0
    var isApproximate = false
    var hasMissingRates = false
    var oldestRateDate: LocalDate? = null
    balancesByCurrency.forEach { (currency, balance) ->
        if (currency == mainCurrency) {
            total += balance
        } else {
            isApproximate = true
            val rate = rates[currency]
            if (rate == null) {
                hasMissingRates = true
            } else {
                total += balance * rate.rateToMain
                oldestRateDate = when {
                    oldestRateDate == null -> rate.updatedDate
                    rate.updatedDate.isBefore(oldestRateDate) -> rate.updatedDate
                    else -> oldestRateDate
                }
            }
        }
    }
    return CombinedBalance(
        total = total,
        mainCurrency = mainCurrency,
        isApproximate = isApproximate,
        oldestRateDate = oldestRateDate,
        hasMissingRates = hasMissingRates,
    )
}
