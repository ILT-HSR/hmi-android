package ch.hsr.ifs.gcs.driver.mavlink.platform

import android.util.Log
import ch.hsr.ifs.gcs.driver.Command
import ch.hsr.ifs.gcs.driver.mavlink.*
import ch.hsr.ifs.gcs.driver.mavlink.support.*
import ch.hsr.ifs.gcs.mission.Execution
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking
import me.drton.jmavlib.mavlink.MAVLinkMessage

/**
 * States of a [mission execution][MissionExecution]
 */
private enum class ExecutionState {
    CREATED,
    UPLOADING,
    UPLOADED,
    LAUNCHING,
    LAUNCHED,
    FAILED,
    COMPLETED
}

private enum class LandedState {
    UNDEFINED,
    ON_GROUND,
    IN_AIR,
    TAKEOFF,
    LANDING;

    companion object {
        fun from(value: Int) = values().sortedBy(LandedState::ordinal)[value]
    }
}

private sealed class ItemKind

private object Last : ItemKind()

private object Count : ItemKind()

private data class Sequenced(val number: Int) : ItemKind()

open class MissionExecution(private val fPlatform: MAVLinkPlatform) : Execution(), MAVLinkExecution {

    companion object {
        private val LOG_TAG = MissionExecution::class.java.simpleName
    }

    @Suppress("EXPERIMENTAL_API_USAGE")
    private val fContext = newSingleThreadContext("MissionExecution_${this.hashCode()}")

    private var fState = ExecutionState.CREATED
    private var fMissionSize = 0
    private var fCurrentMissionItem = -1
    private var fLastReachedMissionItem = -1
    private var fIsOnGround = false
    private var fIsLanding = false
    private var fDidTakeOff = false
    private var fIsTakingOff = false
    private var fReactiveCommands = mutableMapOf<Int, PayloadCommand>()

    private val fSender = fPlatform.senderSystem
    private val fTarget = fPlatform.targetSystem
    private val fPlatformSchema = fPlatform.schema

    // 'Execution' implementation

    override fun plusAssign(command: Command<*>) {
        assert(command is MAVLinkCommand)
        when (val nativeCommand = command.nativeCommand) {
            is PlanCommand -> fMissionSize++
            is PayloadCommand -> {
                assert(fMissionSize > 0)
                fReactiveCommands[fMissionSize - 1] = nativeCommand
            }
        }
        super.plusAssign(command)
    }

    override fun tick() = runBlocking(fContext) {
        when (fState) {
            ExecutionState.CREATED -> {
                fState = ExecutionState.UPLOADING
                launch(Dispatchers.IO) { fState = upload() }
                Status.PREPARING
            }
            ExecutionState.UPLOADING -> Status.PREPARING
            ExecutionState.UPLOADED -> {
                fState = ExecutionState.LAUNCHING
                launch(Dispatchers.IO) { fState = launch() }
                Status.PREPARING
            }
            ExecutionState.LAUNCHING -> Status.PREPARING
            ExecutionState.LAUNCHED ->
                if (fDidTakeOff && fIsOnGround) {
                    fState = ExecutionState.COMPLETED
                    Status.FINISHED
                } else {
                    Status.RUNNING
                }
            ExecutionState.FAILED -> Status.FAILURE
            ExecutionState.COMPLETED -> Status.FINISHED
        }
    }

    // 'MAVLinkExecution' implementation

    override fun handleCurrentMissionItem(itemNumber: Int) {
        if (itemNumber != fCurrentMissionItem) {
            fCurrentMissionItem = itemNumber
        }
    }

    override fun handleMissionItemReached(itemNumber: Int) {
        if (itemNumber != fLastReachedMissionItem) {
            fLastReachedMissionItem = itemNumber
            fReactiveCommands[itemNumber]?.let { fire(it) }
        }
    }

    private fun fire(command: PayloadCommand): Unit {
        val payload = fPlatform.payloads.filterIsInstance<MAVLinkPayload>()
                .find { it.system == command.system } ?: return
        val tunnel = fPlatform.payloadTunnels[payload.system] ?: return

        val message = createTargetedMAVLinkMessage(command.name, fSender, payload.system!!, payload.schema).apply {
            command.data.forEach { (parameter, value) ->
                set(parameter, value)
            }
        }
        Log.i(LOG_TAG, "Firing payload command: $message")
        fPlatform.send(tunnel.encode(message))
    }

    override fun handleLandedState(state: Int) {
        assert(state in 0..4)
        when (LandedState.from(state)) {
            LandedState.UNDEFINED -> {
                fIsOnGround = false
                fDidTakeOff = false
                fIsTakingOff = false
                fIsLanding = false
            }
            LandedState.ON_GROUND -> {
                fIsOnGround = true
                fIsLanding = false
                fIsTakingOff = false
            }
            LandedState.IN_AIR -> {
                fIsOnGround = false
                fIsLanding = false
                fDidTakeOff = fIsTakingOff
            }
            LandedState.TAKEOFF -> {
                fIsTakingOff = false
                fIsOnGround = true
                fIsLanding = false
                fIsTakingOff = true
            }
            LandedState.LANDING -> {
                fIsOnGround = false
                fIsLanding = true
            }
        }
    }

    override fun reset() {
        fState = ExecutionState.CREATED
        fMissionSize = 0
        fCurrentMissionItem = -1
        fLastReachedMissionItem = -1
        fIsOnGround = false
        fIsLanding = false
        fDidTakeOff = false
        fIsTakingOff = false
        fReactiveCommands = mutableMapOf<Int, PayloadCommand>()
        super.reset()
    }

    // 'Private' implementation

    private suspend fun upload(): ExecutionState {
        val count = createTargetedMAVLinkMessage(MessageID.MISSION_COUNT, fSender, fTarget, fPlatformSchema)
        count["count"] = fMissionSize

        if (!sendMissionCommand(count, Count)) {
            return ExecutionState.FAILED
        }

        fCommands.filter { it.nativeCommand is PlanCommand }.forEachIndexed { index, command ->
            if (!sendPlanItem(index, command.nativeCommand as PlanCommand)) {
                return ExecutionState.FAILED
            }
        }

        return ExecutionState.UPLOADED
    }

    private suspend fun launch(): ExecutionState {
        val arm = (fPlatform.arm().nativeCommand as PlanCommand).asMessage(fSender, fTarget, fPlatformSchema)

        if (!fPlatform.sendCommand(arm)) {
            return ExecutionState.FAILED
        }

        val startMission = createLongCommandMessage(fSender, fTarget, fPlatformSchema, LongCommand.MISSION_START).apply {
            set("param1", 0)
            set("param2", fMissionSize - 1)
        }

        if (!fPlatform.sendCommand(startMission)) {
            return ExecutionState.FAILED
        }

        return ExecutionState.LAUNCHED
    }

    private suspend fun sendPlanItem(sequenceNumber: Int, command: PlanCommand): Boolean {
        val item = createTargetedMAVLinkMessage(MessageID.MISSION_ITEM, fSender, fTarget, fPlatformSchema)

        item["seq"] = sequenceNumber
        item["frame"] = command.frame.ordinal
        item["command"] = command.id.value
        item["current"] = if (sequenceNumber == 0) 1 else 0
        item["autocontinue"] = 1
        item["param1"] = command.param1
        item["param2"] = command.param2
        item["param3"] = command.param3
        item["param4"] = command.param4
        item["x"] = command.x
        item["y"] = command.y
        item["z"] = command.z

        return if (sequenceNumber != fMissionSize - 1) {
            sendMissionCommand(item, Sequenced(sequenceNumber))
        } else {
            sendMissionCommand(item, Last)
        }
    }

    private suspend fun sendMissionCommand(command: MAVLinkMessage, kind: ItemKind) =
            when (kind) {
                is Last -> fPlatform.sendWithAck(command, MessageID.MISSION_ACK) {
                    it.getInt("type") == 0
                }
                is Sequenced -> fPlatform.sendWithAck(command, MessageID.MISSION_REQUEST) {
                    it.getInt("seq") == kind.number + 1
                }
                is Count -> {
                    fPlatform.sendWithAck(command, MessageID.MISSION_REQUEST) {
                        it.getInt("seq") == 0
                    }
                }
            }
}

