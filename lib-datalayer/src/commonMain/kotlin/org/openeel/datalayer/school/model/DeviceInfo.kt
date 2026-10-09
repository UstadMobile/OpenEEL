package world.respect.datalayer.school.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
data class DeviceInfo(
    val platform: Platform,
    val androidSdkInt: Int,
    val version: String,
    val manufacturer: String?,
    val model: String?,
    val ram: Long,
) {


    @Serializable(with = PlatformSerializer::class)
    enum class Platform(val pName: String) {
        ANDROID("android"), UNKNOWN("unknown");

        companion object {

            fun fromValue(value: String): Platform {
                return entries.first { it.pName == value }
            }

        }
    }


    fun toHeaderLine(): String {
        return mapOf(
            "platform" to platform.pName,
            "androidSdkInt" to androidSdkInt.toString(),
            "version" to version,
            "manufacturer" to manufacturer,
            "model" to model,
            "ram" to ram.toString(),
        ).entries.joinToString(separator = ";") { "${it.key}=${it.value}" }
    }


    companion object {

        const val HEADER_NAME = "X-Respect-Device-Info"

        fun fromHeaderLine(line: String) : DeviceInfo {
            val map = line.split(";").associate {
                val (key, value) = it.split("=")
                key to value
            }

            return DeviceInfo(
                platform = Platform.Companion.fromValue(map["platform"] ?: throw IllegalArgumentException()),
                androidSdkInt = (map["androidSdkInt"] ?: throw IllegalArgumentException()).toInt(),
                version = map["version"] ?: throw IllegalArgumentException(),
                manufacturer = map["manufacturer"] ?: throw IllegalArgumentException(),
                model = map["model"] ?: throw IllegalArgumentException(),
                ram = map["ram"]?.toLong() ?: 0,
            )
        }

        fun fromHeaderLineOrNull(line: String) : DeviceInfo? {
            return try {
                fromHeaderLine(line)
            }catch (e: Exception) {
                null
            }
        }

    }
}


object PlatformSerializer : KSerializer<DeviceInfo.Platform> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
        "world.respect.Platform", PrimitiveKind.STRING
    )

    override fun serialize(
        encoder: Encoder,
        value: DeviceInfo.Platform
    ) {
        encoder.encodeString(value.pName)
    }

    override fun deserialize(decoder: Decoder): DeviceInfo.Platform {
        return DeviceInfo.Platform.fromValue(decoder.decodeString())
    }
}
