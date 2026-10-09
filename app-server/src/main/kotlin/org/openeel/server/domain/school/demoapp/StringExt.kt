package world.respect.server.domain.school.demoapp

fun String.replacePlaceholders(
    gradeNum: Int,
    lessonNum: Int
): String {
    return this.replace("(gradeNum)", gradeNum.toString())
        .replace("(lessonNum)", lessonNum.toString())
}
