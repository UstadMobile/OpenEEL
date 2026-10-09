package world.respect.shared.domain.launchapp

object OpenEelIntent {

    /**
     * Intent action to launch a lesson. The behavior of the app should be the same as when using
     * android.intent.action.VIEW . It is needed because:
     *
     * a) It can be declared in the queries of the launcher app to allow the launcher app to detect
     *    if a compatible app is installed or not.
     * b) It can be used to launch a publication in a native app even when the native app does not
     *    have a verified app link. This is required when the native app uses dynamic server names.
     */
    const val ACTION_LAUNCH = "org.openeel.action.LAUNCH"

}