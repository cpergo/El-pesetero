package com.pesetas.data.mapper

import com.pesetas.data.local.entity.AccountEntity
import com.pesetas.data.local.entity.CategoryEntity
import com.pesetas.data.local.entity.TransactionEntity
import com.pesetas.domain.model.Account
import com.pesetas.domain.model.Category
import com.pesetas.domain.model.Transaction
import java.time.LocalDate

fun AccountEntity.toDomain() = Account(
    id = id,
    name = name,
    iconKey = iconKey,
    colorArgb = colorArgb,
    initialBalance = initialBalance,
    position = position,
)

fun Account.toEntity() = AccountEntity(
    id = id,
    name = name,
    iconKey = iconKey,
    colorArgb = colorArgb,
    initialBalance = initialBalance,
    position = position,
)

fun CategoryEntity.toDomain() = Category(
    id = id,
    name = name,
    iconKey = iconKey,
    colorArgb = colorArgb,
    type = type,
    position = position,
)

fun Category.toEntity() = CategoryEntity(
    id = id,
    name = name,
    iconKey = iconKey,
    colorArgb = colorArgb,
    type = type,
    position = position,
)

fun TransactionEntity.toDomain() = Transaction(
    id = id,
    amount = amount,
    date = LocalDate.ofEpochDay(dateEpochDay),
    type = type,
    categoryId = categoryId,
    accountId = accountId,
    transferAccountId = transferAccountId,
    note = note,
)

fun Transaction.toEntity() = TransactionEntity(
    id = id,
    amount = amount,
    dateEpochDay = date.toEpochDay(),
    type = type,
    categoryId = categoryId,
    accountId = accountId,
    transferAccountId = transferAccountId,
    note = note,
)
