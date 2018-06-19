package ch.hsr.ifs.gcs.driver.mavlink

import ch.hsr.ifs.gcs.driver.Payload
import ch.hsr.ifs.gcs.driver.mavlink.support.CommandDescriptor

interface MAVLinkPayload : Payload {

    val commandDescriptor: CommandDescriptor

}