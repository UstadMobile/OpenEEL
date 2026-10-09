package world.respect.lib.xapi.extensions.reportoptions

import kotlinx.serialization.Serializable

@Serializable
data class Indicator(
    val indicatorId: String = "",
    val name: String = "",
    val description: String = "",
    val type: String = "",
    val sql: String = "",
){
    companion object {
        const val TABLE_ID = 5
    }
}