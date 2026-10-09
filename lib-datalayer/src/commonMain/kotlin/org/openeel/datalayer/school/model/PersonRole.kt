package world.respect.datalayer.school.model

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class PersonRole(
    val isPrimaryRole: Boolean,
    val roleEnum: PersonRoleEnum,
    val beginDate: LocalDate? = null,
    val endDate: LocalDate? = null,
)
