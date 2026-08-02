package com.pesetas.data.backup

object CsvBuilder {

    fun row(values: List<String>): String = values.joinToString(",") { escape(it) }

    private fun escape(value: String): String {
        val needsQuoting = value.contains(',') || value.contains('"') ||
            value.contains('\n') || value.contains('\r')
        return if (needsQuoting) {
            "\"" + value.replace("\"", "\"\"") + "\""
        } else {
            value
        }
    }
}
