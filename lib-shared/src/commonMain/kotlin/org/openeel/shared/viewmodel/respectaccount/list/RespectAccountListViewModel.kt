package world.respect.shared.viewmodel.respectaccount.list

import world.respect.shared.domain.account.RespectAccount
import world.respect.shared.domain.account.RespectAccountManager

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