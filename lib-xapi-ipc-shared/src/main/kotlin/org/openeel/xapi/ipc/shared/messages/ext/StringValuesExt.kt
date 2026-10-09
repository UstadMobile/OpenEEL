package world.respect.xapi.ipc.shared.messages.ext

import android.os.Bundle
import io.ktor.util.StringValues

fun StringValues.toBundle() : Bundle {
    return Bundle().also { bundle ->
        names().forEach { name ->
            bundle.putStringArray(name, this.getAll(name)?.toTypedArray() ?: emptyArray())
        }
    }
}


