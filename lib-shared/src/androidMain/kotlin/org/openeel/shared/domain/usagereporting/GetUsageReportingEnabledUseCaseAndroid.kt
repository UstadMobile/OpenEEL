package world.respect.shared.domain.usagereporting

import android.content.Context
import androidx.preference.PreferenceManager
import org.acra.ACRA

class GetUsageReportingEnabledUseCaseAndroid(
    private val context: Context
): GetUsageReportingEnabledUseCase {

    override fun invoke(): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
            ACRA.PREF_ENABLE_ACRA,
            true
        )
    }
}