package world.respect.shared.domain.testlaunchableapp

enum class TestLaunchableAppModeEnum(val id: String) {
    WEBVIEW("webview"), NATIVE("native");

    companion object {

        fun forId(id: String) = entries.first { it.id == id }

    }
}