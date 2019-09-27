package ch.hsr.ifs.gcs.driver.mavlink.platform

import ch.hsr.ifs.gcs.driver.mavlink.support.MAVLinkSystem
import ch.hsr.ifs.gcs.mission.Execution
import me.drton.jmavlib.mavlink.MAVLinkSchema
import org.junit.Test
import java.nio.channels.ByteChannel

class TestPlatform(channel: ByteChannel, schema: MAVLinkSchema) : BasicPlatform(channel, emptyList(), schema) {

    private inner class TestExecution(platform: TestPlatform, target: MAVLinkSystem) : MissionExecution(platform), IntrospectableExecution {
        override val size get() = fCommands.size
        override val commands get() = fCommands
    }

    override var fExecution: MissionExecution = TestExecution(this, MAVLinkSystem(42, 21))

    override val driverId = "ch.hsr.ifs.gcs.driver.mavlink.platform.TestPlatform"

    override val execution: Execution
        get() = TestExecution(this, targetSystem)

}