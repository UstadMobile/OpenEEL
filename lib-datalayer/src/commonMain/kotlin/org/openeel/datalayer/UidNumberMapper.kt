package world.respect.datalayer

/**
 * Each type of data has a uid which needs to support abstract strings (e.g. activity id urls in
 * xAPI, etc). When storing this in a database we want to be able to map this to a number to use
 * as a primary key to make lookups more efficient.
 *
 * Ids also need to be built to prevent collision (see PrimaryKeyGenerator for further notes).
 *
 * URL ids often have the same prefix, so even with an index, a normal string lookup will be very
 * inefficient.
 *
 * There are two potential approaches to mapping a uid string to a number:
 *
 * a) xxhash64: where the uid string is generated externally, as long as the number of unique keys
 *    is in the thousands or less, xxhash64 works well. See
 *    https://github.com/Cyan4973/xxHash/tree/dev/tests/collisions
 *
 * b) toLong: where the uid is generated using a Snowflake approach (as per PrimaryKeyGenerator),
 *    there is no need to use xxhash64. This has a slight advantage for database performance
 *    because timestamp based snowflakes will generally go into the end of an index, whereas the
 *    more random values produced by xxhash64 could go anywhere.
 *
 */
fun interface UidNumberMapper {

    operator fun invoke(uid: String): Long

}