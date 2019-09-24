package ch.hsr.ifs.gcs.driver.mavlink.platform

import ch.hsr.ifs.gcs.driver.mavlink.support.MAVLinkSystem
import ch.hsr.ifs.gcs.mission.Execution
import me.drton.jmavlib.mavlink.MAVLinkSchema
import java.nio.channels.ByteChannel

class TestPlatform(channel: ByteChannel, schema: MAVLinkSchema) : BasicPlatform(channel, schema) {

    private inner class TestExecution(target: MAVLinkSystem) : BasicPlatform.NativeMissionExecution(), IntrospectableExecution {
        override val size get() = fCommands.size
        override val commands get() = fCommands
    }

    override val driverId = "ch.hsr.ifs.gcs.driver.mavlink.platform.TestPlatform"

    override val execution: Execution
        get() = TestExecution(targetSystem)

}