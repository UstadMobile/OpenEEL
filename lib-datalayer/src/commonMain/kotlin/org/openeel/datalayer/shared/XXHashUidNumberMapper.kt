package org.openeel.datalayer.shared

import org.openeel.datalayer.UidNumberMapper
import org.openeel.libxxhash.XXStringHasher

class XXHashUidNumberMapper(
    val xxStringHasher: XXStringHasher
): UidNumberMapper {

    override fun invoke(uid: String): Long  = xxStringHasher.hash(uid)

}