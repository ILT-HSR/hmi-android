package ch.hsr.ifs.gcs.driver.mavlink.payload

import ch.hsr.ifs.gcs.driver.Platform
import ch.hsr.ifs.gcs.driver.mavlink.support.CommandDescriptor
import ch.hsr.ifs.gcs.driver.mavlink.support.LongCommand

class NullPayload(platform: Platform) : BasicPayload(platform) {

    companion object {
        const val DRIVER_ID = "ch.hsr.ifs.gcs.driver.mavlink.payload.null"
    }

    override val commandDescriptor = CommandDescriptor(LongCommand.NAV_LAST)

}