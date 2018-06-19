package ch.hsr.ifs.gcs.driver.mavlink.payload

import ch.hsr.ifs.gcs.driver.Platform
import ch.hsr.ifs.gcs.driver.mavlink.support.CommandDescriptor
import ch.hsr.ifs.gcs.driver.mavlink.support.LongCommand

class Gripper(platform: Platform) : BasicPayload(platform) {

    companion object {
        const val DRIVER_ID = "ch.hsr.ifs.gcs.driver.mavlink.payload.gripper"
    }

    override val commandDescriptor = CommandDescriptor(
            LongCommand.DO_SET_SERVO,
            5.toFloat(),
            900.toFloat()
    )

}