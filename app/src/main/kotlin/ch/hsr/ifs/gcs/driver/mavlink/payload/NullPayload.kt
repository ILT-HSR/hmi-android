package ch.hsr.ifs.gcs.driver.mavlink.payload

import ch.hsr.ifs.gcs.driver.mavlink.MAVLinkPayload
import ch.hsr.ifs.gcs.driver.mavlink.LongCommand
import ch.hsr.ifs.gcs.driver.mavlink.PlanCommand
import ch.hsr.ifs.gcs.driver.mavlink.support.MAVLinkSystem
import ch.hsr.ifs.gcs.driver.mavlink.support.NavigationFrame
import me.drton.jmavlib.mavlink.MAVLinkMessage
import me.drton.jmavlib.mavlink.MAVLinkSchema
import me.drton.jmavlib.mavlink.MAVLinkSchemaRegistry

class NullPayload : MAVLinkPayload {

    companion object {
        const val DRIVER_ID = "ch.hsr.ifs.gcs.driver.mavlink.payload.null"
    }

    override val schema: MAVLinkSchema = MAVLinkSchemaRegistry.get("common")!!

    override fun handle(message: MAVLinkMessage) = Unit

    override val commandDescriptor = PlanCommand(LongCommand.NAV_LAST, NavigationFrame.MISSION)

    override val system: MAVLinkSystem? = null
}