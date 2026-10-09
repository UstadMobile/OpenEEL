package world.respect.xapi.ipc.server

import android.os.Message
import android.util.Log
import io.ktor.http.URLBuilder
import io.ktor.http.Url
import io.ktor.util.collections.ConcurrentMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import net.thauvin.erik.urlencoder.UrlEncoderUtil
import org.openeel.lib.ipc.messagebridge.IpcMessageBridgeWhatFlags
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.dataloadstate.ext.toPrettyString
import world.respect.lib.xapi.OpenEelXapiConstants
import world.respect.lib.xapi.XapiResourceProvider
import world.respect.lib.xapi.exceptions.XapiException
import world.respect.lib.xapi.ext.asAssignmentRecipeStmtIfIdNotNull
import world.respect.lib.xapi.model.XapiStatement
import world.respect.lib.xapi.model.XapiStatementResult
import world.respect.lib.xapi.resources.XapiStatementsResource
import world.respect.libutil.ext.normalizeForEndpoint
import world.respect.xapi.ipc.shared.messages.XapiIpcKeys
import world.respect.xapi.ipc.server.ext.provideResource
import world.respect.xapi.ipc.server.ext.removeCollectorIfFlowCompleted
import world.respect.xapi.ipc.shared.messages.XapiIpcMethodEnum
import world.respect.xapi.ipc.shared.messages.XapiIpcResourceAndMethod
import world.respect.xapi.ipc.shared.messages.XapiIpcTags
import world.respect.xapi.ipc.shared.messages.ext.getDataLoadParams
import world.respect.xapi.ipc.shared.messages.ext.getDeserialized
import world.respect.xapi.ipc.shared.messages.ext.getXapiIpcQueryParameters
import world.respect.xapi.ipc.shared.messages.ext.orEmpty
import world.respect.xapi.ipc.shared.messages.ext.sendResponseMessage
import world.respect.xapi.ipc.shared.messages.ext.toBundle
import kotlin.collections.set
import kotlin.uuid.Uuid

class StatementsResourceIncomingHandler(
    private val xapiResourceProvider: XapiResourceProvider,
    private val json: Json,
    private val scope: CoroutineScope,
): XapiIpcResourceIncomingHandler {

    private val flowCollectors = ConcurrentMap<Int, Job>()

    override fun handleMessage(msg: Message) {
        val endpoint = msg.data.getString(XapiIpcKeys.KEY_ENDPOINT)?.let {
            Url(it)
        } ?: throw IllegalArgumentException("Message has no endpoint")

        if(flowCollectors.removeCollectorIfFlowCompleted(msg)) {
            return
        }

        val assignmentSegmentIndex = endpoint.segments.indexOf(
            OpenEelXapiConstants.ASSIGNMENT_XAPI_SEGMENT
        )

        val assignmentActivityId = if(assignmentSegmentIndex >= 0) {
            UrlEncoderUtil.decode(endpoint.segments[assignmentSegmentIndex + 1])
        }else {
            null
        }

        val logPrefix = "XapiIpcService  msg #${msg.arg1})"

        val scopeEndpoint = if(assignmentActivityId != null){
            URLBuilder(endpoint).apply {
                //Builder adds a blank segment at the beginning, so this needs done again
                val segmentIndex = pathSegments.indexOf(
                    OpenEelXapiConstants.ASSIGNMENT_XAPI_SEGMENT
                )

                pathSegments = pathSegments.filterIndexed { index, _ ->
                    index != segmentIndex && index != (segmentIndex + 1)
                }

                normalizeForEndpoint()
            }.build()
        }else {
            endpoint
        }

        val dataBundle = msg.data
        val replyTo = msg.replyTo
        val incomingMessageId = msg.arg1

        val xapiResource = runBlocking {
            xapiResourceProvider.provideResource(
                bundle = dataBundle,
                endpoint = scopeEndpoint,
            )
        }

        when(XapiIpcResourceAndMethod.fromArg2Int(msg.arg2).method) {
            XapiIpcMethodEnum.GET -> {
                Log.d(XapiIpcTags.LOGTAG, "$logPrefix: get ")
                scope.launch {
                    replyTo.sendResponseMessage(
                        what = IpcMessageBridgeWhatFlags.WHAT_RESPONSE,
                        messageId = incomingMessageId,
                        data = xapiResource.statements.get(
                            listParams = XapiStatementsResource.GetStatementParams.fromParams(
                                params = dataBundle.getXapiIpcQueryParameters().orEmpty(),
                                json = json
                            ),
                            dataLoadParams = dataBundle.getDataLoadParams(),
                        ).also {
                            Log.d(XapiIpcTags.LOGTAG, "$logPrefix get: response ${it.toPrettyString()}")
                        }.toBundle(XapiStatementResult.serializer(), json),
                    )
                }
            }

            XapiIpcMethodEnum.GET_AS_FLOW -> {
                scope.launch {
                    Log.d(XapiIpcTags.LOGTAG, "$logPrefix #$incomingMessageId getAsFlow")
                    xapiResource.statements.getAsFlow(
                        listParams = XapiStatementsResource.GetStatementParams.fromParams(
                            params = dataBundle.getXapiIpcQueryParameters().orEmpty(),
                            json = json
                        ),
                        dataLoadParams = dataBundle.getDataLoadParams(),
                    ).collect {
                        replyTo.sendResponseMessage(
                            what = IpcMessageBridgeWhatFlags.WHAT_FLOW_EMISSION,
                            messageId = incomingMessageId,
                            data = it.toBundle(XapiStatementResult.serializer(), json),
                        )
                        Log.d(
                            XapiIpcTags.LOGTAG,
                            "$logPrefix getAsFlow emit ${it.toPrettyString()}"
                        )
                    }
                }.also {
                    flowCollectors[incomingMessageId] = it
                }
            }

            XapiIpcMethodEnum.POST -> {
                Log.d(XapiIpcTags.LOGTAG, "$logPrefix: post ")
                scope.launch {
                    replyTo.sendResponseMessage(
                        what = IpcMessageBridgeWhatFlags.WHAT_RESPONSE,
                        messageId = incomingMessageId,
                        data = xapiResource.statements.post(
                            list = dataBundle.getDeserialized(
                                key = XapiIpcKeys.KEY_BODY,
                                json = json,
                                deserializer = ListSerializer(
                                    XapiStatement.serializer()
                                ),
                            )?.also {
                                Log.d(
                                    XapiIpcTags.LOGTAG,
                                    "$logPrefix post send ${it.size} statements"
                                )
                            }?.map {
                                it.asAssignmentRecipeStmtIfIdNotNull(assignmentActivityId)
                            } ?: throw XapiException(400, "Post statements has no body")
                        ).also {
                            Log.d(XapiIpcTags.LOGTAG, "$logPrefix post: response ${it.toPrettyString()}")
                        }.toBundle(ListSerializer(Uuid.serializer()), json),
                    )
                }
            }

            else -> {
                //Bad request
            }
        }
    }
}