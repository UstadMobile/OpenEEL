package world.respect.shared.domain.getwarnings

import world.respect.shared.resources.UiText

/**
 * Use case that can, if needed, provider compatibility warnings/notices on known issues.
 */
interface GetWarningsUseCase {

    suspend operator fun invoke(): UiText?

}