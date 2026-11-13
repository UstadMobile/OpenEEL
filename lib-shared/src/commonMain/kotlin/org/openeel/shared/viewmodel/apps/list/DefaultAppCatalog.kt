package org.openeel.shared.viewmodel.apps.list

import io.ktor.http.Url

/**
 * Simple class wrapper so the dependency injection can identify it by class
 */
data class DefaultAppCatalog(
    val url: Url = Url("https://respect.directory/respect-ds/base.json")
)