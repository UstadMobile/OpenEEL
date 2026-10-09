package world.respect.libutil.ext

import io.ktor.http.ParametersBuilder
import io.ktor.util.StringValuesBuilder
import kotlin.uuid.Uuid


fun StringValuesBuilder.appendIfNotNull(
    name: String,
    value: String?
) {
    value?.also { append(name, it) }
}

fun ParametersBuilder.appendIfNotNull(
    name: String,
    value: Uuid?
) {
    value?.also { append(name, it.toString()) }
}
