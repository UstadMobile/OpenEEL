package org.openeel.datalayer.db.school.entities


data class LastModifiedAndPermission(
    val uidNum: Long,
    val lastModified: Long?,
    val hasPermission: Boolean,
)
