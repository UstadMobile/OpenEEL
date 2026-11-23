package org.openeel.datalayer.db.school.opds.crawlpublications

import app.cash.turbine.ReceiveTurbine

suspend fun <T: Any> ReceiveTurbine<T>.awaitItemWhere(
    predicate: (T) -> Boolean
): T {
    while(true) {
        val item = awaitItem()
        if(predicate(item))
            return item
    }
}
