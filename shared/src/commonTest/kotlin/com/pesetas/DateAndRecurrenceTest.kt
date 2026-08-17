package com.pesetas

import com.pesetas.data.repository.recurringOccurrencesBetween
import com.pesetas.util.LocalDate
import com.pesetas.util.YearMonth
import com.pesetas.util.atEndOfMonth
import com.pesetas.util.localDateFromEpochDay
import com.pesetas.util.minusMonths
import com.pesetas.util.plusMonths
import com.pesetas.util.toEpochDay
import kotlin.test.Test
import kotlin.test.assertEquals

class DateAndRecurrenceTest {
    @Test
    fun epochDayRoundTripsWithoutTimezoneShift() {
        val dates = listOf(
            LocalDate(1970, 1, 1),
            LocalDate(2000, 2, 29),
            LocalDate(2026, 8, 2),
            LocalDate(1969, 12, 31),
        )
        dates.forEach { date -> assertEquals(date, localDateFromEpochDay(date.toEpochDay())) }
        assertEquals(0L, LocalDate(1970, 1, 1).toEpochDay())
    }

    @Test
    fun yearMonthArithmeticAndMonthEndMatchJavaTimeSemantics() {
        val january = YearMonth(2024, 1)
        assertEquals(YearMonth(2024, 2), january.plusMonths(1))
        assertEquals(YearMonth(2023, 12), january.minusMonths(1))
        assertEquals(LocalDate(2024, 2, 29), january.plusMonths(1).atEndOfMonth())
    }

    @Test
    fun monthlyRecurrenceClampsToLastDayAndBackfillsAllMonths() {
        assertEquals(
            listOf(
                LocalDate(2024, 2, 29),
                LocalDate(2024, 3, 31),
                LocalDate(2024, 4, 30),
            ),
            recurringOccurrencesBetween(
                lastGenerated = LocalDate(2024, 1, 31),
                today = LocalDate(2024, 4, 30),
                dayOfMonth = 31,
            ),
        )
    }
}
