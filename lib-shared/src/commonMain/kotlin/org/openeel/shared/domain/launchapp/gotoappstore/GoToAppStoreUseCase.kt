package world.respect.shared.domain.launchapp.gotoappstore

import world.respect.lib.opds.model.Publication
import world.respect.lib.opds.model.ReadiumLink

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