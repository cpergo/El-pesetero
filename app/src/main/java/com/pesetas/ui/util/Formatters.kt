package com.pesetas.ui.util

import java.text.NumberFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private val locale = Locale("es", "ES")
private val currencyFormat: NumberFormat = NumberFormat.getCurrencyInstance(locale)
private val dayFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM yyyy", locale)
private val dayShortFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE d MMM", locale)

fun formatMoney(amount: Double): String = currencyFormat.format(amount)

fun formatSignedMoney(amount: Double, positive: Boolean): String {
    val prefix = if (positive) "+" else "-"
    return prefix + currencyFormat.format(kotlin.math.abs(amount))
}

fun formatDate(date: LocalDate): String = date.format(dayFormatter)

fun formatDayHeader(date: LocalDate): String =
    date.format(dayShortFormatter).replaceFirstChar { it.uppercase(locale) }

fun formatMonth(month: YearMonth): String {
    val name = month.month.getDisplayName(TextStyle.FULL, locale)
        .replaceFirstChar { it.uppercase(locale) }
    return "$name ${month.year}"
}

fun formatMonthShort(month: YearMonth): String =
    month.month.getDisplayName(TextStyle.SHORT, locale).replaceFirstChar { it.uppercase(locale) }
