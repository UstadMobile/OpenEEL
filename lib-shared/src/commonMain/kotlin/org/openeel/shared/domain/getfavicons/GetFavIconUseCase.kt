package world.respect.domain.getfavicons

import io.ktor.http.Url

/**
 * Get the favicon(s) for a given URL.
 *
 * See
 * https://www.w3schools.com/html/html_favicon.asp
 */
interface GetFavIconUseCase {

    data class FavIcon(
        val url: String,
        val type: String?,
        val sizes: String?,
        val width: Int?,
        val height: Int?,
    )

    suspend operator fun invoke(url: Url): List<FavIcon>

}