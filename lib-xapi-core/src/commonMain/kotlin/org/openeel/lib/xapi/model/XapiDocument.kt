package world.respect.lib.xapi.model

import io.ktor.util.date.GMTDate

/**
 * Xapi Document: this interface can wrap different types: e.g. entities on the database that are
 * using a bytearray, an http response, etc.
 *
 * See
 * https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Communication.md#22-document-resources
 *
 * As per:
 * https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Communication.md#part-three-data-processing-validation-and-security
 * "An LRS responding to a GET request without using a transfer encoding or using the identity transfer encoding MUST calculate the value of the ETag header to be a hexadecimal string of the SHA-1 digest of the contents"
 */
interface XapiDocument {

    val type: String

    /**
     * The updated property as per the specification "is HTTP header information". The HTTP
     * Last-Modified header precision is limited to seconds (cannot include milliseconds/nanoseconds),
     * hence this is of type GMTDate NOT Instant.
     */
    val updated: GMTDate

    suspend fun contentsAsByteArray(): ByteArray

}