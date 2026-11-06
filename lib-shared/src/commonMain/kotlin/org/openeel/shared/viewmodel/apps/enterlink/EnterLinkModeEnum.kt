package org.openeel.shared.viewmodel.apps.enterlink

enum class EnterLinkModeEnum(val value: String) {
    APP_OR_PLAYLIST("app_or_playlist"),
    HOST("host");

    companion object {

        fun fromValue(value: String): EnterLinkModeEnum = entries.first { it.value == value }

    }
}
