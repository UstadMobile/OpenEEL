package world.respect.server.routes.e2etestartifactsroute

import io.github.aakira.napier.Napier
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import world.respect.server.util.ext.HttpStatusException
import world.respect.shared.domain.e2eartifactupload.E2EArtifactUploadUseCase
import java.io.File
import java.sql.DriverManager
import world.respect.shared.domain.school.SchoolDbPath


@Suppress("FunctionName")
fun Route.ReceiveE2EArtifactUploadRoute(e2eUploadsDir: File) {
    e2eUploadsDir.mkdirs()
    post(E2EArtifactUploadUseCase.ENDPOINT_RECEIVE) {
        Napier.d(tag = E2EArtifactUploadUseCase.LOGTAG, message = "Receive")
        val artifactName = call.request.queryParameters[E2EArtifactUploadUseCase.PARAM_NAME_ARTIFACT_NAME]
            ?: throw HttpStatusException("No artifact name", statusCode = HttpStatusCode.BadRequest)

        if (!artifactName.all { it.isLetterOrDigit() || it == '_' || it == '-' }) {
            call.respond(HttpStatusCode.BadRequest, "name must contain only letters, digits, or underscores")
            return@post
        }

        val tempFile = File(e2eUploadsDir, "$artifactName.tmp")
        tempFile.writeBytes(call.receive<ByteArray>())

        if (!tempFile.isValidSQLiteDb()) {
            tempFile.delete()
            call.respond(HttpStatusCode.BadRequest, "corrupt SQLite database")
            return@post
        }

        val dbFile = File(e2eUploadsDir, "$artifactName${SchoolDbPath.DB_EXTENSION}")
        tempFile.renameTo(dbFile)
        Napier.i(tag = E2EArtifactUploadUseCase.LOGTAG, message = "Received: $artifactName saved to ${dbFile.absolutePath}")
        call.respond(HttpStatusCode.OK)
    }
}

private fun File.isValidSQLiteDb(): Boolean = try {
    DriverManager.getConnection("jdbc:sqlite:$absolutePath").use { conn ->
        conn.createStatement().use { it.execute("PRAGMA integrity_check") }
    }
    true
} catch (_: Exception) {
    false
}