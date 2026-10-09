package world.respect.app.domain.e2eartifactupload

import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.lifecycle.lifecycleScope
import com.ustadmobile.libuicompose.theme.RespectAppTheme
import io.ktor.http.Url
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.getKoin
import world.respect.app.view.testing.SendDbToServerScreen
import world.respect.app.view.testing.SendDbToServerUiState
import world.respect.shared.domain.school.SchoolDbPath
import world.respect.shared.domain.e2eartifactupload.E2EArtifactUploadUseCase
import world.respect.shared.domain.e2eartifactupload.E2EArtifactUploadUseCase.Companion.PARAM_NAME_SCHOOL_URL
import world.respect.shared.util.di.SchoolDirectoryEntryScopeId

/**
 * The end-to-end artifact uploading (see E2EArtifactUploadUseCase) needs to be done when everything
 * else is closed and stopped so artifacts (eg the database) can be uploaded without corruption due
 * to changes happening mid-copy/upload.
 */
class E2EArtifactUploadActivity : AppCompatActivity() {

    private val _uiState = MutableStateFlow(SendDbToServerUiState())

    @OptIn(ExperimentalComposeUiApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val uiStateVal by _uiState.collectAsState()
            RespectAppTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .semantics { testTagsAsResourceId = true },
                    color = MaterialTheme.colorScheme.background,
                ) {
                    SendDbToServerScreen(uiStateVal)
                }
            }
        }

        val uri = intent.data
        val schoolUrlStr = uri?.getQueryParameter(PARAM_NAME_SCHOOL_URL)
        val artifactName = uri?.getQueryParameter(E2EArtifactUploadUseCase.PARAM_NAME_ARTIFACT_NAME)

        if(schoolUrlStr != null && artifactName != null) {
            lifecycleScope.launch {
                try {
                    Log.d(E2EArtifactUploadUseCase.LOGTAG, "Starting e2e uploads: schoolurl=$schoolUrlStr artifactName=$artifactName")
                    upload(Url(schoolUrlStr), artifactName)
                    _uiState.update { SendDbToServerUiState(isLoading = false) }
                } catch (e: Exception) {
                    _uiState.update { SendDbToServerUiState(isLoading = false, errorMessage = e.message) }
                }
            }
        }else {
            _uiState.update {
                SendDbToServerUiState(
                    isLoading = false,
                    errorMessage = "ERR: School URL and/or artifact name not specified: uri=$uri schoolUrl=$schoolUrlStr artifactName=$artifactName"
                )
            }
        }
    }

    private suspend fun upload(schoolUrl: Url, name: String) {
        val scopeId = SchoolDirectoryEntryScopeId(schoolUrl, null).scopeId
        if (getKoin().getScopeOrNull(scopeId) != null)
            throw IllegalStateException("school db scope must be closed")

        val dbFile = getDatabasePath(SchoolDbPath.forSchoolUrl(schoolUrl).filename)
        if (!dbFile.exists())
            throw IllegalStateException("School db file not found - ${dbFile.absolutePath}")

        withContext(Dispatchers.IO) {
            SQLiteDatabase.openDatabase(
                dbFile.absolutePath, null, SQLiteDatabase.OPEN_READWRITE
            ).use { db ->
                db.rawQuery(PRAGMA_WAL_CHECKPOINT, null).use { it.moveToFirst() }
            }
        }

        getKoin().get<E2EArtifactUploadUseCase>().invoke(schoolUrl, name)
    }

    companion object {

        private const val PRAGMA_WAL_CHECKPOINT = "PRAGMA wal_checkpoint(FULL)"

    }
}
