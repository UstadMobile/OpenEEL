package org.openeel.datalayer.db.school.xapi.ext

import org.openeel.datalayer.db.school.xapi.entities.XapiActivityInteractionEntityPropEnum
import org.openeel.datalayer.db.school.xapi.entities.XapiActivityLangMapEntryPropEnum

val XapiActivityInteractionEntityPropEnum.langMapPropEnum: XapiActivityLangMapEntryPropEnum
    get() = when(this) {
        XapiActivityInteractionEntityPropEnum.CHOICES -> XapiActivityLangMapEntryPropEnum.CHOICES_INTERACTIONS
        XapiActivityInteractionEntityPropEnum.SCALE -> XapiActivityLangMapEntryPropEnum.SCALE_INTERACTIONS
        XapiActivityInteractionEntityPropEnum.SOURCE -> XapiActivityLangMapEntryPropEnum.SOURCE_INTERACTIONS
        XapiActivityInteractionEntityPropEnum.TARGET -> XapiActivityLangMapEntryPropEnum.TARGET_INTERACTIONS
        XapiActivityInteractionEntityPropEnum.STEPS -> XapiActivityLangMapEntryPropEnum.STEPS_INTERACTIONS
    }
