package world.respect.app.view.sharedschooldevice.login

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.paging.compose.collectAsLazyPagingItems
import world.respect.app.components.RespectPersonAvatar
import world.respect.app.components.respectPagingItems
import world.respect.app.components.respectRememberPager
import world.respect.datalayer.db.school.ext.fullName
import world.respect.datalayer.school.ClassDataSource
import world.respect.datalayer.school.model.Person
import world.respect.shared.viewmodel.sharedschooldevice.login.StudentListUiState
import world.respect.shared.viewmodel.sharedschooldevice.login.StudentListViewModel

@Composable
fun StudentListScreen(
    viewModel: StudentListViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()
    StudentListScreen(
        uiState = uiState,
        onClickStudent = viewModel::onClickStudent,
    )
}

@Composable
fun StudentListScreen(
    uiState: StudentListUiState,
    onClickStudent: (Person) -> Unit,
) {
    val pager = respectRememberPager(uiState.students)

    val lazyPagingItems = pager.flow.collectAsLazyPagingItems()

    LazyColumn(modifier = Modifier.fillMaxSize()) {

        respectPagingItems(
            items = lazyPagingItems,
            key = { item, index -> item?.guid ?: index.toString() },
            contentType = { ClassDataSource.ENDPOINT_NAME },
        ) { student ->
            ListItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        student?.also(onClickStudent)
                    },

                leadingContent = {
                    RespectPersonAvatar(name = student?.fullName() ?: "")
                },

                headlineContent = {
                    Text(text = student?.fullName() ?: "")
                }
            )
        }
    }
}