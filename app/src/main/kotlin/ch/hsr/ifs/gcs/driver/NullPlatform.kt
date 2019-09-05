package ch.hsr.ifs.gcs.driver

import ch.hsr.ilt.uxv.hmi.core.driver.Command
import ch.hsr.ilt.uxv.hmi.core.driver.Payload
import ch.hsr.ilt.uxv.hmi.core.mission.Execution
import ch.hsr.ilt.uxv.hmi.core.support.geo.GPSPosition

class NullPlatform : AerialVehicle {
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