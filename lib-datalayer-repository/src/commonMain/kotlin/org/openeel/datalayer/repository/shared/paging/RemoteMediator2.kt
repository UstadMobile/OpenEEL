package world.respect.datalayer.repository.shared.paging

import androidx.paging.PagingSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import world.respect.datalayer.shared.paging.copyWithLoadSize

class RemoteMediator2(
    val onRemoteLoad: suspend (PagingSource.LoadParams<Int>) -> Unit,
) {

    val scope = CoroutineScope(Dispatchers.Default + Job())

    fun onLocalLoad(
        loadParams: PagingSource.LoadParams<Int>,
    ) {
        scope.launch {
            onRemoteLoad(loadParams.copyWithLoadSize { it + 50 } )
        }
    }

}