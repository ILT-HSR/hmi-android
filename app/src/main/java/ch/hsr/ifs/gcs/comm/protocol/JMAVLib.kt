package ch.hsr.ifs.gcs.comm.protocol

import me.drton.jmavlib.mavlink.MAVLinkMessage
import me.drton.jmavlib.mavlink.MAVLinkSchema

private const val MESSAGE_HEARTBEAT = "HEARTBEAT"
private const val MESSAGE_COMMAND_LONG = "COMMAND_LONG"

enum class MAVLinkLongCommand(val value: Int) {
    NAV_TAKEOFF_LOCAL(24),
    NAV_LOITER_TO_ALT(31),
    COMPONENT_ARM_DISARM(400),
    REQUEST_AUTOPILOT_CAPABILITIES(520),
}

private val Boolean.int get() = if (this) 1 else 0

data class MAVLinkSystem(val id: Int, val component: Int)

/**
 * Create a new MAVLink message
 *
 * @param name The MAVLink ID of the message (e.g. "HEARTBEAT")
 * @param system The sender system ID
 * @param component The sender component ID
 * @param schema The message schema
 *
 * @return A new, empty MAVLink message
 */
fun createMAVLinkMessage(name: String, sender: MAVLinkSystem, schema: MAVLinkSchema) =
        MAVLinkMessage(schema, name, sender.id, sender.component)

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
    msg.set("target_component", 0)
    msg.set("command", command.value)
    msg.set("confirmation", 0)

    return msg
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
fun createTakeOffLocalMessage(target: MAVLinkSystem, sender: MAVLinkSystem, schema: MAVLinkSchema, pitch: Float, ascendRate: Float, yaw: Float, x: Float, y: Float, z: Float): MAVLinkMessage {
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
 * Create a new MAVLink 'Loiter To Altitude' message
 *
 * @param sender The sender system
 * @param target The target system
 * @param schema The message schema
 * @param altitude The desired altitude of the vehicle in m
 *
 * @return a new MAVLink 'Long Command' message containing a 'Loiter To Altitude' command
 */
fun createLoiterToAltitudeMessage(target: MAVLinkSystem, sender: MAVLinkSystem, schema: MAVLinkSchema, altitude: Float): MAVLinkMessage {
    val msg = createLongCommandMessage(sender, target, schema, MAVLinkLongCommand.NAV_TAKEOFF_LOCAL)

    msg.set("param1", false.int)
    msg.set("param7", altitude)

    return msg
}
