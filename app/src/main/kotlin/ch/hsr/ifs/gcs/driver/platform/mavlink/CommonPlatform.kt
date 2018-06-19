package ch.hsr.ifs.gcs.driver.platform.mavlink

import ch.hsr.ifs.gcs.mission.Execution
import ch.hsr.ifs.gcs.mission.need.task.Task
import me.drton.jmavlib.mavlink.MAVLinkSchemaRegistry
import java.nio.channels.ByteChannel
import java.util.concurrent.TimeUnit

/**
 * This class provides a basic [platform][ch.hsr.ifs.gcs.driver.Platform] implementation for the
 * MAVLink **common** schema. Specific platform drivers for vehicles implementing the **common**
 * schema should be derived from this class.
 *
 * @since 1.0.0
 * @author IFS Institute for Software
 */
open class CommonPlatform(channel: ByteChannel) : BasicPlatform(channel, MAVLinkSchemaRegistry["common"]!!) {

    companion object {
        /**
         * The driver ID of the builtin [ch.hsr.ifs.gcs.driver.platform.mavlink.BasicPlatform] implementation
         *
         * @since 1.0.0
         * @author IFS Institute for Software
         */
        const val DRIVER_MAVLINK_COMMON = "ch.hsr.ifs.gcs.driver.platform.mavlink.CommonPlatform"

        private const val COMPONENT_MISSION_PLANNER = 190
    }

    private enum class ExecutionState {
        CREATED,
        UPLOADING
    }

    private inner class NativeMissionExecution(tasks: List<Task>) : Execution(tasks) {

        private var fState = ExecutionState.CREATED

        private fun initiateUpload() {
            val message = createMAVLinkMessage(MessageID.MISSION_COUNT, senderSystem, schema)
            message["target_system"] = targetSystem
            message["target_component"] = COMPONENT_MISSION_PLANNER
            message["count"] = tasks.size
            awaitResponse(message, MessageID.MISSION_REQUEST, 1, TimeUnit.SECONDS) {
                it?.let {
                    transmitItem(it.getInt("seq"))
                } ?: initiateUpload()
            }
        }

        private fun transmitItem(index: Int) {

        }

        private fun upload() {
            fState = ExecutionState.UPLOADING
        }

        override fun tick() = when (fState) {
            CommonPlatform.ExecutionState.CREATED -> {
                upload()
                Status.PREPARING
            }
            CommonPlatform.ExecutionState.UPLOADING -> Status.PREPARING
        }

    }

    override val driverId get() = DRIVER_MAVLINK_COMMON

    override fun getExecutionFor(tasks: List<Task>) = NativeMissionExecution(tasks) as Execution

}