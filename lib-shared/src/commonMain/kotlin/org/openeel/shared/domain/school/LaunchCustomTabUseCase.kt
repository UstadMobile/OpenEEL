package world.respect.shared.domain.school

import io.ktor.http.Url

interface LaunchCustomTabUseCase {
    operator fun invoke(url: Url)
}