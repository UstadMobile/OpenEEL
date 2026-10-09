package world.respect.lib.xapi

object OpenEelXapiConstants {

    /**
     * When an xAPI activity is to be completed as part of an assignment it needs to include the
     * assignment activity ID in the contextActivities as per the assignment recipe. Making all
     * launchable apps change the way they generate statements would be burdensome.
     *
     * Therefor the launcher uses a pattern where the local xAPI url is modified to include the
     * assignment activity id in the path segments and the local server modifies the statement
     * received to put the assignment activity id in the contextActivities.grouping list.
     *
     * eg. https://school.example.org/xAPI/openeel_assignment/assignment-id-encoded/
     *
     */
    const val ASSIGNMENT_XAPI_SEGMENT = "openeel_assignment"

    const val HEADER_XAPI_VERSION = "X-Experience-API-Version"

    /**
     * Required header on get statement results
     *
     * As per
     * https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Communication.md#213-get-statements
     */
    const val HEADER_XAPI_CONSISTENT_THROUGH = "X-Experience-API-Consistent-Through"

    /**
     * As per README_EXTENSIONS_PUBLICATION.md
     */
    const val ACTIVITY_EXTENSION_WEBPUB_MANIFEST_LINK = "https://id.openeel.org/extensions/activity/webpub-manifest-link"

    const val ACTIVITY_EXTENSION_DEADLINE = "https://id.ustadmobile.com/xapi/extension/deadline"

    const val CATEGORY_ASSIGNMENT_RECIPE = "https://id.ustadmobile.com/xapi/activities/assignment-recipe"

    const val CATEGORY_APP_LISTING_RECIPE = "https://id.openeel.org/recipes/applisting"

    const val CATEGORY_BOOKMARK_RECIPE = "https://id.openeel.org/recipes/bookmark"

    const val ACTIVITY_PROFILEID_OPDS_COLLECTION = "https://id.openeel.org/profile/activity/opds-collection"

}