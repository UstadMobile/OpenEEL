package world.respect.lib.opds.model

import world.respect.lib.opds.model.ext.filterByHasRel
import world.respect.lib.opds.model.ext.hasRel

val LEARNING_UNIT_MIME_TYPES = listOf("text/html", "application/xml", "application/html+xml")

/**
 * Relationship for app opds link indicating a default lesson catalog
 */
const val REL_RESPECT_DEFAULT_CATALOG = "https://respect.ustadmobile.com/ns/default-lesson-catalog"

/**
 * Relationship for a link to a launchable app manifest
 */
const val REL_LAUNCHABLE_APP = "https://id.openeel.org/rel/launchable-app"

/**
 * Relationship for a link to a tincan.xml file
 */
const val REL_TINCAN_XML = "https://id.openeel.org/rel/tincanxml"

fun Publication.findLearningUnitAcquisitionLinks(): List<ReadiumLink> {
    return links.filter { link ->
        link.rel?.any {
            it.startsWith("http://opds-spec.org/acquisition") } == true &&
                LEARNING_UNIT_MIME_TYPES.any { link.type?.startsWith(it) == true
        }
    }
}

fun Publication.findSelfLinks(): List<ReadiumLink> {
    return links.filterByHasRel("self")
}

fun Publication.findHighlightCardLinks(): List<ReadiumLink> {
    return links.filter {
        it.rel?.contains("https://id.openeel.org/rel/app-highlight-card") == true
    }
}

fun Publication.findLicenseLink(): ReadiumLink? =
    links.firstOrNull { it.rel?.contains("license") == true }

fun Publication.findTermsOfServiceLink(): ReadiumLink? =
    links.firstOrNull { it.rel?.contains("terms-of-service") == true }

fun Publication.findAppStoreAndroidLinks() : List<ReadiumLink> {
    return links.filter {
        it.hasRel(OpenEelConstants.REL_ANDROID_APP_STORE)
    }
}

fun Publication.findAppStoreAndroidLink(): ReadiumLink? =
    links.firstOrNull { it.rel?.contains(OpenEelConstants.REL_ANDROID_APP_STORE) == true }

fun Publication.findCollection(): ReadiumLink? =
    links.firstOrNull {
        it.rel?.contains("collection") == true
    } ?: respectAppManifestDefaultLessonList()


fun Publication.findLaunchableAppLink(): ReadiumLink? =
    links.firstOrNull { it.rel?.contains(REL_LAUNCHABLE_APP) == true }

fun Publication.findTinCanXmlLink(): ReadiumLink? =
    links.firstOrNull { it.rel?.contains(REL_TINCAN_XML) == true }

fun Publication.findIcons(): List<ReadiumLink> {
    return images ?: emptyList()
}

fun Publication.respectAppManifestDefaultLessonList(): ReadiumLink? {
    return links.firstOrNull {
        REL_RESPECT_DEFAULT_CATALOG in (it.rel ?: emptyList())
    }
}
