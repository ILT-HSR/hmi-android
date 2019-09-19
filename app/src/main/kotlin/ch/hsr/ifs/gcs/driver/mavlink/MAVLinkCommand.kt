package ch.hsr.ifs.gcs.driver.mavlink

import ch.hsr.ifs.gcs.driver.Command
import ch.hsr.ifs.gcs.driver.mavlink.support.NavigationFrame
import me.drton.jmavlib.mavlink.MAVLinkMessage

enum class LongCommand(val value: Int) {
    NAV_WAYPOINT(16),
    NAV_RETURN_TO_LAUNCH(20),
    NAV_LAND(21),
    NAV_TAKEOFF(22),
    NAV_LOITER_TO_ALT(31),
    NAV_LAST(95),
    DO_CHANGE_SPEED(178),
    DO_SET_SERVO(183),
    MISSION_START(300),
    COMPONENT_ARM_DISARM(400),
    REQUEST_AUTOPILOT_CAPABILITIES(520),
}

sealed class NativeCommand()

data class PlanCommand(
        val id: LongCommand,
        val frame: NavigationFrame,
        val param1: Float = 0.0f,
        val param2: Float = 0.0f,
        val param3: Float = 0.0f,
        val param4: Float = 0.0f,
        val x: Float = 0.0f,
        val y: Float = 0.0f,
        val z: Float = 0.0f) : NativeCommand()

data class MessageCommand(val message: MAVLinkMessage) : NativeCommand()

data class MAVLinkCommand(override val nativeCommand: NativeCommand) : Command<NativeCommand>