package org.openeel.libxxhash

interface XXHasher64 {

    fun update(data: ByteArray)

    fun digest(): Long

}