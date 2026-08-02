package com.pesetas.util

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.onDay
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.yearMonth
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

typealias LocalDate = kotlinx.datetime.LocalDate
typealias YearMonth = kotlinx.datetime.YearMonth

@OptIn(ExperimentalTime::class)
fun currentDate(): LocalDate =
    Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

fun currentYearMonth(): YearMonth = currentDate().yearMonth

fun localDateFromEpochDay(epochDay: Long): LocalDate =
    kotlinx.datetime.LocalDate.fromEpochDays(epochDay.toInt())

fun LocalDate.toEpochDay(): Long = toEpochDays().toLong()

fun yearMonthOf(date: LocalDate): YearMonth = date.yearMonth

fun YearMonth.atDay(dayOfMonth: Int): LocalDate = onDay(dayOfMonth)

fun YearMonth.atEndOfMonth(): LocalDate = onDay(numberOfDays)

fun YearMonth.lengthOfMonth(): Int = numberOfDays

fun YearMonth.plusMonths(months: Long): YearMonth = plus(months, DateTimeUnit.MONTH)

fun YearMonth.minusMonths(months: Long): YearMonth = minus(months, DateTimeUnit.MONTH)

fun YearMonth.isAfter(other: YearMonth): Boolean = this > other

fun LocalDate.plusMonths(months: Long): LocalDate = plus(months, DateTimeUnit.MONTH)

fun LocalDate.isAfter(other: LocalDate): Boolean = this > other

fun LocalDate.isBefore(other: LocalDate): Boolean = this < other
