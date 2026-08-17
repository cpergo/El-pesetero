package com.pesetas.ui.util

object CurrencyCatalog {
    val currencies: List<Pair<String, String>> = listOf(
        "EUR" to "Euro",
        "USD" to "Dólar estadounidense",
        "GBP" to "Libra esterlina",
        "CHF" to "Franco suizo",
        "JPY" to "Yen japonés",
        "CNY" to "Yuan chino",
        "CAD" to "Dólar canadiense",
        "AUD" to "Dólar australiano",
        "MXN" to "Peso mexicano",
        "ARS" to "Peso argentino",
        "BRL" to "Real brasileño",
        "CLP" to "Peso chileno",
        "COP" to "Peso colombiano",
        "PEN" to "Sol peruano",
        "UYU" to "Peso uruguayo",
        "MAD" to "Dírham marroquí",
    )

    fun nameFor(code: String): String =
        currencies.firstOrNull { it.first == code }?.second ?: code
}
