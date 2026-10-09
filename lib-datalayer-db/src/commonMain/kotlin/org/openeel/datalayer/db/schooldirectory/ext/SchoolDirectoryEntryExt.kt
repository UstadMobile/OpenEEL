package org.openeel.datalayer.db.schooldirectory.ext

import org.openeel.datalayer.respect.model.SchoolDirectoryEntry

val SchoolDirectoryEntry.virtualHostScopeId: String
    get() = self.toString()


