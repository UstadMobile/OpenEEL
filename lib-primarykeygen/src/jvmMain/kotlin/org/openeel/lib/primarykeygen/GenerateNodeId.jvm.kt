package world.respect.lib.primarykeygen

import java.net.NetworkInterface
import kotlin.random.Random

actual fun generateDoorNodeId(maxNodeId: Int): Int {
    try {
        var macString = ""
        NetworkInterface.getNetworkInterfaces().toList().forEach { netInterface ->
            netInterface.hardwareAddress.forEach {
                macString += String.format("%02X", it)
            }
        }

        return macString.hashCode() and maxNodeId
    }catch(_: Exception) {
        return Random.nextInt(1, maxNodeId)
    }
}