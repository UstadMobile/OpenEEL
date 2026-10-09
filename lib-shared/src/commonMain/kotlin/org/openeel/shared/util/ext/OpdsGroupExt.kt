package world.respect.shared.util.ext

import world.respect.lib.opds.model.OpdsGroup
import world.respect.shared.viewmodel.catalog.opdsfeededit.OpdsGroupType

val OpdsGroup.idOrNull: String?
    get() = this.metadata.identifier?.toString()

val OpdsGroup.groupType: OpdsGroupType
    get() = if(this.navigation != null) OpdsGroupType.NAVIGATION else OpdsGroupType.PUBLICATION
