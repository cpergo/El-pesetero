package com.pesetas.domain.model

data class TransactionDetails(
    val transaction: Transaction,
    val category: Category?,
    val account: Account,
    val transferAccount: Account?,
)
