package org.openeel.app.view.clazz.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItems
import org.jetbrains.compose.resources.stringResource
import org.openeel.app.components.RespectListSortHeader
import org.openeel.app.components.RespectPersonAvatar
import org.openeel.app.components.respectPagingItems
import org.openeel.app.components.respectRememberPager
import org.openeel.datalayer.db.school.ext.fullName
import org.openeel.lib.dataloadstate.ext.dataOrNull
import org.openeel.datalayer.school.model.EnrollmentRoleEnum
import org.openeel.datalayer.school.model.Person
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.add_student
import org.openeel.shared.generated.resources.add_teacher
import org.openeel.shared.generated.resources.description
import org.openeel.shared.generated.resources.pending_requests
import org.openeel.shared.generated.resources.collapse_pending_invites
import org.openeel.shared.generated.resources.collapse_students
import org.openeel.shared.generated.resources.collapse_teachers
import org.openeel.shared.generated.resources.description
import org.openeel.shared.generated.resources.expand_pending_invites
import org.openeel.shared.generated.resources.expand_students
import org.openeel.shared.generated.resources.expand_teachers
import org.openeel.shared.generated.resources.manage_enrollments
import org.openeel.shared.generated.resources.more_options
import org.openeel.shared.generated.resources.pending_requests
import org.openeel.shared.generated.resources.remove_from_class
import org.openeel.shared.generated.resources.student
import org.openeel.shared.generated.resources.students
import org.openeel.shared.generated.resources.teacher
import org.openeel.shared.generated.resources.teachers
import org.openeel.shared.util.SortOrderOption
import org.openeel.shared.viewmodel.clazz.detail.ClazzDetailUiState
import org.openeel.shared.viewmodel.clazz.detail.ClazzDetailViewModel


@Composable
fun ClazzDetailScreen(
    viewModel: ClazzDetailViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    ClazzDetailScreen(
        uiState = uiState,
        onClickAddPersonToClazz = viewModel::onClickAddPersonToClazz,
        onSortOrderChanged = viewModel::onSortOrderChanged,
        onSelectChip = viewModel::onSelectChip,
        onClickAcceptInvite = viewModel::onClickAcceptInvite,
        onClickDismissInvite = viewModel::onClickDismissInvite,
        onTogglePendingSection = viewModel::onTogglePendingSection,
        onToggleTeachersSection = viewModel::onToggleTeachersSection,
        onToggleStudentsSection = viewModel::onToggleStudentsSection,
        onClickRemovePersonFromClass = viewModel::onClickRemovePersonFromClass,
        onClickManageEnrollments = viewModel::onClickManageEnrollments,
        onClickPerson = viewModel::onClickPerson,
    )
}

@Composable
fun ClazzDetailScreen(
    uiState: ClazzDetailUiState,
    onClickAddPersonToClazz: (EnrollmentRoleEnum) -> Unit,
    onSortOrderChanged: (SortOrderOption) -> Unit = { },
    onSelectChip: (String) -> Unit,
    onClickAcceptInvite: (Person) -> Unit,
    onClickDismissInvite: (Person) -> Unit,
    onTogglePendingSection: () -> Unit,
    onToggleTeachersSection: () -> Unit,
    onToggleStudentsSection: () -> Unit,
    onClickRemovePersonFromClass: (Person, EnrollmentRoleEnum) -> Unit,
    onClickManageEnrollments: (Person, EnrollmentRoleEnum) -> Unit,
    onClickPerson: (Person) -> Unit,
) {
    val teacherPager = respectRememberPager(uiState.teachers)
    val studentPager = respectRememberPager(uiState.students)

    val pendingTeacherPager = respectRememberPager(uiState.pendingTeachers)
    val pendingStudentPager = respectRememberPager(uiState.pendingStudents)

    val teacherLazyPagingItems = teacherPager.flow.collectAsLazyPagingItems()
    val studentLazyPagingItems = studentPager.flow.collectAsLazyPagingItems()
    val pendingTeacherLazyPagingItems = pendingTeacherPager.flow.collectAsLazyPagingItems()
    val pendingStudentLazyPagingItems = pendingStudentPager.flow.collectAsLazyPagingItems()

    fun Person?.key(role: EnrollmentRoleEnum, index: Int): Any {
        return this?.guid?.let {
            Pair(it, role)
        } ?: "${role}_$index"
    }


    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
    ) {
        item {
            ListItem(
                headlineContent = {
                    Text(
                        text = stringResource(
                            resource = Res.string.description
                        )
                    )
                },

                supportingContent = {
                    Text(text = uiState.clazz.dataOrNull()?.description ?: "")
                }
            )
        }

        item {
            RespectListSortHeader(
                activeSortOrderOption = uiState.activeSortOrderOption,
                sortOptions = uiState.sortOptions,
                enabled = uiState.fieldsEnabled,
                onClickSortOption = onSortOrderChanged,
            )
        }

        if ((uiState.showAddTeacher || uiState.showAddStudent) &&
            (pendingTeacherLazyPagingItems.itemCount + pendingStudentLazyPagingItems.itemCount) > 0
        ) {
            item("pending_header") {
                ListItem(
                    modifier = Modifier
                        .clickable { onTogglePendingSection() },
                    headlineContent = {
                        Text(
                            text = stringResource(Res.string.pending_requests)
                                    + " (${
                                pendingTeacherLazyPagingItems.itemCount
                                        + pendingStudentLazyPagingItems.itemCount
                            })"
                        )
                    },
                    trailingContent = {
                        Icon(
                            imageVector = Icons.Outlined.KeyboardArrowDown,
                            contentDescription =
                                if (uiState.isPendingExpanded) {
                                    stringResource(
                                        Res.string.collapse_pending_invites
                                    )
                                } else {
                                    stringResource(
                                        Res.string.expand_pending_invites
                                    )
                                },
                            modifier = Modifier.size(24.dp)
                                .rotate(
                                    if (uiState.isPendingExpanded) 0f else -90f
                                ),
                        )
                    }
                )
            }
        }

        if (uiState.isPendingExpanded) {
            if (uiState.showAddTeacher) {
                respectPagingItems(
                    items = pendingTeacherLazyPagingItems,
                    key = { person, index ->
                        person.key(EnrollmentRoleEnum.PENDING_TEACHER, index)
                    }
                ) { person ->
                    ClassPendingPersonListItem(
                        person = person,
                        pendingRole = Res.string.teacher,
                        showApproveDeclineOptions = person?.let {
                            uiState.showApproveOption(it)
                        } ?: false,
                        onClickAcceptInvite = onClickAcceptInvite,
                        onClickDismissInvite = onClickDismissInvite,
                    )
                }
            }

            if (uiState.showAddStudent) {
                respectPagingItems(
                    items = pendingStudentLazyPagingItems,
                    key = { person, index ->
                        person.key(EnrollmentRoleEnum.PENDING_STUDENT, index)
                    }
                ) { person ->
                    ClassPendingPersonListItem(
                        person = person,
                        pendingRole = Res.string.student,
                        showApproveDeclineOptions = person?.let {
                            uiState.showApproveOption(it)
                        } ?: false,
                        onClickAcceptInvite = onClickAcceptInvite,
                        onClickDismissInvite = onClickDismissInvite,
                    )
                }
            }
        }

        item("teacher_header") {
            ListItem(
                modifier = Modifier
                    .clickable { onToggleTeachersSection() },

                headlineContent = {
                    Text(
                        modifier = Modifier.padding(top = 24.dp),
                        text = stringResource(Res.string.teachers)
                    )
                },

                trailingContent = {
                    Icon(
                        imageVector = Icons.Outlined.KeyboardArrowDown,
                        contentDescription = if (uiState.isTeachersExpanded) {
                            stringResource(Res.string.collapse_teachers)
                        } else {
                            stringResource(Res.string.expand_teachers)
                        },
                        modifier = Modifier.size(24.dp)
                            .rotate(
                                if (uiState.isTeachersExpanded) 0f else -90f
                            ),
                    )
                }
            )
        }

        if (uiState.isTeachersExpanded) {
            if (uiState.showAddTeacher) {
                item("add_teacher") {
                    ListItem(
                        modifier = Modifier.clickable {
                            onClickAddPersonToClazz(EnrollmentRoleEnum.TEACHER)
                        },
                        leadingContent = {
                            Icon(
                                modifier = Modifier.size(40.dp).padding(8.dp),
                                imageVector = Icons.Filled.Add,
                                contentDescription = stringResource(resource = Res.string.add_teacher)
                            )
                        },
                        headlineContent = {
                            Text(
                                text =
                                    stringResource(resource = Res.string.add_teacher)
                            )
                        }
                    )
                }
            }

            respectPagingItems(
                items = teacherLazyPagingItems,
                key = { person, index -> person.key(EnrollmentRoleEnum.TEACHER, index) }
            ) { teacher ->
                PersonListItemWithMenu(
                    person = teacher,
                    showMenu = uiState.showAddTeacher,
                    onClickRemove = { onClickRemovePersonFromClass(it, EnrollmentRoleEnum.TEACHER) },
                    onClickManage = { onClickManageEnrollments(it, EnrollmentRoleEnum.TEACHER) },
                    onClick = onClickPerson,
                )
            }
        }

        item("student_header") {
            ListItem(
                modifier = Modifier
                    .clickable { onToggleStudentsSection() },

                headlineContent = {
                    Text(
                        modifier = Modifier.padding(top = 24.dp),
                        text = stringResource(
                            resource = Res.string.students
                        )
                    )
                },

                trailingContent = {
                    Icon(
                        imageVector = Icons.Outlined.KeyboardArrowDown,
                        contentDescription = if (uiState.isStudentsExpanded) {
                            stringResource(Res.string.collapse_students)
                        } else {
                            stringResource(Res.string.expand_students)
                        },
                        modifier = Modifier.size(24.dp)
                            .rotate(
                                if (uiState.isStudentsExpanded) 0f else -90f
                            )
                    )
                }
            )
        }

        if (uiState.isStudentsExpanded) {
            if(uiState.showAddStudent) {
                item("add_student") {
                    ListItem(
                        modifier = Modifier.clickable {
                            onClickAddPersonToClazz(EnrollmentRoleEnum.STUDENT)
                        },

                        leadingContent = {
                            Icon(
                                modifier = Modifier.size(40.dp).padding(8.dp),
                                imageVector = Icons.Filled.Add,
                                contentDescription = stringResource(resource = Res.string.add_student)
                            )
                        },

                        headlineContent = {
                            Text(
                                text =
                                    stringResource(resource = Res.string.add_student)
                            )
                        }
                    )
                }
            }
            respectPagingItems(
                items = studentLazyPagingItems,
                key = { person, index -> person.key(EnrollmentRoleEnum.STUDENT, index) }
            ) { student ->
                PersonListItemWithMenu(
                    person = student,
                    onClickRemove = {
                        onClickRemovePersonFromClass(it, EnrollmentRoleEnum.STUDENT)
                    },
                    onClickManage = { onClickManageEnrollments(it, EnrollmentRoleEnum.STUDENT) },
                    showMenu = uiState.showAddStudent,
                    onClick = onClickPerson,
                )
            }
        }
    }
}

@Composable
fun PersonListItemWithMenu(
    person: Person?,
    showMenu: Boolean = false,
    onClickRemove: (Person) -> Unit,
    onClickManage: (Person) -> Unit,
    onClick: (Person) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    ListItem(
        modifier = Modifier.fillMaxWidth().clickable {
            person?.also(onClick)
        },
        leadingContent = {
            RespectPersonAvatar(name = person?.fullName() ?: "")
        },
        headlineContent = {
            Text(text = person?.fullName().orEmpty())
        },
        trailingContent = if(showMenu) {
            {
                IconButton(onClick = { expanded = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = stringResource(resource = Res.string.more_options)
                    )
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(Res.string.remove_from_class)) },
                        onClick = {
                            expanded = false
                            person?.also(onClickRemove)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(Res.string.manage_enrollments)) },
                        onClick = {
                            expanded = false
                            person?.also(onClickManage)
                        }
                    )
                }
            }
        }else {
            null
        }
    )
}
