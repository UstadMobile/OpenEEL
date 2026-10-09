package org.openeel.libxxhash

interface XXHasher64Factory {

    fun newHasher(seed: Long): XXHasher64

}