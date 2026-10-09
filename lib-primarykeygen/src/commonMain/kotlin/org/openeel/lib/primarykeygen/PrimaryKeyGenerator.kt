package world.respect.lib.primarykeygen

import kotlinx.atomicfu.AtomicLong
import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.updateAndGet
import kotlinx.coroutines.delay
import world.respect.lib.primarykeygen.ext.millisUntilNextSecond
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.pow
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Manage generation of unique primary keys a list of tables. Based on DoorPrimaryKeyManager. It is
 * often helpful to get a unique primary key _before_ committing anything to the database (e.g. when
 * creating entities that are joined such as a publication and link entities. The link entities
 * require the publication uid to use as a foreign key. This allows creating entities with the
 * appropriate foreign keys set without having to commit to the database (especially helpful in
 * screens where data can be edited that the user may discard).
 *
 * This is inspired by the Twitter snowflake approach. It is slightly modified so that the 64bit
 * keys are generated as follows:
 *
 * 31 bits: unix timestamp (offset by CUSTOM_EPOCH)
 * 20 bits: Node Id
 * 12 bits: Sequence number
 * 1 bit (sign bit): unused
 *
 * This allows 1,048,576 nodes to create 4,096 unique new entries per second (per table). This supports
 * more unique nodes than snowflake with fewer unique keys per second. This seems appropriate as most
 * work is delegated to the client.
 *
 * @param tableIdList a list of all tableIds on the database.
 */
@OptIn(ExperimentalTime::class)
class PrimaryKeyGenerator(
    tableIdList: Collection<Int>
) {


    private val tableKeyManagers: MutableMap<Int, TablePrimaryKeyManager> = ConcurrentHashMap()


    private inline val timestamp: Long
        get() = (Clock.System.now().epochSeconds) - CUSTOM_EPOCH

    val nodeId: Int = generateDoorNodeId(MAX_NODE_ID)

    init {
        tableIdList.forEach {
            tableKeyManagers[it] = TablePrimaryKeyManager()
        }
    }

    private inner class TablePrimaryKeyManager() {

        /**
         * We have two values that we are tracking concurrently: the sequence number and the
         * timestamp. When the timestamp advances, we can reset the sequence number to zero.
         *
         * These two values are wrapped in a single long so that we can use a single AtomicLong
         * wrapper instead of requiring thread locking.
         */
        private val atomicWrapper = atomic(0L)

        //If this is not inline, then AtomicFu will complain
        @Suppress("NOTHING_TO_INLINE")
        private inline fun AtomicLong.nextWrappedTimeAndSeqNum(): Long = updateAndGet { lastVal ->
            val lastTimestamp = lastVal shr 32
            val lastSeq = lastVal and Int.MAX_VALUE.toLong()

            val newTimestamp = timestamp
            val newSeq = if(newTimestamp > lastTimestamp) {
                0
            }else {
                lastSeq + 1
            }

            (newTimestamp shl 32) or newSeq
        }

        private fun Long.unwrapTime() = this shr 32

        private fun Long.unwrapSeqNum() = this and Int.MAX_VALUE.toLong()

        private fun generateId(currentTimestamp: Long, nodeId: Long, seqNum: Long) : Long{
            return currentTimestamp shl (NODE_ID_BITS + SEQUENCE_BITS) or
                    (nodeId shl SEQUENCE_BITS) or
                    seqNum
        }

        fun nextId(): Long {
            val nextWrappedId = atomicWrapper.nextWrappedTimeAndSeqNum()
            val seqNum = nextWrappedId.unwrapSeqNum()

            if(seqNum < MAX_SEQUENCE) {
                return generateId(nextWrappedId.unwrapTime(), nodeId.toLong(), seqNum)
            }else {
                //wait long enough for the next second
                Thread.sleep(Clock.System.now().millisUntilNextSecond.toLong() + 1)
                return nextId()
            }
        }

        suspend fun nextIdAsync() : Long {
            val nextWrappedId = atomicWrapper.nextWrappedTimeAndSeqNum()
            val seqNum = nextWrappedId.unwrapSeqNum()

            if(seqNum < MAX_SEQUENCE) {
                return generateId(nextWrappedId.unwrapTime(), nodeId.toLong(), seqNum)
            }else {
                //wait long enough for the next second
                delay(Clock.System.now().millisUntilNextSecond.toLong() + 1)
                return nextId()
            }
        }

    }

    fun nextId(tableId: Int): Long {
        //Note: it is required that the list of valid table ids is passed as a constructor parameter
        //Hence the return result should never be null
        return tableKeyManagers[tableId]!!.nextId()
    }

    suspend fun nextIdAsync(tableId: Int): Long = tableKeyManagers[tableId]!!.nextIdAsync()

    companion object {
        @Suppress("unused")
        const val UNUSED_BITS = 1

        @Suppress("unused")
        const val EPOCH_BITS = 31 // Time in seconds - works for up to 34 years from 1/Jan/2020

        const val NODE_ID_BITS = 20

        const val SEQUENCE_BITS = 12

        val MAX_NODE_ID = 2f.pow(NODE_ID_BITS).toInt() // Up to 1,048,576 nodes

        val MAX_SEQUENCE = 2f.pow(SEQUENCE_BITS).toInt() //Up to 4.096

        // Custom Epoch (January 1, 2015 Midnight UTC = 2015-01-01T00:00:00Z)

        //01/Jan/2020 at 00:00:00 UTC
        const val CUSTOM_EPOCH = 1577836800
    }


}