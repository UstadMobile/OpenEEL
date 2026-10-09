package world.respect.lib.xapi.ext

import com.eygraber.uri.Uri
import world.respect.lib.xapi.exceptions.XapiException

fun xapiRequireValidIRIOrNull(iri: String?, errorMessage: String = "Invalid IRI:") : String? {
    if(iri != null) {
        try {
            Uri.parse(iri)
        }catch(e: Throwable) {
            throw XapiException(400, "$errorMessage: ${e.message}", e)
        }
    }

    return iri
}

fun xapiRequireValidIRI(iri: String?, errorMessage: String = "Invalid iri") : String {
    if(iri == null)
        throw XapiException(400, "$errorMessage: iri is null")

    xapiRequireValidIRIOrNull(iri)
    return iri
}

