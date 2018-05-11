package ch.hsr.ifs.gcs.driver.internal

import android.util.Log
import ch.hsr.ifs.gcs.comm.protocol.createLegacySetModeMessage
import ch.hsr.ifs.gcs.driver.AerialVehicle
import ch.hsr.ifs.gcs.driver.DRIVER_MAVLINK_PIXHAWK_PX4
import ch.hsr.ifs.gcs.driver.Platform
import java.nio.channels.ByteChannel

/**
 * Concrete  implementation of the [platform driver interface][Platform] for Pixhawk PX4 vehicles
 *
 * Pixhawk PX4 controllers have certain quirks, that need special handling. For example, in order
 * to take-off with a PX4 controller, it is required to switch into the 'Take-Off' submode of the
 * 'Automatic' custom mode.\
 *
 * @since 1.0.0
 * @author IFS Institute for Software
 */
internal class MAVLinkPlaformPixhawkPX4(channel: ByteChannel) : MAVLinkCommonPlatformImpl(channel) {

    companion object {
        private val TAG = MAVLinkPlaformPixhawkPX4::class.simpleName
    }

    enum class PX4CustomMode(val id: Int) {
        MANUAL(1 shl 16),
        ALTITUDE_CONTROL(2 shl 16),
        POSITION_CONTROL(3 shl 16),
        AUTOMATIC(4 shl 16),
        ACROBATIC(5 shl 16),
        OFFBOARD_CONTROL(6 shl 16),
        STABILIZED(7 shl 16),
        RATTITUDE(8 shl 16),
        SIMPLE(9 shl 16)
    }

    enum class PX4AutomaticModeSubMode(val id: Int) {
        READY(1 shl 24),
        TAKEOFF(2 shl 24),
        LOITER(3 shl 24),
        MISSION(4 shl 24),
        RETURN_TO_LAND(5 shl 24),
        LAND(6 shl 24),
        RETURN_TO_GROUND_STATION(7 shl 24),
        FOLLOW_TARGET(8 shl 24)
    }

    override val driverId get() = DRIVER_MAVLINK_PIXHAWK_PX4

    override fun takeOff(altitude: AerialVehicle.Altitude) = changeMode(PX4CustomMode.AUTOMATIC, PX4AutomaticModeSubMode.TAKEOFF.id)

    override fun land() = changeMode(PX4CustomMode.AUTOMATIC, PX4AutomaticModeSubMode.LAND.id)

    private fun changeMode(customMode: PX4CustomMode, customSubMode: Int) {
        if (customMode == PX4CustomMode.AUTOMATIC && PX4AutomaticModeSubMode.values().asSequence().none { it.id == customSubMode }) {
            Log.w(TAG, "Ignoring unknown 'Automatic' mode sub-mode '$customSubMode'")
            return
        }

        enqueueCommand(createLegacySetModeMessage(senderSystem, targetSystem, schema, 1, customMode.id + customSubMode))
    }

}