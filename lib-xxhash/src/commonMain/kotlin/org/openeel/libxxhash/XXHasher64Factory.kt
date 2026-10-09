package world.respect.libxxhash

interface XXHasher64Factory {

    fun newHasher(seed: Long): XXHasher64

}