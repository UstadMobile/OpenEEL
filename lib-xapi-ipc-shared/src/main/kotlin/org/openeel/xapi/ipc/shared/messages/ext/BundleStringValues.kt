package world.respect.xapi.ipc.shared.messages.ext

import android.os.Bundle
import io.ktor.util.StringValues

/**
 * StringValues implementation based on an Android bundle. Everything in the bundle MUST be a
 * string array as per StringValuesExt.toBundle()
 */
open class BundleStringValues(
    val bundle: Bundle,
    override val caseInsensitiveName: Boolean = false,
) : StringValues{

    override fun entries(): Set<Map.Entry<String, List<String>>> {
        return bundle.keySet().associateWith { key ->
            (bundle.getStringArray(key)?.toList() ?: emptyList())
        }.entries
    }

    override fun getAll(name: String): List<String>? {
        return if(!caseInsensitiveName) {
            bundle.getStringArray(name)?.toList()
        }else {
            val matchingKeys = bundle.keySet().filter { it.equals(name ,true) }

            matchingKeys.takeIf { it.isNotEmpty() }?.flatMap {
                bundle.getStringArray(it)?.toList() ?: emptyList()
            }
        }
    }

    override fun isEmpty(): Boolean {
        return bundle.keySet().isEmpty()
    }

    override fun names(): Set<String> {
        return bundle.keySet()
    }

}