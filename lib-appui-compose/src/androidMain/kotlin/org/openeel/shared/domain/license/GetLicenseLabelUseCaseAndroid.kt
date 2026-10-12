package org.openeel.shared.domain.license

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.openeel.appcompose.R
import org.openeel.shared.resources.StringUiText
import org.openeel.shared.domain.license.GetLicenseLabelUseCase.LicenseLabelResult
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.open_source
import org.openeel.shared.generated.resources.proprietary
import org.jetbrains.compose.resources.getString

class GetLicenseLabelUseCaseAndroid(
    private val context: Context,
    private val json: Json,
) : GetLicenseLabelUseCase {

    private val licenseLabelEntries: List<LicenseLabelEntry> by lazy {
        val jsonString = context.resources.openRawResource(R.raw.license_label_json)
            .bufferedReader()
            .readText()
        json.decodeFromString<List<LicenseLabelEntry>>(jsonString)
    }


    override suspend fun invoke(licenseUrl: String): LicenseLabelResult {
        return withContext(Dispatchers.IO) {
            licenseLabelEntries.firstOrNull { entry ->
                entry.links?.html?.href == licenseUrl
                        || entry.licenseStewardUrl == licenseUrl
            }?.let { entry ->
                LicenseLabelResult(
                    title = StringUiText("${getString(Res.string.open_source)}: ${entry.spdxId}"),
                    isOpenSource = true,
                )
            } ?: LicenseLabelResult(
                title = StringUiText(getString(Res.string.proprietary)),
                isOpenSource = false
            )
        }
    }
}
