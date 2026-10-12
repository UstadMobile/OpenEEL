package org.openeel.datalayer.school

import kotlinx.coroutines.flow.Flow
import org.openeel.lib.dataloadstate.DataLoadParams
import org.openeel.lib.dataloadstate.DataLoadState
import org.openeel.lib.xapi.extensions.reportoptions.Indicator

interface IndicatorDataSource {

    suspend fun allIndicatorAsFlow(): Flow<DataLoadState<List<Indicator>>>

    suspend fun getIndicatorAsync(loadParams: DataLoadParams, indicatorId: String): DataLoadState<Indicator>

    suspend fun getIndicatorAsFlow(indicatorId: String): Flow<DataLoadState<Indicator>>

    suspend fun putIndicator(indicator: Indicator)

    suspend fun updateIndicator(indicator: Indicator)

    suspend fun initializeDefaultIndicators(idGenerator: () -> String)
}