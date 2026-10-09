package world.respect.lib.primarykeygen

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test


class DoorPrimaryKeyGeneratorTest {

    @Test
    fun givenSingleThread_whenIdsRequested_thenShouldGiveUniqueIds() {
        val numIdsToMake = 10000
        val pkManager = PrimaryKeyGenerator(listOf(42))
        val generatedPks = LongArray(numIdsToMake)
        for(i in 0 until numIdsToMake) {
            generatedPks[i] = pkManager.nextId(42)
        }
        Assert.assertEquals("Size of set of unique pks is the same as original size",
            generatedPks.size, generatedPks.toSet().size)
    }

    @DelicateCoroutinesApi
    @Test
    fun givenMultipleThreads_whenIdsRequested_thenShouldGiveUniqueIds() {
        val numThreads = 4
        val numItemsPerThread = 2500
        runBlocking {
            val pkManager = PrimaryKeyGenerator(listOf(42))

            val deferredResults = mutableListOf<Deferred<List<Pair<Int, Long>>>>()
            for(i in 0 until numThreads) {
                deferredResults += GlobalScope.async {
                    val generatedList = mutableListOf<Pair<Int, Long>>()
                    (0 until numItemsPerThread).forEach { _ ->
                        generatedList += Pair(i, pkManager.nextId(42))
                    }

                    generatedList
                }
            }

            deferredResults.forEach {
                it.await()
            }

            val generatedPks = mutableListOf<Pair<Int, Long>>()
            deferredResults.forEach {
                generatedPks += it.await()
            }


            val duplicates = generatedPks.groupBy { it.second }.values.filter { it.size > 1 }

            Assert.assertEquals("Generated expect number of ids",
                numItemsPerThread * numThreads, generatedPks.size)
            Assert.assertEquals("All items are unique. Duplicates=${duplicates.joinToString()}",
                0, duplicates.size)
        }
    }
}