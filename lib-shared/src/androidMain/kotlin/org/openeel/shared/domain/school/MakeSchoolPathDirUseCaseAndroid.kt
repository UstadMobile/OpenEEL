package world.respect.shared.domain.school

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class MakeSchoolPathDirUseCaseAndroid(
    private val schoolPath: RespectSchoolPath
): MakeSchoolPathDirUseCase {

    override suspend fun invoke() {
        withContext(Dispatchers.IO) {
            File(schoolPath.path.toString()).takeIf { !it.exists() }?.mkdirs()
        }
    }
}