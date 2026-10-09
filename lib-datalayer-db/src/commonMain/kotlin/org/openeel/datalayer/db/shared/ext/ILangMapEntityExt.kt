package world.respect.datalayer.db.shared.ext

import world.respect.datalayer.db.shared.entities.ILangMapEntity

val ILangMapEntity.langMapKey: String
    get() = if(region != null) {
        "$lang-$region"
    }else {
        lang
    }