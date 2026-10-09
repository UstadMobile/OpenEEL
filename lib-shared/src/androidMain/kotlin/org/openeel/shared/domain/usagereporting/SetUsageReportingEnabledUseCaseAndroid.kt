package world.respect.shared.domain.usagereporting

import android.content.Context
import androidx.preference.PreferenceManager
import androidx.core.content.edit
import org.acra.ACRA

class SetUsageReportingEnabledUseCaseAndroid(
    private val context: Context,
) : SetUsageReportingEnabledUseCase {

    override fun invoke(enabled: Boolean) {
        PreferenceManager.getDefaultSharedPreferences(context).edit(commit = true) {
            putBoolean(ACRA.PREF_ENABLE_ACRA, enabled)
        }
    }

}