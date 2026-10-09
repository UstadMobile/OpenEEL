package world.respect.datalayer.db.school.ext

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
fun Uuid.toLongPair(): Pair<Long, Long> {
    return toLongs { hi, lo -> Pair(hi, lo) }
}
