package world.respect.libutil.ext

fun <T> Set<T>.toggle(
    item: T,
): Set<T> = if (item in this) {
    this - item
} else {
    this + item
}
