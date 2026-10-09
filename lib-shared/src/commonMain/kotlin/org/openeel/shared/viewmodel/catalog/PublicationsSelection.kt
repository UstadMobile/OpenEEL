package world.respect.shared.viewmodel.catalog

import io.ktor.http.Url
import kotlinx.serialization.Serializable
import world.respect.lib.opds.model.Publication

/**
 * Represents learning unit(s) (OpdsPublication(s)) that have been selected on one screen to be
 * returned to another screen. This can be directly from a standalone OpdsPublication on its
 * own Url (e.g. from LearningUnitDetail screen) or selected publications from a catalog (List
 * screen)
 *
 * @param url the URL from which the selected publications were retrieved. In case of a standalone
 *        publication being selected, this will be the Url of the publication itself. In case of
 *        selections from a catalog, this will be the catalog's url.
 * @param selectedPublications the selected publications.
 */
@Serializable
data class PublicationsSelection(
    val url: Url,
    val selectedPublications: List<Publication>,
)
