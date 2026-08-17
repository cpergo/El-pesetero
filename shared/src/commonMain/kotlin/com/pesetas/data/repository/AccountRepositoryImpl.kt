package com.pesetas.data.repository

import com.pesetas.data.local.dao.AccountDao
import com.pesetas.data.mapper.toDomain
import com.pesetas.data.mapper.toEntity
import com.pesetas.domain.model.Account
import com.pesetas.domain.model.AccountBalance
import com.pesetas.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AccountRepositoryImpl(
    private val accountDao: AccountDao,
) : AccountRepository {

    override fun observeAccounts(): Flow<List<Account>> =
        accountDao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeAccountBalances(): Flow<List<AccountBalance>> =
        accountDao.observeBalances().map { list ->
            list.map { AccountBalance(it.account.toDomain(), it.balance) }
        }

    override fun observeTotalBalance(): Flow<Double> = accountDao.observeTotalBalance()

    override suspend fun getAccount(id: Long): Account? = accountDao.getById(id)?.toDomain()

    override suspend fun upsert(account: Account): Long {
        val prepared = if (account.id == 0L) {
            account.copy(position = accountDao.nextPosition())
        } else {
            account
        }
        return accountDao.upsert(prepared.toEntity())
    }

    override suspend fun delete(account: Account) = accountDao.delete(account.toEntity())

    override suspend fun reorder(orderedIds: List<Long>) = accountDao.reorder(orderedIds)

    override suspend fun isInUse(accountId: Long): Boolean = accountDao.transactionCount(accountId) > 0

    override suspend fun count(): Int = accountDao.count()
}
