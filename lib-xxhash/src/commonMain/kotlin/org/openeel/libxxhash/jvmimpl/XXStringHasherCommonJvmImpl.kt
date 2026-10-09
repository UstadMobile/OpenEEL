package world.respect.libxxhash.jvmimpl

import kotlinx.atomicfu.locks.ReentrantLock
import kotlinx.atomicfu.locks.withLock
import net.jpountz.xxhash.XXHashFactory
import world.respect.libxxhash.XXStringHasher

class XXStringHasherCommonJvm: XXStringHasher {

    private val factory = XXHashFactory.fastestJavaInstance()

    //As per https://github.com/Cyan4973/xxHash/blob/dev/doc/xxhash_spec.md
    //Default seed = 0
    private val hasher = factory.newStreamingHash64(0)

    private val lock = ReentrantLock()
    override fun hash(string: String): Long {

        return lock.withLock {
            val byteArray = string.toByteArray()
            hasher.update(byteArray, 0, byteArray.size)
            hasher.value.also { hasher.reset() }
        }

    }
}