package ch.hsr.ifs.gcs.driver.mavlink

import ch.hsr.ifs.gcs.driver.mavlink.support.MAVLinkMissionCommand
import ch.hsr.ilt.uxv.hmi.core.driver.Payload

interface MAVLinkPayload : Payload {

    val commandDescriptor: MAVLinkMissionCommand

    override fun trigger() = listOf(MAVLinkCommand(commandDescriptor))

}