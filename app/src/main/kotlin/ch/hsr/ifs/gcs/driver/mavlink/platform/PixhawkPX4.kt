package ch.hsr.ifs.gcs.driver.mavlink.platform

import ch.hsr.ifs.gcs.driver.AerialVehicle
import ch.hsr.ifs.gcs.driver.Platform
import ch.hsr.ifs.gcs.driver.mavlink.support.*
import ch.hsr.ifs.gcs.support.geo.GPSPosition
import ch.hsr.ifs.gcs.support.geo.WGS89Position
import java.nio.channels.ByteChannel

/**
 * Concrete  implementation of the [platform driver interface][Platform] for Pixhawk PX4 vehicles
 *
 * Pixhawk PX4 controllers have certain quirks, that need special handling. For example, in order
 * to take-off with a PX4 controller, it is required to switch into the 'Take-Off' sub-mode of the
 * 'Automatic' custom mode.
 *
 * @since 1.0.0
 * @author IFS Institute for Software
 */
internal class PixhawkPX4(channel: ByteChannel, payloadDriverId: String?) : CommonPlatform(channel, payloadDriverId) {

    enum class PX4CustomMode(val id: Int) {
        MANUAL(1),
        ALTITUDE_CONTROL(2),
        POSITION_CONTROL(3),
        AUTOMATIC(4),
        ACROBATIC(5),
        OFFBOARD_CONTROL(6),
        STABILIZED(7),
        RATTITUDE(8),
        SIMPLE(9)
    }

    enum class PX4AutomaticModeSubMode(val id: Int) {
        READY(1),
        TAKEOFF(2),
        LOITER(3),
        MISSION(4),
        RETURN_TO_LAND(5),
        LAND(6),
        RETURN_TO_GROUND_STATION(7),
        FOLLOW_TARGET(8)
    }

    companion object {
        /**
         * The driver ID of the builtin [ch.hsr.ifs.gcs.driver.platform.mavlink.MAVLinkPlatform] implementation for Pixhawk PX4 controllers
         *
         * @since 1.0.0
         * @author IFS Institute for Software
         */
        const val DRIVER_MAVLINK_PIXHAWK_PX4 = "ch.hsr.ifs.gcs.driver.mavlink.platform.PixhawkPX4"
    }

    override val driverId get() = DRIVER_MAVLINK_PIXHAWK_PX4

    override fun takeOff(altitude: AerialVehicle.Altitude) =
            enqueueCommands(
                    createDoSetModeMessage(senderSystem, targetSystem, schema, VehicleMode.GUIDED_ARMED),
                    createDoTakeoffMessage(senderSystem, targetSystem, schema, currentPosition?.let { (it.altitude + altitude.meters).toFloat() }
                            ?: Float.NaN)
            )

    override fun land() =
            enqueueCommands(
                    createDoSetModeMessage(senderSystem, targetSystem, schema, VehicleMode.GUIDED_ARMED),
                    createDoLandMessage(senderSystem, targetSystem, schema)
            )

    override fun changeAltitude(altitude: AerialVehicle.Altitude) {
        currentPosition?.let {
            val pos = GPSPosition(it.latitude, it.longitude, it.altitude + altitude.meters.toFloat())
            enqueueCommands(
                    createDoSetModeMessage(senderSystem, targetSystem, schema, VehicleMode.GUIDED_ARMED),
                    createDoRepositionMessage(senderSystem, targetSystem, schema, WGS89Position(pos))
            )
        }
    }
}