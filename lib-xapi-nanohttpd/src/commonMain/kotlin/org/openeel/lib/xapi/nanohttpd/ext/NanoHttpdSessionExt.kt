package org.openeel.lib.xapi.nanohttpd.ext

import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.NanoHTTPD.Method
import java.io.File

fun NanoHTTPD.IHTTPSession.bodyAsBytes(): ByteArray? {
    val bodyMap = mutableMapOf<String,String>()
    parseBody(bodyMap)

    return if(method == Method.PUT) {
        //NanoHTTPD will always put the content of a PUT body into a temp file, with the path in the "content" key
        val tmpFileName = bodyMap["content"]
        tmpFileName?.let { File(it).readBytes() }
    }else if(method == Method.POST) {
        //NanoHTTPD will put small (less than 1024 bytes) content into the memory, otherwise it will make a file
        val mapContent = bodyMap["postData"] ?: return null
        val tmpFile = File(mapContent)
        if(tmpFile.exists()) {
            tmpFile.readBytes()
        }else {
            mapContent.encodeToByteArray()
        }
    }else {
        null
    }
}