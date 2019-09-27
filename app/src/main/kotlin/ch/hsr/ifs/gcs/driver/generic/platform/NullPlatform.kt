package ch.hsr.ifs.gcs.driver.generic.platform

import ch.hsr.ifs.gcs.driver.AerialVehicle
import ch.hsr.ifs.gcs.driver.Command
import ch.hsr.ifs.gcs.driver.Payload
import ch.hsr.ifs.gcs.driver.generic.NullCommand
import ch.hsr.ifs.gcs.driver.generic.payload.NullPayload
import ch.hsr.ifs.gcs.mission.Execution
import ch.hsr.ifs.gcs.support.geo.GPSPosition
import java.nio.channels.ByteChannel

class NullPlatform(channel: ByteChannel, payloads: List<Payload>) : AerialVehicle {

    companion object {
        const val DRIVER_ID = "ch.hsr.ifs.gcs.driver.generic.platform.null"

        fun instantiate(channel: ByteChannel, payloads: List<Payload>): NullPlatform = NullPlatform(channel, payloads)
    }

    override fun limitTravelSpeed(speed: Double): Command<*> {
        return NullCommand()
    }

    private lateinit var fPayload: Payload

    override fun takeOff(altitude: AerialVehicle.Altitude): Command<*> {
        return NullCommand()
    }

    override fun land(): Command<*> {
        return NullCommand()
    }

    override fun changeAltitude(altitude: AerialVehicle.Altitude): Command<*> {
        return NullCommand()
    }

    override fun returnToLaunch(): Command<*> {
        return NullCommand()
    }

    override fun moveTo(position: GPSPosition): Command<*> {
        return NullCommand()
    }

    override val driverId: String
        get() = "ch.hsr.ifs.gcs.driver.platform.null"

    override val name: String
        get() = "NullPlatform"

    override val isAlive: Boolean
        get() = true

    override val currentPosition: GPSPosition?
        get() = null

    override var payload: Payload
        get() = fPayload
        set(value) {fPayload = value}

    override val execution: Execution
        get() = object : Execution() {
            override fun tick(): Status {
                return Status.RUNNING
            }

        }
}