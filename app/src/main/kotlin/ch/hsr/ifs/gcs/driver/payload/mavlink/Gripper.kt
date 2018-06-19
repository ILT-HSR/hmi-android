package ch.hsr.ifs.gcs.driver.payload.mavlink

import ch.hsr.ifs.gcs.driver.platform.mavlink.MAVLinkPlatform
import ch.hsr.ifs.gcs.driver.platform.mavlink.support.CommandDescriptor

class Gripper(platform: MAVLinkPlatform) : BasicPayload(platform) {

    override val commandDescriptor: CommandDescriptor
        get() = TODO("not implemented")

}