package com.pesetas.data.mapper

import com.pesetas.data.local.entity.AccountEntity
import com.pesetas.data.local.entity.BudgetEntity
import com.pesetas.data.local.entity.CategoryEntity
import com.pesetas.data.local.entity.RecurringTransactionEntity
import com.pesetas.data.local.entity.SavingsGoalEntity
import com.pesetas.data.local.entity.TransactionEntity
import com.pesetas.domain.model.Account
import com.pesetas.domain.model.Budget
import com.pesetas.domain.model.Category
import com.pesetas.domain.model.RecurringTransaction
import com.pesetas.domain.model.SavingsGoal
import com.pesetas.domain.model.Transaction
import java.time.LocalDate

fun AccountEntity.toDomain() = Account(
    id = id,
    name = name,
    iconKey = iconKey,
    colorArgb = colorArgb,
    initialBalance = initialBalance,
    position = position,
    currency = currency,
)

fun Account.toEntity() = AccountEntity(
    id = id,
    name = name,
    iconKey = iconKey,
    colorArgb = colorArgb,
    initialBalance = initialBalance,
    position = position,
    currency = currency,
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
    receiptImagePath = receiptImagePath,
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
    receiptImagePath = receiptImagePath,
)

fun BudgetEntity.toDomain() = Budget(
    categoryId = categoryId,
    monthlyLimit = monthlyLimit,
)

fun Budget.toEntity() = BudgetEntity(
    categoryId = categoryId,
    monthlyLimit = monthlyLimit,
)

fun RecurringTransactionEntity.toDomain() = RecurringTransaction(
    id = id,
    amount = amount,
    type = type,
    categoryId = categoryId,
    accountId = accountId,
    note = note,
    dayOfMonth = dayOfMonth,
    lastGeneratedDate = LocalDate.ofEpochDay(lastGeneratedEpochDay),
)

fun RecurringTransaction.toEntity() = RecurringTransactionEntity(
    id = id,
    amount = amount,
    type = type,
    categoryId = categoryId,
    accountId = accountId,
    note = note,
    dayOfMonth = dayOfMonth,
    lastGeneratedEpochDay = lastGeneratedDate.toEpochDay(),
)

fun SavingsGoalEntity.toDomain() = SavingsGoal(
    id = id,
    name = name,
    targetAmount = targetAmount,
    currentAmount = currentAmount,
    deadline = deadlineEpochDay?.let { LocalDate.ofEpochDay(it) },
    iconKey = iconKey,
    colorArgb = colorArgb,
)

fun SavingsGoal.toEntity() = SavingsGoalEntity(
    id = id,
    name = name,
    targetAmount = targetAmount,
    currentAmount = currentAmount,
    deadlineEpochDay = deadline?.toEpochDay(),
    iconKey = iconKey,
    colorArgb = colorArgb,
)
