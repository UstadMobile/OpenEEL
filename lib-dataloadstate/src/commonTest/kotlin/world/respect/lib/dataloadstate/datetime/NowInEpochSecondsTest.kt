package org.openeel.lib.dataloadstate.datetime

import io.ktor.http.fromHttpToGmtDate
import io.ktor.http.toHttpDate
import io.ktor.util.date.GMTDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

class NowInEpochSecondsTest {

    @Test
    fun givenNow_whenNowInEpochSecondsCalled_thenNanosecondsIsZero() {
        val now = Clock.System.now().roundToEpochSeconds()
        assertEquals(0, now.nanosecondsOfSecond)
    }

    @Test
    fun givenNow_whenNowInEpochSecondsCalled_thenWithinOneSecondOfClockSystemNow() {
        val clockNow = Clock.System.now()
        val now = Clock.System.now().roundToEpochSeconds()
        val diff = (clockNow - now).absoluteValue
        assertTrue(diff <= 2.seconds, "Expected diff ($diff) to be within 2 seconds")
    }


    @Test
    fun givenInstantNowRoundedToSeconds_whenConvertedToFromGMTDate_thenWillBeEqual() {
        val roundedNow = Clock.System.now().roundToEpochSeconds()
        val httpDate = GMTDate(roundedNow.toEpochMilliseconds()).toHttpDate()
        val fromHttpDate = Instant.fromEpochMilliseconds(httpDate.fromHttpToGmtDate().timestamp)
        assertEquals(roundedNow, fromHttpDate)
    }
}
