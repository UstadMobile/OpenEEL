package world.respect.shared.util.ext

import org.jetbrains.compose.resources.StringResource
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.select_collections
import world.respect.shared.generated.resources.select_units
import world.respect.shared.viewmodel.catalog.OpdsPickType

/**
 * The title to show in the app bar when the user in the process of picking an opds feed or publication.
 */
val OpdsPickType.appbarTitleString: StringResource
    get() = when(this) {
        OpdsPickType.PUBLICATION -> Res.string.select_units
        OpdsPickType.CATALOG_FEED -> Res.string.select_collections
    }
