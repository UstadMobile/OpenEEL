package world.respect.shared.viewmodel.person.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.lib.dataloadstate.DataLoadingState
import world.respect.lib.dataloadstate.DataReadyState
import world.respect.datalayer.SchoolDataSource
import world.respect.lib.dataloadstate.ext.dataOrNull
import world.respect.lib.dataloadstate.ext.isReadyAndSettled
import world.respect.datalayer.school.PersonDataSource
import world.respect.datalayer.school.domain.GetWritableRolesListUseCase
import world.respect.datalayer.school.model.Person
import world.respect.datalayer.school.model.PersonGenderEnum
import world.respect.datalayer.school.model.PersonRole
import world.respect.datalayer.school.model.PersonRoleEnum
import world.respect.datalayer.shared.params.GetListCommonParams
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.domain.phonenumber.PhoneNumValidatorUseCase
import world.respect.shared.domain.school.SchoolPrimaryKeyGenerator
import world.respect.shared.domain.validateemail.ValidateEmailUseCase
import world.respect.shared.ext.tryOrShowSnackbarOnError
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.add_person
import world.respect.shared.generated.resources.date_of_birth_in_future
import world.respect.shared.generated.resources.edit_person
import world.respect.shared.generated.resources.invalid
import world.respect.shared.generated.resources.required_field
import world.respect.shared.generated.resources.invalid_email
import world.respect.shared.generated.resources.save
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.NavResultReturner
import world.respect.shared.navigation.PersonDetail
import world.respect.shared.navigation.PersonEdit
import world.respect.shared.navigation.PersonList
import world.respect.shared.navigation.RouteResultDest
import world.respect.shared.navigation.sendResultIfResultExpected
import world.respect.shared.resources.UiText
import world.respect.shared.util.LaunchDebouncer
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.ActionBarButtonUiState
import world.respect.shared.viewmodel.app.appstate.SnackBarDispatcher
import kotlin.collections.first
import kotlin.getValue
import kotlin.time.Clock

data class PersonEditUiState(
    val uid: String = "",
    val persons: DataLoadState<List<Person>> = DataLoadingState(),
    val roleOptions: List<PersonRoleEnum> = emptyList(),
    val showRoleDropdown: Boolean = false,
    val dateOfBirthError: UiText? = null,

    /**
     * Used to determine if the user has actually set a phone number. This is set by the UI
     * components as a user inputs a number. True if the national phone number part (e.g. not just
     * country code) is set, false otherwise.
     *
     * A person without any phone number set is allowed, but if a number is entered, it will be
     * validated.
     */
    val nationalPhoneNumSet: Boolean = false,
    val phoneNumError: UiText? = null,
    val emailError: UiText? = null,
    val genderError: UiText? = null,
    val firstNameError: UiText? = null,
    val lastNameError: UiText? = null,
    val presetRole: PersonRoleEnum? = null,
    val hasManageFamilyPermission: Boolean = false,
) {
    val person: Person?
        get() = persons.dataOrNull()?.firstOrNull { it.guid == uid }

    val familyMembers: List<Person>
        get() = persons.dataOrNull()?.filter { it.guid != uid } ?: emptyList()

    val fieldsEnabled : Boolean
        get() = persons.isReadyAndSettled()

    val hasErrors: Boolean
        get() = firstNameError!=null ||
                lastNameError!=null ||
                genderError != null ||
                dateOfBirthError != null ||
                phoneNumError != null ||
                emailError!=null

    val showFamilyMembers: Boolean
        get() = hasManageFamilyPermission && person?.roles?.any {
            it.roleEnum in listOf(PersonRoleEnum.PARENT, PersonRoleEnum.STUDENT)
        } == true && presetRole == null
}

class PersonEditViewModel(
    savedStateHandle: SavedStateHandle,
    accountManager: RespectAccountManager,
    private val json: Json,
    private val phoneNumValidatorUseCase: PhoneNumValidatorUseCase,
    private val navResultReturner: NavResultReturner,
    private val validateEmailUseCase: ValidateEmailUseCase,
    private val snackBarDispatcher: SnackBarDispatcher,
) : RespectViewModel(savedStateHandle), KoinScopeComponent {

    override val scope: Scope = accountManager.requireActiveAccountScope()

    private val schoolDataSource: SchoolDataSource by inject()

    val route: PersonEdit = savedStateHandle.toRoute()

    private val schoolPrimaryKeyGenerator: SchoolPrimaryKeyGenerator by inject()

    private val getWritableRolesListUseCase: GetWritableRolesListUseCase by inject()

    private val guid = route.guid ?: schoolPrimaryKeyGenerator.primaryKeyGenerator.nextId(
        Person.TABLE_ID
    ).toString()

    private val _uiState = MutableStateFlow(
        PersonEditUiState(uid = guid, presetRole = route.presetRole)
    )

    val uiState = _uiState.asStateFlow()

    private val debouncer = LaunchDebouncer(viewModelScope)

    init {
        _appUiState.update { prev ->
            prev.copy(
                title = if (route.guid == null)
                    Res.string.add_person.asUiText()
                else
                    Res.string.edit_person.asUiText(),
                hideBottomNavigation = true,
                actionBarButtonState = ActionBarButtonUiState(
                    onClick = ::onClickSave,
                    text = Res.string.save.asUiText(),
                    visible = true,
                )
            )
        }

        launchWithLoadingIndicator {
            val currentPersonRole = accountManager.selectedAccountAndPersonFlow.first()
                ?.person?.roles?.first()?.roleEnum

            val roleOptions = if (route.presetRole != null) {
                listOf(route.presetRole)
            } else if(currentPersonRole != null ){
                getWritableRolesListUseCase(currentPersonRole)
            }else {
                emptyList()
            }

            _uiState.update { prev ->
                prev.copy(
                    roleOptions = roleOptions,
                    hasManageFamilyPermission = currentPersonRole == PersonRoleEnum.TEACHER
                            || currentPersonRole == PersonRoleEnum.SITE_ADMINISTRATOR
                            || currentPersonRole == PersonRoleEnum.SYSTEM_ADMINISTRATOR
                )
            }

            if (route.guid != null) {
                loadEntity(
                    json = json,
                    serializer = ListSerializer(Person.serializer()),
                    initialStateKey = KEY_INITIAL_STATE,
                    loadFn = { loadParams ->
                        schoolDataSource.personDataSource.list(
                            loadParams = loadParams,
                            params = PersonDataSource.GetListParams(
                                common = GetListCommonParams(
                                    guid = route.guid
                                ),
                                includeRelated = true
                            )
                        )
                    },
                    uiUpdateFn = { person ->
                        _uiState.update { prev -> prev.copy(persons = person) }
                    }
                )
            } else {
                _uiState.update { prev ->
                    prev.copy(
                        persons = DataReadyState(
                            data = listOf(
                                Person(
                                    guid = guid,
                                    givenName = "",
                                    familyName = "",
                                    roles = listOf(
                                        PersonRole(
                                            isPrimaryRole = true,
                                            roleEnum = route.presetRole ?: PersonRoleEnum.STUDENT,
                                        )
                                    ),
                                    gender = PersonGenderEnum.UNSPECIFIED
                                )
                            )
                        ),
                        showRoleDropdown = route.presetRole == null,
                    )
                }
            }
        }

        viewModelScope.launch {
            navResultReturner.filteredResultFlowForKey(
                PERSON_SELECT_RESULT
            ).collect { navResult ->
                val selectedPerson = navResult.result as? Person ?: return@collect
                val currentPersons = _uiState.value.persons.dataOrNull() ?: return@collect

                if(currentPersons.any { it.guid ==  selectedPerson.guid }) {
                    return@collect //already in list
                }

                _uiState.update { prev ->
                    prev.copy(
                        persons = DataReadyState(
                            data= currentPersons + selectedPerson
                        )
                    )
                }
            }
        }
    }

    fun onRemoveFamilyMember(person: Person) {
        val prevPersons = _uiState.value.persons.dataOrNull() ?: return

        _uiState.update { prev ->
            prev.copy(
                persons = DataReadyState(
                    data = prevPersons.filterNot { it.guid == person.guid }
                )
            )
        }
    }

    fun onEntityChanged(person: Person) {
        val personsToCommit = _uiState.updateAndGet { prev ->
            val prevPerson = prev.person ?: return@updateAndGet prev

            prev.copy(
                persons = DataReadyState(
                    listOf(person) + prev.familyMembers
                ),
                firstNameError = prev.firstNameError?.takeIf { prevPerson.givenName == person.givenName },
                lastNameError = prev.lastNameError?.takeIf { prevPerson.familyName == person.familyName },
                genderError = prev.genderError?.takeIf { prevPerson.gender == person.gender },
                emailError = prev.emailError?.takeIf {
                    prevPerson.email == person.email
                },
                phoneNumError = if (prev.phoneNumError != null && prevPerson.phoneNumber == person.phoneNumber) {
                    prev.phoneNumError
                }else {
                    null
                },
            )
        }.persons.dataOrNull() ?: return

        debouncer.launch(DEFAULT_SAVED_STATE_KEY) {
            savedStateHandle[DEFAULT_SAVED_STATE_KEY] = json.encodeToString(personsToCommit)
        }
    }

    fun onNationalPhoneNumSetChanged(phoneNumSet: Boolean) {
        _uiState.takeIf { it.value.nationalPhoneNumSet != phoneNumSet }?.update { prev ->
            prev.copy(nationalPhoneNumSet = phoneNumSet)
        }
    }

    fun onClickAddFamilyMember() {
        val roleEnumVal = uiState.value.person?.roles?.firstOrNull()?.roleEnum
            ?: PersonRoleEnum.STUDENT

        val filterByRole = when (roleEnumVal) {
            PersonRoleEnum.STUDENT -> PersonRoleEnum.PARENT
            PersonRoleEnum.PARENT -> PersonRoleEnum.STUDENT
            else -> PersonRoleEnum.STUDENT
        }

        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                PersonList.create(
                    filterByRole = filterByRole,
                    personGuid = route.guid,
                    resultDest = RouteResultDest(
                        resultPopUpTo = route,
                        resultKey = PERSON_SELECT_RESULT
                    ),
                    hideInvite = true,
                )
            )
        )
    }

    fun onClickSave() {
        val personToSave = _uiState.value.person?.copy(
            relatedPersonUids = uiState.value.familyMembers.map { it.guid }
        ) ?: return

        val initialStatePersons = savedStateHandle.get<String>(KEY_INITIAL_STATE)?.let {
            json.decodeFromString(ListSerializer(Person.serializer()), it)
        }

        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

        val dob = personToSave.dateOfBirth

        _uiState.update { prev ->
            prev.copy(
                firstNameError = if(personToSave.givenName.isBlank()) {
                    Res.string.required_field.asUiText()
                }else {
                    null
                },
                lastNameError = if(personToSave.familyName.isBlank()) {
                    Res.string.required_field.asUiText()
                }else {
                    null
                },
                dateOfBirthError = if (dob != null && dob > today) {
                    Res.string.date_of_birth_in_future.asUiText()
                }else {
                    null
                },
                phoneNumError = if (uiState.value.nationalPhoneNumSet &&
                    !phoneNumValidatorUseCase.isValid(personToSave.phoneNumber ?: "")
                ) {
                    Res.string.invalid.asUiText()
                }else {
                    null
                },
                emailError = if (!personToSave.email.isNullOrBlank() && !validateEmailUseCase(personToSave.email.toString())) {
                    Res.string.invalid_email.asUiText()
                } else null,
                genderError = if(personToSave.gender == PersonGenderEnum.UNSPECIFIED) {
                    Res.string.required_field.asUiText()
                }else {
                    null
                },
            )
        }


        if(uiState.value.hasErrors)
            return

        launchWithLoadingIndicator {
            snackBarDispatcher.tryOrShowSnackbarOnError {
                val modTime = Clock.System.now()
                val familyMembersAdded = uiState.value.familyMembers.mapNotNull { familyPerson ->
                    if(guid !in familyPerson.relatedPersonUids) {
                        familyPerson.copy(
                            relatedPersonUids = familyPerson.relatedPersonUids + guid,
                            lastModified = modTime,
                        )
                    }else {
                        null
                    }
                }

                val familyMembersRemoved: List<Person> = initialStatePersons?.filter {
                    it.guid != guid
                }?.mapNotNull { familyPerson ->
                    if(familyPerson.guid !in personToSave.relatedPersonUids) {
                        familyPerson.copy(
                            relatedPersonUids = familyPerson.relatedPersonUids - guid,
                            lastModified = modTime,
                        )
                    }else {
                        null
                    }
                } ?: emptyList()

                schoolDataSource.personDataSource.store(
                    familyMembersAdded + familyMembersRemoved + personToSave.copy(lastModified = modTime)
                )

                if(
                    !navResultReturner.sendResultIfResultExpected(
                        route = route,
                        navCommandFlow = _navCommandFlow,
                        result = personToSave,
                    )
                ) {
                    if (route.guid == null) {
                        _navCommandFlow.tryEmit(
                            NavCommand.Navigate(
                                PersonDetail(guid), popUpTo = route, popUpToInclusive = true
                            )
                        )
                    } else {
                        _navCommandFlow.tryEmit(NavCommand.PopUp())
                    }
                }
            }
        }
    }
    companion object {
        const val PERSON_SELECT_RESULT = "person_select_result"
    }
}
