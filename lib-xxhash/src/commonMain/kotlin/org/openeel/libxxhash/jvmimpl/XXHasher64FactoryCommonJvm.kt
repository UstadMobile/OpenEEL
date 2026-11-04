package org.openeel.libxxhash.jvmimpl

import net.jpountz.xxhash.XXHashFactory
import org.openeel.libxxhash.XXHasher64
import org.openeel.libxxhash.XXHasher64Factory

class XXHasher64FactoryCommonJvm(): XXHasher64Factory {

    private val factory = XXHashFactory.fastestJavaInstance()

    override fun newHasher(seed: Long): XXHasher64 {
        return XXHasher64CommonJvm(factory.newStreamingHash64(seed))
    }

}