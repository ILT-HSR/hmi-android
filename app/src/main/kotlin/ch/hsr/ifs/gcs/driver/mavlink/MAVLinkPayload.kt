package ch.hsr.ifs.gcs.driver.mavlink

import ch.hsr.ifs.gcs.driver.Payload
import ch.hsr.ifs.gcs.driver.mavlink.support.MAVLinkSystem

interface MAVLinkPayload : Payload {

    val commandDescriptor: NativeCommand

    val system: MAVLinkSystem?

    override fun trigger() = listOf(MAVLinkCommand(commandDescriptor))

}