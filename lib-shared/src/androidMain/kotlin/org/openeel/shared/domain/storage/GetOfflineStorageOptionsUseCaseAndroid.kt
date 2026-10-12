package org.openeel.shared.domain.storage

import com.ustadmobile.core.domain.storage.GetOfflineStorageOptionsUseCase
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.memory_card
import org.openeel.shared.generated.resources.phone_memory

class GetOfflineStorageOptionsUseCaseAndroid(
    private val getAndroidSdCardDirUseCase: GetAndroidSdCardDirUseCase
) : GetOfflineStorageOptionsUseCase {


    private val internalStorage = OfflineStorageOption(
        label = Res.string.phone_memory,
        value = INTERNAL,
    )

    override fun invoke(): List<OfflineStorageOption> {
        return if(getAndroidSdCardDirUseCase() != null) {
            listOf(
                internalStorage,
                OfflineStorageOption(Res.string.memory_card, EXTERNAL),
            )
        }else {
            listOf(internalStorage)
        }
    }

    companion object {

        const val INTERNAL = "internal"

        const val EXTERNAL = "external"

    }
}