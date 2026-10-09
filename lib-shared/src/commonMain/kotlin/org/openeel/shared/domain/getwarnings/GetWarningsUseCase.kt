package org.openeel.shared.domain.getwarnings

import org.openeel.shared.resources.UiText

/**
 * Use case that can, if needed, provider compatibility warnings/notices on known issues.
 */
interface GetWarningsUseCase {

    suspend operator fun invoke(): UiText?

}