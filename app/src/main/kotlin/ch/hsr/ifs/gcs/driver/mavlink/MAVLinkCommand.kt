package ch.hsr.ifs.gcs.driver.mavlink

import ch.hsr.ifs.gcs.driver.Command
import ch.hsr.ifs.gcs.driver.mavlink.support.MAVLinkSystem
import ch.hsr.ifs.gcs.driver.mavlink.support.NavigationFrame
import ch.hsr.ifs.gcs.driver.mavlink.support.createLongCommandMessage
import me.drton.jmavlib.mavlink.MAVLinkSchema

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

/**
 * @brief A command that is executed as part of a mission plan
 *
 * Commands of this type will be executed as mission items. How they are executed (e.g. uploaded as
 * a mission, or ticked by the driver) is up the the respective [execution][Execution] type.
 *
 * @since 1.2.0
 */
data class PlanCommand(
        val id: LongCommand,
        val frame: NavigationFrame,
        val param1: Float = 0.0f,
        val param2: Float = 0.0f,
        val param3: Float = 0.0f,
        val param4: Float = 0.0f,
        val x: Float = 0.0f,
        val y: Float = 0.0f,
        val z: Float = 0.0f) : NativeCommand() {

    fun asMessage(sender: MAVLinkSystem, target: MAVLinkSystem, schema: MAVLinkSchema) =
            createLongCommandMessage(sender, target, schema, id).also {
                it.set("param1", param1)
                it.set("param2", param2)
                it.set("param3", param3)
                it.set("param4", param4)
                it.set("param5", x)
                it.set("param6", y)
                it.set("param7", z)
            }

}

/**
 * @brief A command that is executed outside of a mission plan
 *
 * Commands of this type shall be executed outside of a mission plan. They may be intermixed with
 * [plan commands][PlanCommand] as to facilitate event driven messaging for external payload or
 * platforms. [Execution]s shall never upload [message commands][PayloadCommand] to the vehicle
 * as part of a mission.
 *
 * @since 1.2.0
 */
data class PayloadCommand(val name: String, val data: Map<String, Any>) : NativeCommand()

data class MAVLinkCommand(override val nativeCommand: NativeCommand) : Command<NativeCommand>