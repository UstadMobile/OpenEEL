package world.respect.shared.ext

fun <T> List<T>.replace(
    element: T,
    replacePredicate: (T) -> Boolean,
): List<T> {
    val replaceIndex = indexOfFirst(replacePredicate)
    if(replaceIndex == -1)
        throw IllegalArgumentException("element to replace not found")

    return toMutableList().also {
        it[replaceIndex] = element
    }.toList()
}