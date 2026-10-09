package world.respect.lib.test.clientservertest

import io.ktor.http.ContentType
import io.ktor.http.Url
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.routing.Routing
import io.ktor.server.routing.routing
import kotlinx.serialization.json.Json
import world.respect.lib.xapi.auth.GetAuthenticatedXapiAgentsUseCase
import world.respect.libutil.findFreePort
import java.io.File
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation as ContentNegotiationServer

data class EmbeddedDataSourceServerContext(
    val schoolUrl: Url,
    val datasourceContext: SchoolDbDataSourceContext,
)


suspend fun withEmbeddedDataSourceServer(
    dbDir: File,
    start: Boolean = true,
    json: Json = Json,
    getAuthenticatedXapiAgentsUseCase: GetAuthenticatedXapiAgentsUseCase? = null,
    routingConfig: Routing.(EmbeddedDataSourceServerContext) -> Unit,
    block: suspend EmbeddedDataSourceServerContext.() -> Unit
) {
    val port = findFreePort()
    val schoolUrl = Url("http://localhost:$port/")

    withSchoolDbDataSource(
        dbDir = dbDir,
        schoolUrl = schoolUrl,
        getAuthenticatedXapiAgentsUseCase = getAuthenticatedXapiAgentsUseCase,
    ) {
        datasource.insertAdminAndDefaultGrants(db)
        val context = EmbeddedDataSourceServerContext(
            schoolUrl = schoolUrl,
            datasourceContext = this
        )

        val server = embeddedServer(Netty, port = port) {
            install(ContentNegotiationServer) {
                json(json = json, contentType = ContentType.Application.Json)
            }

            routing {
                routingConfig(context)
            }
        }.also {
            if(start)
                it.start()
        }

        try {
            block(context)
        }finally {
            server.stop(gracePeriodMillis = 100)
        }

    }
}