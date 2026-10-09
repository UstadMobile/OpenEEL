package world.respect.datalayer.db.school.xapi.ext

import kotlin.uuid.Uuid

/**
 * As per the Xapi Spec a Statement can contain a substatement, which is almost identical to a normal
 * xAPI statement, with the exception that substatements cannot contain nested substatements.
 *
 * See
 * https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Data.md#2443-when-the-object-is-a-statement
 *
 * It therefor makes sense to store them in the same table and apply mostly the same logic.
 * Substatements don't directly have their own ID. We make one up according to this formula: the
 * uuid longs, with the least significant bit incremented by one.
 */
internal fun Uuid.uuidForSubstatement(): Uuid {
    return toLongs { mostSignificantBits, leastSignificantBits ->
        Uuid.fromLongs(mostSignificantBits, leastSignificantBits + 1)
    }
}
