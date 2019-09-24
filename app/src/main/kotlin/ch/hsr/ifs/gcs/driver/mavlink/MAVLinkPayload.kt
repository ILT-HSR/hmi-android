package ch.hsr.ifs.gcs.driver.mavlink

import ch.hsr.ifs.gcs.driver.Payload
import ch.hsr.ifs.gcs.driver.mavlink.support.MAVLinkSystem
import me.drton.jmavlib.mavlink.MAVLinkMessage
import me.drton.jmavlib.mavlink.MAVLinkSchema

interface MAVLinkPayload : Payload {

    val commandDescriptor: NativeCommand

    val system: MAVLinkSystem?

    val schema: MAVLinkSchema

    override fun trigger() = listOf(MAVLinkCommand(commandDescriptor))

    fun handle(message: MAVLinkMessage)

}