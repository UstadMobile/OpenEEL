package org.openeel.credentials.passkey

import android.os.Build
import io.ktor.http.Url
import org.openeel.datalayer.RespectAppDataSource
import org.openeel.lib.dataloadstate.ext.dataOrNull

class CheckPasskeySupportUseCaseAndroidImpl(
    private val verifyDomainUseCase: VerifyDomainUseCase,
    private val schoolUrl: Url,
    private val respectAppDataSource: RespectAppDataSource,
) : CheckPasskeySupportUseCase {

    override suspend fun invoke(): Boolean {
        if (Build.VERSION.SDK_INT < 28)
            return false

        val schoolDirEntry = respectAppDataSource.schoolDirectoryEntryDataSource
            .getSchoolDirectoryEntryByUrl(schoolUrl).dataOrNull() ?: return false

        val rpId = schoolDirEntry.rpId ?: return false

        return verifyDomainUseCase(rpId)
    }

}