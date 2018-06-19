package ch.hsr.ifs.gcs.driver.mavlink.platform

import ch.hsr.ifs.gcs.driver.AerialVehicle
import ch.hsr.ifs.gcs.driver.Command
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
class `Structural tests for BasicPlatform's Execution` {

    private val fSchema = MAVLinkSchema(RuntimeEnvironment.application, "schemas/common.xml")
    private val fChannel = TestChannel()
    private val fPlatform = TestPlatform(fChannel, fSchema)

    @Test
    fun `Execution allows adding of commands`() {
        val execution = fPlatform.execution
        val command = fPlatform.returnToLaunch()

        with(execution as IntrospectableExecution) {
            execution += command

            assertThat(size, `is`(equalTo(1)))
            assertThat(commands, `is`(equalTo(listOf<Command<*>>(command))))
        }
    }

    @Test
    fun `Execution allows duplicate commands`() {
        val execution = fPlatform.execution
        val command = fPlatform.returnToLaunch()

        with(execution as IntrospectableExecution) {
            execution += command
            execution += command

            assertThat(size, `is`(equalTo(2)))
            assertThat(commands, `is`(equalTo(listOf<Command<*>>(command, command))))
        }
    }

    @Test
    fun `Execution preserves order of the added commands`() {
        val execution = fPlatform.execution

        with(execution as IntrospectableExecution) {
            execution += fPlatform.returnToLaunch()
            execution += fPlatform.takeOff(AerialVehicle.Altitude(12.0))

            assertThat(size, `is`(equalTo(2)))
            assertThat(commands, `is`(equalTo(listOf<Command<*>>(
                    fPlatform.returnToLaunch(),
                    fPlatform.takeOff(AerialVehicle.Altitude(12.0))
            ))))
        }
    }

}