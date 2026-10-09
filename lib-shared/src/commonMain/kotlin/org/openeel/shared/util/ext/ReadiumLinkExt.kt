package org.openeel.shared.util.ext

import io.github.aakira.napier.Napier
import io.ktor.http.Url
import org.openeel.lib.opds.model.ReadiumLink
import org.openeel.lib.opds.model.ext.hasRel
import org.openeel.libutil.ext.resolve

fun ReadiumLink.resolve(
    baseUrl: Url
): ReadiumLink {
    return try {
        copy(
            href = baseUrl.resolve(this.href).toString(),
            alternate = this.alternate?.resolveAll(baseUrl),
            children = this.children?.resolveAll(baseUrl),
            subcollections = this.subcollections?.resolveAll(baseUrl),
        )
    }catch(e: Throwable) {
        //If this fails, the validation would have failed. Notwithstanding, we don't want to crash
        //the whole app.
        Napier.e("Resolving ReadiumLink from $baseUrl FAIL", throwable = e)
        this
    }
}

fun List<ReadiumLink>.resolveAll(
    baseUrl: Url
) = this.map { it.resolve(baseUrl) }

fun List<ReadiumLink>.firstSelfLinkOrNull(): ReadiumLink? {
    return firstOrNull { link ->
        link.hasRel("self")
    }
}
