package ch.hsr.ifs.gcs.comm.protocol

import me.drton.jmavlib.mavlink.MAVLinkMessage
import me.drton.jmavlib.mavlink.MAVLinkSchema

const val MESSAGE_HEARTBEAT = "HEARTBEAT"
const val MESSAGE_COMMAND_LONG = "COMMAND_LONG"
const val MESSAGE_SET_MODE = "SET_MODE"

enum class MAVLinkLongCommand(val value: Int) {
    NAV_RETURN_TO_LAUNCH(20),
    NAV_LAND(21),
    NAV_TAKEOFF(22),
    DO_SET_MODE(176),
    DO_REPOSITION(192),
    COMPONENT_ARM_DISARM(400),
    REQUEST_AUTOPILOT_CAPABILITIES(520),
}

/**
 * This enumeration describes the modes a MAVLink vehicle can be in.
 *
 * @since 1.0.0
 * @author IFS Institute for Software
 */
enum class MAVLinkMode(val id: Int) {
    /**
     * System is not ready to fly
     */
    PREFLIGHT(0),

    /**
     * The system is:
     *   - disarmed
     *   - allowed to be active
     *   - under manual RC control without stabilization
     *
     */
    MANUAL_DISARMED(64),

    /**
     * The system is:
     *   - armed
     *   - allowed to be active
     *   - under manual RC control without stabilization
     *
     */
    MANUAL_ARMED(MANUAL_DISARMED.id + 128),

    /**
     * The system is:
     *   - disarmed
     *   - in an undefined/developer mode
     */
    TEST_DISARMED(66),

    /**
     * The system is:
     *   - armed
     *   - in an undefined/developer mode
     */
    TEST_ARMED(TEST_DISARMED.id + 128),

    /**
     * The system is:
     *   - disarmed
     *   - allowed to be active
     *   - under manual RC control with stabilization
     */
    STABILIZE_DISARMED(80),

    /**
     * The system is:
     *   - armed
     *   - allowed to be active
     *   - under manual RC control with stabilization
     */
    STABILIZE_ARMED(STABILIZE_DISARMED.id + 128),

    /**
     * The system is:
     *   - disarmed
     *   - allowed to be active
     *   - under autonomous control with manual setpoint
     */
    GUIDED_DISARMED(88),

    /**
     * The system is:
     *   - armed
     *   - allowed to be active
     *   - under autonomous control with manual setpoint
     */
    GUIDED_ARMED(GUIDED_DISARMED.id + 128),

    /**
     * The system is:
     *   - disarmed
     *   - under autonomous control and navigation
     *
     * The system's trajectory is decided onboard and not pre-programmed by waypoints
     */
    AUTO_DISARMED(92),

    /**
     * The system is:
     *   - armed
     *   - under autonomous control and navigation
     *
     * The system's trajectory is decided onboard and not pre-programmed by waypoints
     */
    AUTO_ARMED(AUTO_DISARMED.id + 128),
}

private val Boolean.int get() = if (this) 1 else 0

/**
 * A simple value type to represent a MAVLink system
 *
 * @param id The id of the system
 * @param component The id of the main system component
 *
 * @since 1.0.0
 * @author IFS Institute for Software
 */
data class MAVLinkSystem(val id: Int, val component: Int)

/**
 * A simple value type to hold GPS floating point coordinates
 *
 * @param latitude The latitude of the point (degrees)
 * @param longitude The longitude of the point (degrees)
 * @param altitude The altitude of the point (meters)
 *
 * @since 1.0.0
 * @author IFS Institute for Software
 */
data class GPSPosition(val latitude: Double, val longitude: Double, val altitude: Double) {

    /**
     * Convert a #WGS89Position into a #GPSPosition
     *
     * @since 1.0.0
     */
    constructor(position: WGS89Position) : this(
            position.latitude.toFloat() / 1e7,
            position.longitude.toFloat() / 1e7,
            position.altitude.toFloat() / 1e3)

}

/**
 * A simple value type to hold scaled WGS89 integer coordinates
 *
 * @param latitude The latitude of the point (degrees * 10e7)
 * @param longitude The longitude of the point (degrees * 10e7)
 * @param altitude The altitude of the point (meters * 10e3)
 *
 * @since 1.0.0
 * @author IFS Institute for Software
 */
data class WGS89Position(val latitude: Int, val longitude: Int, val altitude: Int) {

    /**
     * Convert a #GPSPosition into a #WGS89Position
     *
     * @since 1.0.0
     */
    constructor(position: GPSPosition) : this(
            (position.latitude * 1e7).toInt(),
            (position.longitude * 1e7).toInt(),
            (position.altitude * 1e3).toInt())
}

/**
 * @internal
 *
 * Create a new MAVLink message
 *
 * @param name The MAVLink ID of the message (e.g. "HEARTBEAT")
 * @param system The sender system ID
 * @param component The sender component ID
 * @param schema The message schema
 *
 * @return A new, empty MAVLink message
 */
internal fun createMAVLinkMessage(name: String, sender: MAVLinkSystem, schema: MAVLinkSchema) =
        MAVLinkMessage(schema, name, sender.id, sender.component)

/**
 * @internal
 *
 * Create a new MAVLink 'Long Command' message
 *
 * @param sender The sender system
 * @param target The target system
 * @param schema The message schema
 * @param command The 'Long Command'
 *
 * @return A new, empty MAVLink 'Long Command' message
 */
internal fun createLongCommandMessage(sender: MAVLinkSystem, target: MAVLinkSystem, schema: MAVLinkSchema, command: MAVLinkLongCommand): MAVLinkMessage {
    val msg = createMAVLinkMessage(MESSAGE_COMMAND_LONG, sender, schema)

    msg.set("target_system", target.id)
    msg.set("target_component", target.component)
    msg.set("command", command.value)
    msg.set("confirmation", 0)

    return msg
}

/**
 * Create a new MAVLink 'Heartbeat' message
 *
 * @param sender The sender system
 * @param schema The message schema
 *
 * @return A new MAVLink 'Heartbeat' message
 */
fun createHeartbeatMessage(sender: MAVLinkSystem, schema: MAVLinkSchema): MAVLinkMessage {
    val heartbeat = createMAVLinkMessage(MESSAGE_HEARTBEAT, sender, schema)

    heartbeat.set("type", 6)
    heartbeat.set("autopilot", 0)
    heartbeat.set("base_mode", 128)
    heartbeat.set("custom_mode", 0)
    heartbeat.set("system_status", 4)

    return heartbeat
}

/**
 * @internal
 *
 * Create a new MAVLink 'Long Command' message for arming or disarming the vehicle
 *
 * @param sender The sender system
 * @param target The target system
 * @param schema The message schema
 *
 * @return A new MAVLink 'Long Command' containing a partially configured 'Arm/Disarm' command
 */
internal fun newArmDisarmMessage(sender: MAVLinkSystem, target: MAVLinkSystem, schema: MAVLinkSchema) =
        createLongCommandMessage(sender, target, schema, MAVLinkLongCommand.COMPONENT_ARM_DISARM)

/**
 * Create a new MAVLink 'Arm' message
 *
 * @param sender The sender system
 * @param target The target system
 * @param schema The message schema
 *
 * @return a new MAVLink 'Long Command' message containing an 'Arm' command
 */
fun createArmMessage(sender: MAVLinkSystem, target: MAVLinkSystem, schema: MAVLinkSchema): MAVLinkMessage {
    val msg = newArmDisarmMessage(sender, target, schema)

    msg.set("param1", true.int)

    return msg
}

/**
 * Create a new MAVLink 'Disarm' message
 *
 * @param sender The sender system
 * @param target The target system
 * @param schema The message schema
 *
 * @return a new MAVLink 'Long Command' message containing a 'Disarm' command
 */
fun createDisarmMessage(sender: MAVLinkSystem, target: MAVLinkSystem, schema: MAVLinkSchema): MAVLinkMessage {
    val msg = newArmDisarmMessage(sender, target, schema)

    msg.set("param1", false.int)

    return msg
}

/**
 * Create a new MAVLink 'Request Autopilot Capabilities' message
 *
 * @param sender The sender system
 * @param target The target system
 * @param schema The message schema
 *
 * @return a new MAVLink 'Long Command' message containing a 'Request Autopilot Capabilities' command
 */
fun createRequestAutopilotCapabilitiesMessage(sender: MAVLinkSystem, target: MAVLinkSystem, schema: MAVLinkSchema): MAVLinkMessage {
    val msg = createLongCommandMessage(sender, target, schema, MAVLinkLongCommand.REQUEST_AUTOPILOT_CAPABILITIES)

    msg.set("param1", true.int)

    return msg
}

/**
 * Create a new MAVLink 'Do Reposition' message
 *
 * @param sender The sender system
 * @param target The target system
 * @param schema The message schema
 * @param position The target position to reach
 *
 * @return a new MAVLink 'Long Command' message containing a 'Do Reposition' command
 */
fun createDoRepositionMessage(sender: MAVLinkSystem, target: MAVLinkSystem, schema: MAVLinkSchema, position: WGS89Position): MAVLinkMessage {
    val msg = createLongCommandMessage(sender, target, schema, MAVLinkLongCommand.DO_REPOSITION)

    msg.set("param1", -1)
    msg.set("param2", 1)
    msg.set("param3", 0)
    msg.set("param4", Float.NaN)
    msg.set("param5", position.latitude)
    msg.set("param6", position.longitude)
    msg.set("param7", position.altitude.toFloat() / 1e3F)

    return msg
}

/**
 * Create a new MAVLink 'Do Set Mode' message
 *
 * @param sender The sender system
 * @param target The target system
 * @param schema The message schema
 * @param base The base mode
 * @param custom The custom mode
 * @param customSubmode The submode of the custom mode
 *
 * @return a new MAVLink 'Long Command' message containing a 'Do Set Mode' command
 */
fun createDoSetModeMessage(sender: MAVLinkSystem, target: MAVLinkSystem, schema: MAVLinkSchema, base: MAVLinkMode, custom: Int = 0, customSubmode: Int = 0): MAVLinkMessage {
    val msg = createLongCommandMessage(sender, target, schema, MAVLinkLongCommand.DO_SET_MODE)

    msg.set("param1", base.id)
    msg.set("param2", custom)
    msg.set("param3", customSubmode)

    return msg
}


/**
 * Create a new MAVLink 'Set Mode' message
 *
 * @param sender The sender system
 * @param target The target system
 * @param schema The message schema
 * @param base The base mode to switch to
 * @param custom The custom mode to switch to
 *
 * @return a new MAVLink 'Set Mode' message
 */
@Deprecated("This function creates a deprecated message. Please consider using createDoSetMode.")
fun createLegacySetModeMessage(sender: MAVLinkSystem, target: MAVLinkSystem, schema: MAVLinkSchema, base: Int, custom: Int = 0): MAVLinkMessage {
    val msg = createMAVLinkMessage(MESSAGE_SET_MODE, sender, schema)

    msg.set("target_system", target.id)
    msg.set("base_mode", base)
    msg.set("custom_mode", custom)

    return msg
}

/**
 * Create a new MAVLink 'Do Take-off' message
 *
 * Note that the desired altitude might be ignored by a vehicle if it is lower than the minimum
 * altitude specified in the vehicles firmware.
 *
 * @param sender The sender system
 * @param target The target system
 * @param schema The message schema
 * @param altitude The desired altitude. Specifying `Float.NaN` will instruct the vehicle to takeoff
 * to the firmware configured default altitude.
 *
 * @return a new MAVLink 'Long Command' message containing a 'Do Take-off' command
 */
fun createDoTakeoffMessage(sender: MAVLinkSystem, target: MAVLinkSystem, schema: MAVLinkSchema, altitude: Float = Float.NaN): MAVLinkMessage {
    val msg = createLongCommandMessage(sender, target, schema, MAVLinkLongCommand.NAV_TAKEOFF)

    msg.set("param1", Float.NaN) // minimum pitch
    msg.set("param4", Float.NaN) // yaw angle
    msg.set("param5", Float.NaN) // latitude
    msg.set("param6", Float.NaN) // longitude
    msg.set("param7", altitude)

    return msg
}

/**
 * Create a new MAVLink 'Do Land' message
 *
 * @param sender The sender system
 * @param target The target system
 * @param schema The message schema
 *
 * @return a new MAVLink 'Long Command' message containing a 'Do Land' command
 */
fun createDoLandMessage(sender: MAVLinkSystem, target: MAVLinkSystem, schema: MAVLinkSchema): MAVLinkMessage {
    val msg = createLongCommandMessage(sender, target, schema, MAVLinkLongCommand.NAV_LAND)

    msg.set("param1", Float.NaN) // AbortAlt
    msg.set("param1", Float.NaN) // precision landing mode
    msg.set("param4", Float.NaN) // yaw angle
    msg.set("param5", Float.NaN) // latitude
    msg.set("param6", Float.NaN) // longitude
    msg.set("param7", Float.NaN) // altitude

    return msg
}

/**
 * Create a new MAVLink 'Return to Launch' message
 *
 * @param sender The sender system
 * @param target The target system
 * @param schema The message schema
 *
 * @return a new MAVLink 'Long Command' message containing a 'Return to Launch' command
 */
fun createReturnToLaunchMessage(sender: MAVLinkSystem, target: MAVLinkSystem, schema: MAVLinkSchema): MAVLinkMessage {
    val msg = createLongCommandMessage(sender, target, schema, MAVLinkLongCommand.NAV_RETURN_TO_LAUNCH)
    return msg
}
