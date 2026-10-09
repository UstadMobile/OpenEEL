package world.respect.datalayer.shared

import kotlin.time.Instant

/**
 * Most models have a lastModified and a stored time: both are required as discussed in
 * respect-datalayer-repository/README.md
 */
interface ModelWithTimes {

    val stored: Instant

    val lastModified: Instant

}