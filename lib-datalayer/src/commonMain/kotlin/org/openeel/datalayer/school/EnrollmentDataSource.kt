package world.respect.datalayer.school

import io.ktor.util.StringValues
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import world.respect.lib.dataloadstate.DataLayerParams
import world.respect.lib.dataloadstate.DataLayerParams.ACTIVE_ON_DAY
import world.respect.lib.dataloadstate.DataLayerParams.ORDER_BY
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.datalayer.school.model.Enrollment
import world.respect.datalayer.school.model.EnrollmentRoleEnum
import world.respect.datalayer.shared.WritableDataSource
import world.respect.datalayer.shared.paging.IPagingSourceFactory
import world.respect.datalayer.shared.params.GetListCommonParams
import world.respect.datalayer.shared.params.OrderOption

interface EnrollmentDataSource: WritableDataSource<Enrollment> {

    enum class OrderBy(val orderOption: OrderOption) {
        UID_ASC(OrderOption.UID_ASC),
        UID_DESC(OrderOption.UID_DESC),
        STORED_ASC(OrderOption.STORED_ASC);

        companion object {

            fun fromValue(value: String): OrderBy {
                return entries.first { it.orderOption.name == value }
            }

        }
    }

    /**
     * @property activeOnDay if not null, then include only enrollments that would be active on the
     *           given day (between beginDate and endDate, inclusive). If beginDate or endDate is
     *           not specified it defaults to being included.
     */
    data class GetListParams(
        val common: GetListCommonParams = GetListCommonParams(),
        val classUid: String? = null,
        val role: EnrollmentRoleEnum? = null,
        val personUid: String? = null,
        val activeOnDay: LocalDate? = null,
        val orderBy: OrderBy = OrderBy.STORED_ASC,
    ) {

        companion object {

            fun fromParams(params: StringValues) : GetListParams{
                return GetListParams(
                    common = GetListCommonParams.fromParams(params),
                    classUid = params[DataLayerParams.FILTER_BY_CLASS_UID],
                    role = params[DataLayerParams.FILTER_BY_ENROLLMENT_ROLE]?.let {
                        EnrollmentRoleEnum.fromValue(it)
                    },
                    personUid = params[FILTER_BY_PERSON_UID],
                    activeOnDay = params[ACTIVE_ON_DAY]?.let { LocalDate.parse(it) },
                    orderBy = params[ORDER_BY]?.let {
                        OrderBy.fromValue(it)
                    } ?: OrderBy.STORED_ASC,
                )
            }
        }

    }

    suspend fun findByGuid(
        loadParams: DataLoadParams,
        guid: String,
    ): DataLoadState<Enrollment>

    fun findByGuidAsFlow(
        loadParams: DataLoadParams,
        guid: String,
    ): Flow<DataLoadState<Enrollment>>

    fun listAsPagingSource(
        loadParams: DataLoadParams,
        listParams: GetListParams,
    ): IPagingSourceFactory<Int, Enrollment>

    suspend fun list(
        loadParams: DataLoadParams,
        listParams: GetListParams,
    ): DataLoadState<List<Enrollment>>

    override suspend fun store(
        list: List<Enrollment>
    )

    companion object {

        const val ENDPOINT_NAME = "enrollment"

        const val FILTER_BY_PERSON_UID = "filterByPersonUid"

    }

}