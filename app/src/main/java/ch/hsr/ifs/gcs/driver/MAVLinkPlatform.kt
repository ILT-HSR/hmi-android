package ch.hsr.ifs.gcs.driver

import me.drton.jmavlib.mavlink.MAVLinkSchema

/**
 * This interface specifies the generic API of MAVLink vehicle platforms.
 *
 * @since 1.0.0
 * @author IFS Institute for Software
 */
interface MAVLinkPlatform : AerialVehicle {

    /**
     * An enum of messages IDs commonly used by MAVLink vehicles
     *
     * @since 1.0.0
     */
    enum class MessageID {
        HEARTBEAT,
        AUTOPILOT_VERSION,
        GLOBAL_POSITION_INT;

        companion object {

            /**
             * Try to create a [MessageID] with the given name
             *
             * @param name The name of a MAVLink message
             * @return The corresponding [MessageID] if it exists, `null` otherwise
             * @since 1.0.0
             * @author IFS Institute for Software
             */
            fun from(name: String) = try {
                MessageID.valueOf(name)
            } catch (e: Exception) {
                null
            }

        }
    }

    /**
     * The MAVLink message schema associated with this platform
     *
     * @since 1.0.0
     */
    val schema: MAVLinkSchema

    /**
     * Arm the vehicle for takeoff
     *
     * @since 1.0.0
     */
    fun arm()

    /**
     * Disarm the vehicle, preventing takeoff
     *
     * @since 1.0.0
     */
    fun disarm()

}