package ch.hsr.ifs.gcs.driver.payload.mavlink

import ch.hsr.ifs.gcs.driver.Payload
import ch.hsr.ifs.gcs.driver.platform.mavlink.support.CommandDescriptor

interface MAVLinkPayload : Payload {

    val commandDescriptor: CommandDescriptor

}