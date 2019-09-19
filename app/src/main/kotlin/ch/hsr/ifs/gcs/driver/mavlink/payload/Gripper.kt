package ch.hsr.ifs.gcs.driver.mavlink.payload

import ch.hsr.ifs.gcs.driver.mavlink.MAVLinkPayload
import ch.hsr.ifs.gcs.driver.mavlink.LongCommand
import ch.hsr.ifs.gcs.driver.mavlink.PlanCommand
import ch.hsr.ifs.gcs.driver.mavlink.support.MAVLinkSystem
import ch.hsr.ifs.gcs.driver.mavlink.support.NavigationFrame

class Gripper : MAVLinkPayload {

    companion object {
        const val DRIVER_ID = "ch.hsr.ifs.gcs.driver.mavlink.payload.gripper"
    }

    override val commandDescriptor = PlanCommand(
            LongCommand.DO_SET_SERVO,
            NavigationFrame.MISSION,
            1.toFloat(),
            2000.toFloat()
    )

    override val system: MAVLinkSystem? = null

}