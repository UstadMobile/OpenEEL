package org.openeel.server.util.ext

fun String?.splitCommaSeparatedValues(): List<String> =
    this?.split(',')
        ?.map(String::trim)
        ?.filter(String::isNotEmpty)
        .orEmpty()
