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

fun formatMoney(amount: Double, currencyCode: String): String {
    if (currencyCode == "EUR") return currencyFormat.format(amount)
    val format = NumberFormat.getCurrencyInstance(locale).apply {
        currency = java.util.Currency.getInstance(currencyCode)
    }
    return format.format(amount)
}

fun currencySymbol(currencyCode: String): String =
    java.util.Currency.getInstance(currencyCode).getSymbol(locale)

fun formatSignedMoney(amount: Double, positive: Boolean): String {
    val prefix = if (positive) "+" else "-"
    return prefix + currencyFormat.format(kotlin.math.abs(amount))
}

fun formatSignedMoney(amount: Double, positive: Boolean, currencyCode: String): String {
    val prefix = if (positive) "+" else "-"
    return prefix + formatMoney(kotlin.math.abs(amount), currencyCode)
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
