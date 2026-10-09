package world.respect.lib.xapi.model

import kotlinx.serialization.Serializable

/**
 * https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Data.md#2462-contextactivities-property
 *
 * As per the xAPI spec, to maintain compatibility with xAPI 0.95, a single item is allowed.
 * XapiActivityStatementObjectListSerializer uses SingleItemToListTransformer to get around this
 * issue.
 *
 */
@Serializable()
data class XapiContextActivities(
    @Serializable(with = XapiActivityStatementObjectListSerializer::class)
    val parent: List<XapiActivity>? = null,

    @Serializable(with = XapiActivityStatementObjectListSerializer::class)
    val grouping: List<XapiActivity>? = null,

    @Serializable(with = XapiActivityStatementObjectListSerializer::class)
    val category: List<XapiActivity>? = null,

    @Serializable(with = XapiActivityStatementObjectListSerializer::class)
    val other: List<XapiActivity>? = null,
)
