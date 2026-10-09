package world.respect.datalayer.school.xapi.ext

fun <K: Any, V: Any> Map<K, V>.takeIfNotEmpty() = this.ifEmpty { null }

fun List<Map<String ,String>>.mergeLangMap() : Map<String, String>? {
    return buildMap {
        this@mergeLangMap.forEach {
            putAll(it)
        }
    }.takeIfNotEmpty()
}
