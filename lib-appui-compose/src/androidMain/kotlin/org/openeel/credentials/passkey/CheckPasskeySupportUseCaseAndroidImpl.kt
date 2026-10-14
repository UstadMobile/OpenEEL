package org.openeel.credentials.passkey

import android.os.Build
import io.ktor.http.Url
import org.openeel.datalayer.SchoolDirectoryDataSource
import org.openeel.lib.dataloadstate.ext.dataOrNull

class CheckPasskeySupportUseCaseAndroidImpl(
    private val verifyDomainUseCase: VerifyDomainUseCase,
    private val schoolUrl: Url,
    private val schoolDirectoryDataSource: SchoolDirectoryDataSource,
) : CheckPasskeySupportUseCase {

    override suspend fun invoke(): Boolean {
        if (Build.VERSION.SDK_INT < 28)
            return false

        val schoolDirEntry = this@CheckPasskeySupportUseCaseAndroidImpl.schoolDirectoryDataSource.schoolDirectoryEntryResource
            .getSchoolDirectoryEntryByUrl(schoolUrl).dataOrNull() ?: return false

        val rpId = schoolDirEntry.rpId ?: return false

        return verifyDomainUseCase(rpId)
    }

}