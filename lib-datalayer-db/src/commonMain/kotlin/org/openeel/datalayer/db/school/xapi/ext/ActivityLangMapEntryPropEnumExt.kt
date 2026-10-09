package world.respect.datalayer.db.school.xapi.ext

import world.respect.datalayer.db.school.xapi.entities.XapiActivityInteractionEntityPropEnum
import world.respect.datalayer.db.school.xapi.entities.XapiActivityLangMapEntryPropEnum

/**
 *
 */
val XapiActivityLangMapEntryPropEnum.isActivityProp: Boolean
    get() = this == XapiActivityLangMapEntryPropEnum.NAME ||
            this == XapiActivityLangMapEntryPropEnum.DESCRIPTION


val XapiActivityLangMapEntryPropEnum.interactionProp: XapiActivityInteractionEntityPropEnum?
    get() = when(this) {
        XapiActivityLangMapEntryPropEnum.CHOICES_INTERACTIONS -> XapiActivityInteractionEntityPropEnum.CHOICES
        XapiActivityLangMapEntryPropEnum.SCALE_INTERACTIONS -> XapiActivityInteractionEntityPropEnum.SCALE
        XapiActivityLangMapEntryPropEnum.SOURCE_INTERACTIONS -> XapiActivityInteractionEntityPropEnum.SOURCE
        XapiActivityLangMapEntryPropEnum.TARGET_INTERACTIONS -> XapiActivityInteractionEntityPropEnum.TARGET
        XapiActivityLangMapEntryPropEnum.STEPS_INTERACTIONS -> XapiActivityInteractionEntityPropEnum.STEPS
        else -> null
    }


