package world.respect.libxxhash

interface XXHasher64 {

    fun update(data: ByteArray)

    fun digest(): Long

}