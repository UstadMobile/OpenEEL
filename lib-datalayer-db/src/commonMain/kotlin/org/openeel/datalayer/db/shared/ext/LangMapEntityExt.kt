package world.respect.datalayer.db.shared.ext

import world.respect.datalayer.db.shared.entities.LangMapEntity

val LangMapEntity.langMapKey: String
    get() = if(lmeRegion != null) {
        "$lmeLang-$lmeRegion"
    }else {
        lmeLang
    }