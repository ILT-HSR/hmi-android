package ch.hsr.ifs.gcs.driver.generic.platform

import ch.hsr.ifs.gcs.driver.AerialVehicle
import ch.hsr.ifs.gcs.driver.Command
import ch.hsr.ifs.gcs.driver.Payload
import ch.hsr.ifs.gcs.driver.channel.Channel
import ch.hsr.ifs.gcs.driver.generic.NullCommand
import ch.hsr.ifs.gcs.mission.Execution
import ch.hsr.ifs.gcs.support.geo.GPSPosition

class NullPlatform(channel: Channel, override val payloads: List<Payload>) : AerialVehicle {

    companion object {
        const val DRIVER_ID = "ch.hsr.ifs.gcs.driver.generic.platform.null"

        fun instantiate(channel: Channel, payloads: List<Payload>): NullPlatform = NullPlatform(channel, payloads)
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

    override val driverId = DRIVER_ID

    override val name = "NullPlatform"

    override val isAlive = true

    override val currentPosition = null

    override val execution: Execution
        get() = object : Execution() {
            override fun tick(): Status {
                return Status.RUNNING
            }

        }
}