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

    private var fReactiveCommands = mutableMapOf<Int, PayloadCommand>()

    private val fSender = fPlatform.senderSystem
    private val fTarget = fPlatform.targetSystem
    private val fPayload = MAVLinkSystem(fTarget.id, 25)
    private val fPlatformSchema = fPlatform.schema
    private val fPayloadSchema by lazy {
        (fPlatform.payload as MAVLinkPayload).schema
    }

    private var fCurrentMissionItem = -1
    private var fLastReachedMissionItem = -1
    private var fIsOnGround = false
    private var fIsLanding = false
    private var fDidTakeOff = false

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
            ExecutionState.LAUNCHED -> Status.RUNNING
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
            fReactiveCommands[itemNumber]?.run {
                Log.i(LOG_TAG, "Reacting to having reached point: $itemNumber ")
                val message = createTargetedMAVLinkMessage(name, fSender, fPayload, fPayloadSchema).apply {
                    for (param in data) {
                        set(param.key, param.value)
                    }
                }
                fPlatform.send(fPlatform.payloadTunnel.encode(message))
            }
        }
    }

    override fun handleLandedState(state: Int) {
        when (state) {
            0 -> {
                fIsOnGround = false
                fIsLanding = false
                fDidTakeOff = false
            }
            1 -> {
                fIsOnGround = true
            }
            2 -> {
                fIsOnGround = false
                fDidTakeOff = true
                fIsLanding = false
            }
            3 -> {
                fIsOnGround = true
                fDidTakeOff = false
                fIsLanding = false
            }
            4 -> {
                fIsOnGround = false
                fIsLanding = false
            }
        }
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

