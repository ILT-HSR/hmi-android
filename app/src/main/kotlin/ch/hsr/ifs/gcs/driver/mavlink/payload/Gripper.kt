package ch.hsr.ifs.gcs.driver.mavlink.payload

import ch.hsr.ifs.gcs.driver.mavlink.MAVLinkPayload
import ch.hsr.ifs.gcs.driver.mavlink.support.LongCommand
import ch.hsr.ifs.gcs.driver.mavlink.support.MAVLinkMissionCommand

class Gripper : MAVLinkPayload {

    companion object {
        const val DRIVER_ID = "ch.hsr.ifs.gcs.driver.mavlink.payload.gripper"
    }

    override val commandDescriptor = MAVLinkMissionCommand(
            LongCommand.DO_SET_SERVO,
            5.toFloat(),
            900.toFloat()
    )

}