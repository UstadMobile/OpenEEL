package world.respect.datalayer.shared

import world.respect.datalayer.UidNumberMapper
import world.respect.libxxhash.XXStringHasher

class XXHashUidNumberMapper(
    val xxStringHasher: XXStringHasher
): UidNumberMapper {

    override fun invoke(uid: String): Long  = xxStringHasher.hash(uid)

}