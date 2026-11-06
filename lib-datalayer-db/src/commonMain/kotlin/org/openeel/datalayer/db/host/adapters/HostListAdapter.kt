package org.openeel.datalayer.db.host.adapters

import io.ktor.http.HttpHeaders
import io.ktor.http.Url
import io.ktor.http.headers
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.openeel.datalayer.db.host.entities.HostListEntity
import org.openeel.datalayer.respect.model.SchoolDirectory
import org.openeel.lib.dataloadstate.DataLoadMetaInfo
import org.openeel.lib.dataloadstate.DataReadyState

fun DataReadyState<List<SchoolDirectory>>.toEntity(url: Url, json: Json): HostListEntity =
    HostListEntity(
        url = url.toString(),
        hostsJson = json.encodeToString(data),
        etag = metaInfo.headers[HttpHeaders.ETag],
        lastModified = metaInfo.headers[HttpHeaders.LastModified],
    )

fun HostListEntity.toModel(json: Json): DataReadyState<List<SchoolDirectory>> =
    DataReadyState(
        data = json.decodeFromString(hostsJson),
        metaInfo = DataLoadMetaInfo(
            url = Url(url),
            headers = headers {
                etag?.let { append(HttpHeaders.ETag, it) }
                lastModified?.let { append(HttpHeaders.LastModified, it) }
            },
        ),
    )
