package com.pesetas.domain.repository

import com.pesetas.domain.model.Account
import com.pesetas.domain.model.AccountBalance
import kotlinx.coroutines.flow.Flow

interface AccountRepository {
    fun observeAccounts(): Flow<List<Account>>
    fun observeAccountBalances(): Flow<List<AccountBalance>>
    fun observeTotalBalance(): Flow<Double>
    suspend fun getAccount(id: Long): Account?
    suspend fun upsert(account: Account): Long
    suspend fun delete(account: Account)
    suspend fun reorder(orderedIds: List<Long>)
    suspend fun isInUse(accountId: Long): Boolean
    suspend fun count(): Int
}
