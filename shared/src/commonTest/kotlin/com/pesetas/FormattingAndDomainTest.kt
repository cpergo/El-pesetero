package com.pesetas

import com.pesetas.data.backup.CsvBuilder
import com.pesetas.domain.model.ExchangeRate
import com.pesetas.domain.model.combineBalances
import com.pesetas.ui.util.formatDate
import com.pesetas.ui.util.formatMoney
import com.pesetas.ui.util.formatMonth
import com.pesetas.util.LocalDate
import com.pesetas.util.YearMonth
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FormattingAndDomainTest {
    @Test
    fun spanishFormattingIsDeterministicOnAndroidAndIos() {
        assertEquals("1.234.567,50\u00A0€", formatMoney(1_234_567.5))
        assertEquals("-42,10\u00A0US$", formatMoney(-42.1, "USD"))
        assertEquals("2 ago 2026", formatDate(LocalDate(2026, 8, 2)))
        assertEquals("Agosto 2026", formatMonth(YearMonth(2026, 8)))
    }

    @Test
    fun csvEscapesCommasQuotesAndLineBreaks() {
        assertEquals(
            "normal,\"con,coma\",\"dice \"\"hola\"\"\",\"dos\nlíneas\"",
            CsvBuilder.row(listOf("normal", "con,coma", "dice \"hola\"", "dos\nlíneas")),
        )
    }

    @Test
    fun currencyCombinationPreservesExistingMissingRateBehaviour() {
        val result = combineBalances(
            balancesByCurrency = listOf("EUR" to 100.0, "USD" to 20.0, "GBP" to 10.0),
            mainCurrency = "EUR",
            rates = mapOf(
                "USD" to ExchangeRate("USD", 0.9, LocalDate(2026, 7, 1)),
            ),
        )
        assertEquals(118.0, result.total)
        assertTrue(result.isApproximate)
        assertTrue(result.hasMissingRates)
        assertEquals(LocalDate(2026, 7, 1), result.oldestRateDate)
        assertFalse(result.mainCurrency.isBlank())
    }
}
