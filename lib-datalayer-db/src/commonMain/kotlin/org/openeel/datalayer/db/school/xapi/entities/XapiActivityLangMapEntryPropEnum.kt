package world.respect.datalayer.db.school.xapi.entities


/**
 * Enum class representing which property an LangMapEntry is for.
 */
enum class XapiActivityLangMapEntryPropEnum(
    val flag: Int,
) {

    NAME(1),
    DESCRIPTION(2),


    CHOICES_INTERACTIONS(3),

    SCALE_INTERACTIONS(4),

    SOURCE_INTERACTIONS(5),

    TARGET_INTERACTIONS(6),

    STEPS_INTERACTIONS(7);

    companion object {

        const val NAME_FLAG_INT = 1

        fun fromFlag(flag: Int): XapiActivityLangMapEntryPropEnum {
            return entries.firstOrNull { it.flag == flag } ?: NAME
        }

    }

}