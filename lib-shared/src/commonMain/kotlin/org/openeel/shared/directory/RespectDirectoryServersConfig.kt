package org.openeel.shared.directory

import kotlinx.serialization.Serializable
import org.openeel.datalayer.respect.model.RespectSchoolDirectory

/**
 * @property directories a list of RespectDirectoryServer that implement the RESPECT Directory APIs
 */
@Serializable
data class RespectDirectoryServersConfig(
    val directories: List<RespectSchoolDirectory>,
)

