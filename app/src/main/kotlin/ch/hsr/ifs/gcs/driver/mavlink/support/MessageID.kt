package ch.hsr.ifs.gcs.driver.mavlink.support

/**
 * An enumeration of messages IDs commonly used by MAVLink vehicles
 *
 * @since 1.0.0
 */
enum class MessageID {
    HEARTBEAT,
    COMMAND_LONG,
    SET_MODE,
    AUTOPILOT_VERSION,
    GLOBAL_POSITION_INT,
    COMMAND_ACK,
    MISSION_COUNT,
    MISSION_REQUEST,
    MISSION_ITEM,
    MISSION_ACK;

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