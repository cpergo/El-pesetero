package com.pesetas.ui.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pesetas.domain.model.Account
import com.pesetas.domain.model.AccountBalance
import com.pesetas.domain.model.CombinedBalance
import com.pesetas.domain.model.combineBalances
import com.pesetas.domain.repository.AccountRepository
import com.pesetas.domain.repository.CurrencyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AccountsUiState(
    val isLoading: Boolean = true,
    val accounts: List<AccountBalance> = emptyList(),
    val combinedBalance: CombinedBalance = CombinedBalance(0.0, "EUR", false, null, false),
)

@HiltViewModel
class AccountsViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    currencyRepository: CurrencyRepository,
) : ViewModel() {

    private val _messages = MutableSharedFlow<String>()
    val messages = _messages.asSharedFlow()

    val uiState = combine(
        accountRepository.observeAccountBalances(),
        currencyRepository.observeRates(),
        currencyRepository.mainCurrency,
    ) { balances, rates, main ->
        AccountsUiState(
            isLoading = false,
            accounts = balances,
            combinedBalance = combineBalances(
                balancesByCurrency = balances.map { it.account.currency to it.balance },
                mainCurrency = main,
                rates = rates.associateBy { it.currencyCode },
            ),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AccountsUiState(),
    )

    fun moveUp(account: Account) = move(account, -1)

    fun moveDown(account: Account) = move(account, 1)

    private fun move(account: Account, offset: Int) {
        val current = uiState.value.accounts.map { it.account }
        val index = current.indexOfFirst { it.id == account.id }
        val target = index + offset
        if (index < 0 || target < 0 || target >= current.size) return
        val reordered = current.toMutableList().apply {
            add(target, removeAt(index))
        }
        viewModelScope.launch {
            accountRepository.reorder(reordered.map { it.id })
        }
    }

    fun delete(account: Account) {
        viewModelScope.launch {
            when {
                accountRepository.count() <= 1 ->
                    _messages.emit("Debe existir al menos una cuenta")
                accountRepository.isInUse(account.id) ->
                    _messages.emit("No puedes borrar «${account.name}»: tiene movimientos asociados")
                else -> accountRepository.delete(account)
            }
        }
    }
}
