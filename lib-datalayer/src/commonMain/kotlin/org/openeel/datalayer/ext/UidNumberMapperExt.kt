package org.openeel.datalayer.ext

import org.openeel.datalayer.UidNumberMapper

/**
 * Shorthand function to run the uid number mapper where the uid is not null, or return zero when
 * it is null.
 */
fun UidNumberMapper.uidOrZero(uid: String?) = if(uid != null) invoke(uid) else 0
