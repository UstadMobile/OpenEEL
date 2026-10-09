package world.respect.datalayer.school.xapi.ext


class IntFlagBuilder internal constructor(
    private var flags: Int = 0,
) {

    fun putFlagIf(
        flag: Int,
        condition: Boolean,
    ) {
        if (condition) {
            flags = flags or flag
        }
    }

    fun build(): Int {
        return flags
    }
}

fun flagsOf(
    vararg flags: Pair<Int, Boolean>
): Int {
    return flags.fold(0) { flag, pair ->
        if(pair.second) {
            flag.or(pair.first)
        }else {
            flag
        }
    }
}


fun buildFlags(block: IntFlagBuilder.() -> Unit): Int {
    val builder = IntFlagBuilder()
    block(builder)
    return builder.build()
}