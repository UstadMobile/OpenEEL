package world.respect.datalayer.db.schooldirectory.ext

import world.respect.datalayer.respect.model.SchoolDirectoryEntry

val SchoolDirectoryEntry.virtualHostScopeId: String
    get() = self.toString()


