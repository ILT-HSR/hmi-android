package ch.hsr.ifs.gcs.driver.mavlink

import ch.hsr.ifs.gcs.driver.Payload

interface MAVLinkPayload : Payload {

    val commandDescriptor: NativeCommand

    override fun trigger() = listOf(MAVLinkCommand(commandDescriptor))

}