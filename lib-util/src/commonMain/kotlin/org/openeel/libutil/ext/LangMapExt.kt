package world.respect.libutil.ext

fun Map<String, String>?.isNullOrAllBlank(): Boolean {
    return this == null || this.entries.all { it.value.isBlank() }
}
