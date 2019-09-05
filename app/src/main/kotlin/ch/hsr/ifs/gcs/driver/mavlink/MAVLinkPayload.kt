package ch.hsr.ifs.gcs.driver.mavlink

import ch.hsr.ifs.gcs.driver.Payload
import ch.hsr.ifs.gcs.driver.mavlink.support.MAVLinkMissionCommand

interface MAVLinkPayload : Payload {

    val commandDescriptor: MAVLinkMissionCommand

    override fun trigger() = listOf(MAVLinkCommand(commandDescriptor))

}