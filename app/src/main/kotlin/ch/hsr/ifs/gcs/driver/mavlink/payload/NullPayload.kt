package ch.hsr.ifs.gcs.driver.mavlink.payload

import ch.hsr.ifs.gcs.driver.mavlink.MAVLinkPayload
import ch.hsr.ifs.gcs.driver.mavlink.support.LongCommand
import ch.hsr.ifs.gcs.driver.mavlink.support.MAVLinkMissionCommand
import ch.hsr.ifs.gcs.driver.mavlink.support.NavigationFrame

class NullPayload : MAVLinkPayload {

    companion object {
        const val DRIVER_ID = "ch.hsr.ifs.gcs.driver.mavlink.payload.null"
    }

    override val commandDescriptor = MAVLinkMissionCommand(LongCommand.NAV_LAST, NavigationFrame.MISSION)

}