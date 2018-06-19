package ch.hsr.ifs.gcs.driver.mavlink.payload

import ch.hsr.ifs.gcs.driver.mavlink.MAVLinkPlatform
import ch.hsr.ifs.gcs.driver.mavlink.support.CommandDescriptor

class Gripper(platform: MAVLinkPlatform) : BasicPayload(platform) {

    override val commandDescriptor: CommandDescriptor
        get() = TODO("not implemented")

}