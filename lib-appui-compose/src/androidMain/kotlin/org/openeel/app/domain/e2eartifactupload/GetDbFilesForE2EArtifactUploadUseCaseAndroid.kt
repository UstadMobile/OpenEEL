package world.respect.app.domain.e2eartifactupload

import android.content.Context
import io.ktor.http.Url
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import world.respect.shared.domain.school.SchoolDbPath
import world.respect.shared.domain.e2eartifactupload.DbFileForUpload
import world.respect.shared.domain.e2eartifactupload.GetDbFilesForE2EArtifactUploadUseCase

class GetDbFilesForE2EArtifactUploadUseCaseAndroid(
    private val context: Context,
) : GetDbFilesForE2EArtifactUploadUseCase {

    override suspend fun invoke(schoolUrl: Url): DbFileForUpload? {
        val schoolDbPath = SchoolDbPath.forSchoolUrl(schoolUrl)
        val dbFile = context.getDatabasePath(schoolDbPath.filename)
        if (!dbFile.exists()) return null
        return withContext(Dispatchers.IO) {
            DbFileForUpload(filename = schoolDbPath.filename, bytes = dbFile.readBytes())
        }
    }
}
