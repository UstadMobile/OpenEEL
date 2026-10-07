package org.openeel.shared.util.ext

import org.openeel.lib.opds.model.OpdsGroup
import org.openeel.shared.viewmodel.catalog.opdsfeededit.OpdsGroupType

val OpdsGroup.idOrNull: String?
    get() = this.metadata.identifier?.toString()

val OpdsGroup.groupType: OpdsGroupType
    get() = if(this.navigation != null) OpdsGroupType.NAVIGATION else OpdsGroupType.PUBLICATION
