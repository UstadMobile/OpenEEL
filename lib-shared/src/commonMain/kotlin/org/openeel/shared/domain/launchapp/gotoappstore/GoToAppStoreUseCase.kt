package org.openeel.shared.domain.launchapp.gotoappstore

import org.openeel.lib.opds.model.Publication
import org.openeel.lib.opds.model.ReadiumLink

interface GoToAppStoreUseCase {

    data class Request(
        val launchableApp: Publication,
        val referrer: String,
        val preferredStoreLink: ReadiumLink? = null,
    )

    suspend operator fun invoke(
        request: Request,
    )

}