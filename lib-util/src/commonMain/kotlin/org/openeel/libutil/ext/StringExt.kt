package world.respect.libutil.ext

fun String.requirePostfix(
    postFix: String,
    ignoreCase: Boolean = false
) = if(this.endsWith(postFix, ignoreCase)) this else "$this$postFix"
