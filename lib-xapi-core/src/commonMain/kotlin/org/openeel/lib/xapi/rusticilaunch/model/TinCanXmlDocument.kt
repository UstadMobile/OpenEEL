package world.respect.lib.xapi.rusticilaunch.model

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import nl.adaptivity.xmlutil.serialization.XmlValue

const val TINCAN_XML_NAMESPACE = "http://projecttincan.com/tincan.xsd"

/**
 * TinCan XML document as per the Rustici Launch Method, see:
 * https://github.com/RusticiSoftware/launch/blob/master/lms_lrs.md
 *
 * Example:
 * https://github.com/RusticiSoftware/TinCan_Prototypes/blob/master/JsTetris_TCAPI/tincan.xml
 */
@Serializable
@XmlSerialName(value = "tincan", namespace = TINCAN_XML_NAMESPACE)
data class TinCanXmlDocument(
    val activities: TinCanXmlActivities
)

@Serializable
@XmlSerialName(value = "activities", namespace = TINCAN_XML_NAMESPACE)
data class TinCanXmlActivities(
    val activity: List<TinCanXmlActivity>
)

@Serializable
@XmlSerialName("activity", namespace = TINCAN_XML_NAMESPACE)
data class TinCanXmlActivity(
    val id: String,
    val type: String? = null,
    @XmlElement(true)
    val name: String? = null,
    val description: TinCanXmlDescription? = null,
    val launch: TinCanXmlLaunch? = null,
)

@XmlSerialName("description", namespace = TINCAN_XML_NAMESPACE)
@Serializable
data class TinCanXmlDescription(
    val lang: String,
    @XmlValue
    val value: String,
)

@XmlSerialName("launch", namespace = TINCAN_XML_NAMESPACE)
@Serializable
data class TinCanXmlLaunch(
    val lang: String,
    @XmlValue
    val value: String,
)