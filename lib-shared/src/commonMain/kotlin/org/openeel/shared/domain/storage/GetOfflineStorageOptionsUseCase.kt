package com.ustadmobile.core.domain.storage

import org.openeel.shared.domain.storage.OfflineStorageOption

interface GetOfflineStorageOptionsUseCase {

    operator fun invoke(): List<OfflineStorageOption>

    companion object {
        const val PREFKEY_OFFLINE_STORAGE = "offlineStoragePath"
    }

}