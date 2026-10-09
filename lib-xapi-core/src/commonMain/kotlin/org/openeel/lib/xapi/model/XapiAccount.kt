package world.respect.lib.xapi.model

import kotlinx.serialization.Serializable

@Serializable
data class XapiAccount(
    val homePage: String,
    val name: String,
) {

    companion object {

        fun fromHomePageAndNameOrNull(homePage: String?, name: String?): XapiAccount? {
            return if(homePage != null && name != null) {
                XapiAccount(homePage, name)
            }else {
                null
            }
        }

    }

}
