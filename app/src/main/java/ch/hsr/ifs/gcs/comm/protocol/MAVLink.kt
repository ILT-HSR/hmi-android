package ch.hsr.ifs.gcs.comm.protocol

import me.drton.jmavlib.mavlink.MAVLinkMessage
import me.drton.jmavlib.mavlink.MAVLinkSchema

private const val MESSAGE_HEARTBEAT = "HEARTBEAT"
private const val MESSAGE_COMMAND_LONG = "COMMAND_LONG"
private const val MESSAGE_SET_MODE = "SET_MODE"

enum class MAVLinkLongCommand(val value: Int) {
    NAV_TAKEOFF_LOCAL(24),
    NAV_GUIDED_ENABLE(92),
    NAV_LOITER_TO_ALT(31),
    DO_SET_MODE(176),
    COMPONENT_ARM_DISARM(400),
    REQUEST_AUTOPILOT_CAPABILITIES(520),
    MAV_CMD_NAV_WAYPOINT(16),
    MAV_CMD_DO_REPOSITION(192),
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
data class GPSPosition(val latitude: Float, val longitude: Float, val altitude: Float) {

    /**
     * Convert a #WGS89Position into a #GPSPosition
     *
     * @since 1.0.0
     */
    constructor(position: WGS89Position) : this(
            position.latitude.toFloat() / 10e7F,
            position.longitude.toFloat() / 10e7F,
            position.altitude.toFloat() / 10e3F)
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
            (position.latitude * 10e7F).toInt(),
            (position.longitude * 10e7F).toInt(),
            (position.altitude * 10e3F).toInt())
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
 * Create a new MAVLink 'Take-off Local' message
 *
 * @param sender The sender system
 * @param target The target system
 * @param schema The message schema
 * @param pitch The desired pitch of the vehicle
 * @param ascendRate The desired rate of ascend in m/s
 * @param yaw The desired yaw of the vehicle
 * @param x The desired X position of the vehicle
 * @param y The desired Y position of the vehicle
 * @param z The desired Z position of the vehicle
 *
 * @return a new MAVLink 'Long Command' message containing a 'Take-off Local' command
 */
fun createTakeOffLocalMessage(sender: MAVLinkSystem, target: MAVLinkSystem, schema: MAVLinkSchema, pitch: Float, ascendRate: Float, yaw: Float, x: Float, y: Float, z: Float): MAVLinkMessage {
    val msg = createLongCommandMessage(sender, target, schema, MAVLinkLongCommand.NAV_TAKEOFF_LOCAL)

    msg.set("param1", pitch)
    msg.set("param3", ascendRate)
    msg.set("param4", yaw)
    msg.set("param5", y)
    msg.set("param6", x)
    msg.set("param7", z)

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
    val msg = createLongCommandMessage(sender, target, schema, MAVLinkLongCommand.MAV_CMD_DO_REPOSITION)

    msg.set("param1", -1)
    msg.set("param2", 1)
    msg.set("param3", 0)
    msg.set("param4", Float.NaN)
    msg.set("param5", position.latitude)
    msg.set("param6", position.longitude)
    msg.set("param7", position.altitude.toFloat() / 10e3F)

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
fun createLegacySetModeMessage(sender: MAVLinkSystem, target: MAVLinkSystem, schema: MAVLinkSchema, base: Int, custom: Int): MAVLinkMessage {
    val msg = createMAVLinkMessage(MESSAGE_SET_MODE, sender, schema)

    msg.set("target_system", target.id)
    msg.set("base_mode", base)
    msg.set("custom_mode", custom)

    return msg
}
