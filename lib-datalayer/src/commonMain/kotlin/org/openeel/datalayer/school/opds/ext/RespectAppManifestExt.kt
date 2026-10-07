package org.openeel.datalayer.school.opds.ext

import org.openeel.datalayer.compatibleapps.model.RespectAppManifest
import org.openeel.lib.opds.model.Publication
import org.openeel.lib.opds.model.REL_RESPECT_DEFAULT_CATALOG
import org.openeel.lib.opds.model.ReadiumLink
import org.openeel.lib.opds.model.ReadiumMetadata

fun RespectAppManifest.asOpdsPublication(): Publication {
    return Publication(
        metadata = ReadiumMetadata(
            title = name,
        ),
        links = listOf(
            ReadiumLink(
                href = learningUnits.toString(),
                rel = listOf(REL_RESPECT_DEFAULT_CATALOG),
                type = "application/opds+json",
            )
        ),
        images = icon?.let {
            listOf(ReadiumLink(href = it.toString()))
        } ?: emptyList()
    )
}
