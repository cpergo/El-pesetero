package com.pesetas.ui.util

import com.pesetas.util.*
import kotlin.math.abs
import kotlin.math.roundToLong

private val monthNames = listOf(
    "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
    "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre",
)
private val monthNamesShort = listOf(
    "Ene", "Feb", "Mar", "Abr", "May", "Jun",
    "Jul", "Ago", "Sept", "Oct", "Nov", "Dic",
)
private val weekDaysShort = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")

private val currencySymbols = mapOf(
    "EUR" to "€",
    "USD" to "US$",
    "GBP" to "GBP",
    "CHF" to "CHF",
    "JPY" to "JPY",
    "CNY" to "CNY",
    "CAD" to "CAD",
    "AUD" to "AUD",
    "MXN" to "MXN",
    "ARS" to "ARS",
    "BRL" to "BRL",
    "CLP" to "CLP",
    "COP" to "COP",
    "PEN" to "PEN",
    "UYU" to "UYU",
    "MAD" to "MAD",
)

fun formatMoney(amount: Double): String = formatMoney(amount, "EUR")

fun formatMoney(amount: Double, currencyCode: String): String =
    "${formatSpanishDecimal(amount)}\u00A0${currencySymbol(currencyCode)}"

fun currencySymbol(currencyCode: String): String = currencySymbols[currencyCode] ?: currencyCode

fun formatSignedMoney(amount: Double, positive: Boolean): String =
    formatSignedMoney(amount, positive, "EUR")

fun formatSignedMoney(amount: Double, positive: Boolean, currencyCode: String): String {
    val prefix = if (positive) "+" else "-"
    return prefix + formatMoney(abs(amount), currencyCode)
}

fun formatDate(date: LocalDate): String =
    "${date.day} ${monthNamesShort[date.month.ordinal].lowercase()} ${date.year}"

fun formatDayHeader(date: LocalDate): String =
    "${weekDaysShort[date.dayOfWeek.ordinal]} ${date.day} " +
        monthNamesShort[date.month.ordinal].lowercase()

fun formatMonth(month: YearMonth): String = "${monthNames[month.month.ordinal]} ${month.year}"

fun formatMonthShort(month: YearMonth): String = monthNamesShort[month.month.ordinal]

private fun formatSpanishDecimal(amount: Double): String {
    if (!amount.isFinite()) return amount.toString()
    val negative = amount < 0
    val cents = (abs(amount) * 100.0).roundToLong()
    val integer = (cents / 100).toString()
    val grouped = integer.reversed().chunked(3).joinToString(".").reversed()
    val decimals = (cents % 100).toString().padStart(2, '0')
    return (if (negative) "-" else "") + grouped + "," + decimals
}
