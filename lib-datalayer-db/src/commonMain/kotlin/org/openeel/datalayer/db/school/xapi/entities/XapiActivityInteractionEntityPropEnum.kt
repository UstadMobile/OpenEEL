package world.respect.datalayer.db.school.xapi.entities

/**
 * As per
 * https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Data.md#interaction-activities
 *
 * An Activity can contain properties: choices, scale, source, target, steps.
 *
 * @property flag the constant for this enum which is used by the Room type converter to set a column
 *           value in the table
 * @property listIsNullFlag flag used with ActivityEntity.actFlag to indicate that this property is null
 *           (omitted)
 */
enum class XapiActivityInteractionEntityPropEnum(
    val flag: Int,
    val listIsNullFlag: Int,
) {
    CHOICES(1, XapiActivityEntity.FLAG_CHOICES_NULL),
    SCALE(2, XapiActivityEntity.FLAG_SCALE_NULL),
    SOURCE(3, XapiActivityEntity.FLAG_SOURCE_NULL),
    TARGET(4, XapiActivityEntity.FLAG_TARGET_NULL),
    STEPS(5, XapiActivityEntity.FLAG_STEPS_NULL);

    companion object {

        fun fromFlag(flag: Int): XapiActivityInteractionEntityPropEnum {
            return entries.firstOrNull { it.flag == flag } ?: CHOICES
        }

    }
}
