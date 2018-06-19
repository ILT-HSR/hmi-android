package ch.hsr.ifs.gcs.driver.mavlink.platform

import ch.hsr.ifs.gcs.driver.channel.TestChannel
import ch.hsr.ifs.gcs.mission.Execution
import me.drton.jmavlib.mavlink.MAVLinkSchema
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is.`is`
import org.hamcrest.core.IsEqual.equalTo
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class BasicPlatformExecutionTest {

    private val fSchema = MAVLinkSchema(RuntimeEnvironment.application, "schemas/common.xml")
    private val fChannel = TestChannel()
    private val fPlatform = object : BasicPlatform(fChannel, fSchema, null) {
        override val driverId = "ch.hsr.ifs.gcs.driver.mavlink.platform.TestPlatform"
    }

    @Test
    fun `Ticking an empty Execution returns 'FAILURE'`() {
        assertThat(fPlatform.execution.tick(), `is`(equalTo(Execution.Status.FAILURE)))
    }

}