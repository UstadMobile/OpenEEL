package world.respect.libxxhash.jvmimpl

import net.jpountz.xxhash.StreamingXXHash64
import world.respect.libxxhash.XXHasher64

class XXHasher64CommonJvm(
    private val xxHasher: StreamingXXHash64
): XXHasher64 {

    override fun update(data: ByteArray) {
        xxHasher.update(data, 0, data.size)
    }

    override fun digest(): Long {
        return xxHasher.value
    }
}