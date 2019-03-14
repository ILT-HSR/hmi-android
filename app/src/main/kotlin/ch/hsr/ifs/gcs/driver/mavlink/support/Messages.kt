package ch.hsr.ifs.gcs.driver.mavlink.support

import me.drton.jmavlib.mavlink.MAVLinkMessage
import me.drton.jmavlib.mavlink.MAVLinkSchema

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
 * @internal
 *
 * Create a new MAVLink message
 *
 * @param name The MAVLink name of the message (e.g. "HEARTBEAT")
 * @param sender The sender system
 * @param schema The message schema
 *
 * @return A new, empty MAVLink message
 */
internal fun createMAVLinkMessage(name: String, sender: MAVLinkSystem, schema: MAVLinkSchema) =
        MAVLinkMessage(schema, name, sender.id, sender.component)

/**
 * @internal
 *
 * Create a new MAVLink message
 *
 * @param id The MAVLink ID of the message (e.g. [MessageID.HEARTBEAT])
 * @param sender The sender system
 * @param schema The message schema
 *
 * @return A new, empty MAVLink message
 */
internal fun createMAVLinkMessage(id: MessageID, sender: MAVLinkSystem, schema: MAVLinkSchema) =
        createMAVLinkMessage(id.name, sender, schema)

/**
 * @internal
 *
 * Create a new targeted MAVLink message
 *
 * @param name The name of the MAVLink message (e.g. "HEARTBEAT")
 * @param sender The sender system
 * @param target The target system
 * @param schema The message schema
 *
 * @return A new, empty MAVLink message
 */
internal fun createTargetedMAVLinkMessage(name: String, sender: MAVLinkSystem, target: MAVLinkSystem, schema: MAVLinkSchema) =
        with(createMAVLinkMessage(name, sender, schema)) {
            set("target_system", target.id)
            set("target_component", target.component)
            this
        }

/**
 * @internal
 *
 * Create a new targeted MAVLink message
 *
 * @param id The MAVLink ID of the message (e.g. [MessageID.HEARTBEAT])
 * @param sender The sender system
 * @param target The target system
 * @param schema The message schema
 *
 * @return A new, empty MAVLink message
 */
internal fun createTargetedMAVLinkMessage(id: MessageID, sender: MAVLinkSystem, target: MAVLinkSystem, schema: MAVLinkSchema) =
        createTargetedMAVLinkMessage(id.name, sender, target, schema)

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
internal fun createLongCommandMessage(sender: MAVLinkSystem, target: MAVLinkSystem, schema: MAVLinkSchema, command: LongCommand): MAVLinkMessage {
    val msg = createTargetedMAVLinkMessage(MessageID.COMMAND_LONG, sender, target, schema)

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
    val heartbeat = createMAVLinkMessage(MessageID.HEARTBEAT, sender, schema)

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
        createLongCommandMessage(sender, target, schema, LongCommand.COMPONENT_ARM_DISARM)

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
@Suppress("unused")
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
    val msg = createLongCommandMessage(sender, target, schema, LongCommand.REQUEST_AUTOPILOT_CAPABILITIES)

    msg.set("param1", true.int)

    return msg
}