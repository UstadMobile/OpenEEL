package org.openeel.datalayer.db.school

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.openeel.lib.dataloadstate.DataLoadParams
import org.openeel.lib.dataloadstate.DataLoadState
import org.openeel.lib.dataloadstate.DataReadyState
import org.openeel.lib.dataloadstate.NoDataLoadedState
import org.openeel.datalayer.db.RespectSchoolDatabase
import org.openeel.datalayer.db.school.adapters.toIndicator
import org.openeel.datalayer.db.school.adapters.toIndicatorEntity
import org.openeel.lib.xapi.extensions.reportoptions.Indicator
import org.openeel.datalayer.school.IndicatorDataSource
import org.openeel.lib.xapi.extensions.reportoptions.DefaultIndicators

class IndicatorDataSourceDb(
    private val schoolDb: RespectSchoolDatabase,
): IndicatorDataSource {

    override suspend fun allIndicatorAsFlow(): Flow<DataLoadState<List<Indicator>>> {
        return schoolDb.getIndicatorEntityDao().getAllIndicator().map { indicatorEntities ->
            DataReadyState(indicatorEntities.map { it.toIndicator() })
        }
    }

    override suspend fun getIndicatorAsync(
        loadParams: DataLoadParams,
        indicatorId: String
    ): DataLoadState<Indicator> {
        val indicatorEntity = schoolDb.getIndicatorEntityDao().getIndicatorAsync(indicatorId)
        return if (indicatorEntity != null) {
            DataReadyState(indicatorEntity.toIndicator())
        } else {
            NoDataLoadedState(NoDataLoadedState.Reason.NOT_FOUND)
        }
    }

    override suspend fun getIndicatorAsFlow(indicatorId: String): Flow<DataLoadState<Indicator>> {
        return schoolDb.getIndicatorEntityDao().getIndicatorAsFlow(indicatorId).map { indicatorEntity ->
            if (indicatorEntity != null) {
                DataReadyState(indicatorEntity.toIndicator())
            } else {
                NoDataLoadedState(NoDataLoadedState.Reason.NOT_FOUND)
            }
        }
    }

    override suspend fun putIndicator(indicator: Indicator) {
        val indicatorEntity = indicator.toIndicatorEntity()
        schoolDb.getIndicatorEntityDao().putIndicator(indicatorEntity)
    }

    override suspend fun updateIndicator(indicator: Indicator) {
        val indicatorEntity = indicator.toIndicatorEntity()
        schoolDb.getIndicatorEntityDao().updateIndicator(indicatorEntity)
    }

    override suspend fun initializeDefaultIndicators(idGenerator: () -> String) {
        val existingCount = schoolDb.getIndicatorEntityDao().getIndicatorCount()
        if (existingCount == 0) {
            DefaultIndicators.list.forEach { indicator ->
                val indicatorWithId = indicator.copy(
                    indicatorId = idGenerator()
                )
                putIndicator(indicatorWithId)
            }
        }
    }
}