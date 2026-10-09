package com.ustadmobile.core.domain.storage

import world.respect.shared.domain.storage.OfflineStorageOption

interface GetOfflineStorageOptionsUseCase {

    operator fun invoke(): List<OfflineStorageOption>

    companion object {
        const val PREFKEY_OFFLINE_STORAGE = "offlineStoragePath"
    }

}