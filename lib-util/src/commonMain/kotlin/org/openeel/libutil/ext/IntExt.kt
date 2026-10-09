package world.respect.libutil.ext

fun Int.pad0(): String {
    return if(this < 10)
        "0$this"
    else
        this.toString()
}