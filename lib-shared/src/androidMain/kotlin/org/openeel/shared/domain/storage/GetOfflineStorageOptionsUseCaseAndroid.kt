package world.respect.shared.domain.storage

import com.ustadmobile.core.domain.storage.GetOfflineStorageOptionsUseCase
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.memory_card
import world.respect.shared.generated.resources.phone_memory

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