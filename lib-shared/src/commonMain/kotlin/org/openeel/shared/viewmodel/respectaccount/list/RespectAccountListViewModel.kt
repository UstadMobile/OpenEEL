package org.openeel.shared.viewmodel.respectaccount.list

import org.openeel.shared.domain.account.RespectAccount
import org.openeel.shared.domain.account.RespectAccountManager

data class AccountAndName(
    val account: RespectAccount,
    val name: String,
)

class RespectAccountListUiState(
    val accounts: List<AccountAndName>,
)

class RespectAccountListViewModel(
    private val respectAccountManager: RespectAccountManager,
) {




}