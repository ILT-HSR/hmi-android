package ch.hsr.ifs.gcs.driver.mavlink.platform

import android.util.Log
import ch.hsr.ifs.gcs.driver.AerialVehicle
import ch.hsr.ifs.gcs.driver.Command
import ch.hsr.ifs.gcs.driver.Payload
import ch.hsr.ifs.gcs.driver.PlatformContext
import ch.hsr.ifs.gcs.driver.mavlink.MAVLinkCommand
import ch.hsr.ifs.gcs.driver.mavlink.MAVLinkPlatform
import ch.hsr.ifs.gcs.driver.mavlink.payload.NullPayload
import ch.hsr.ifs.gcs.driver.mavlink.support.*
import ch.hsr.ifs.gcs.mission.Execution
import ch.hsr.ifs.gcs.support.geo.GPSPosition
import ch.hsr.ifs.gcs.support.geo.WGS89Position
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.actor
import me.drton.jmavlib.mavlink.*
import java.io.IOException
import java.nio.channels.ByteChannel
import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.TimeUnit

/**
 * This class provides a basic [platform][ch.hsr.ifs.gcs.driver.Platform] implementation to be used
 * when implementing MAVLink vehicle platforms. It provides infrastructure for gcs-vehicle
 * communication and other schema independent functionality.
 *
 * @since 1.0.0
 * @author IFS Institute for Software
 */
abstract class BasicPlatform(channel: ByteChannel, final override val schema: MAVLinkSchema) : MAVLinkPlatform {

    companion object {
        /**
         * The log prefix for log entries emitted by this implementation
         */
        private val LOG_TAG = BasicPlatform::class.simpleName

        /**
         * The component id of the onboard MAVLink mission planner
         */
        private const val COMPONENT_AUTOPILOT = 1

        /**
         * The component id of the onboard MAVLink mission planner
         */
        private const val COMPONENT_MISSION_PLANNER = 190
    }

    private val fSender = MAVLinkSystem(255, COMPONENT_MISSION_PLANNER)
    private val fTarget = MAVLinkSystem(1, COMPONENT_AUTOPILOT)

    private val fMessageStream = MAVLinkStream(schema, channel)
    private val fMessageQueue = ConcurrentLinkedQueue<MAVLinkMessage>()

    private val fHeartbeatJob: Job
    private val fSurveillanceJob: Job
    private val fReceiverJob: Job
    private val fSenderJob: Job

    private var fCurrentExecution = NativeMissionExecution(fTarget)

    private var fIsAlive = false
    private var fLastHeartbeat = Instant.ofEpochSecond(0)
    private var fTimeSinceBoot = 0
    private var fVendor = "<unknown>"
    private var fProduct = "<unknown>"
    private var fId = 0L
    private var fPosition: GPSPosition? = null
    private val fGroundSpeed = object {
        var north = 0
        var east = 0
        var up = 0
    }

    private var fPendingLongCommand: Pair<Int, CompletableDeferred<MAVLinkMessage>>? = null
    private var fPendingMissionCommand: Pair<MessageID, CompletableDeferred<MAVLinkMessage>>? = null

    sealed class MessageEvent {
        data class Heartbeat(val message: MAVLinkMessage) : MessageEvent()
        data class Position(val message: MAVLinkMessage) : MessageEvent()
        data class Version(val message: MAVLinkMessage) : MessageEvent()
        data class LongCommandAcknowledgement(val message: MAVLinkMessage) : MessageEvent()
        data class MissionRequest(val message: MAVLinkMessage) : MessageEvent()
        data class MissionAcknowledgement(val message: MAVLinkMessage) : MessageEvent()

        data class SendLongCommand(val message: MAVLinkMessage, val result: CompletableDeferred<MAVLinkMessage>) : MessageEvent()
        data class SendMessage(val message: MAVLinkMessage) : MessageEvent()
        data class SendMissionMessage(val message: MAVLinkMessage, val expected: MessageID, val result: CompletableDeferred<MAVLinkMessage>) : MessageEvent()
    }

    private val fMainActor = GlobalScope.actor<MessageEvent>(PlatformContext, Channel.UNLIMITED) {
        for (event in this) {
            @Suppress("IMPLICIT_CAST_TO_ANY")
            when (event) {
                // Incoming messages
                is MessageEvent.Heartbeat -> {
                    fLastHeartbeat = Instant.now()
                }
                is MessageEvent.Position -> event.message.let { message ->
                    Log.i(LOG_TAG, "Position update: $message")
                    fTimeSinceBoot = message.getInt("time_boot_ms")
                    fPosition = GPSPosition(WGS89Position(message.getInt("lat"), message.getInt("lon"), message.getInt("alt")))
                    fGroundSpeed.north = message.getInt("vx")
                    fGroundSpeed.east = message.getInt("vy")
                    fGroundSpeed.up = message.getInt("vz")
                }
                is MessageEvent.Version -> event.message.let { message ->
                    Log.i(LOG_TAG, "Version: $message")
                    fVendor = MAVLinkVendors[message["vendor_id"] as? Int ?: 0]
                    fProduct = MAVLinkProducts[message["product_id"] as? Int ?: 0]
                    fId = message.getLong("uid")
                }
                is MessageEvent.LongCommandAcknowledgement -> event.message.let { message ->
                    val pending = fPendingLongCommand
                    Log.i(LOG_TAG, "Long-Command response: $message")
                    when {
                        pending == null -> Log.w(LOG_TAG, "Unexpected long command ack: $message")
                        pending.first != message.getInt("command") -> Log.w(LOG_TAG, "Stray long command ack: $message")
                        else -> {
                            pending.second.complete(message)
                            fPendingLongCommand = null
                        }
                    }
                }
                is MessageEvent.MissionRequest -> event.message.let { message ->
                    val pending = fPendingMissionCommand
                    Log.i(LOG_TAG, "Mission Item Request: $message")
                    when {
                        pending == null -> Log.w(LOG_TAG, "Unexpected mission command response: $message")
                        pending.first != MessageID.MISSION_REQUEST -> Log.w(LOG_TAG, "Stray mission command response: $message")
                        else -> {
                            pending.second.complete(message)
                            fPendingMissionCommand = null
                        }
                    }
                }
                is MessageEvent.MissionAcknowledgement -> event.message.let { message ->
                    val pending = fPendingMissionCommand
                    Log.i(LOG_TAG, "Mission Item Acknowledgement: $message")
                    when {
                        pending == null -> Log.w(LOG_TAG, "Unexpected mission command response: $message")
                        pending.first != MessageID.MISSION_ACK -> Log.w(LOG_TAG, "Stray mission command response: $message")
                        else -> {
                            pending.second.complete(message)
                            fPendingMissionCommand = null
                        }
                    }
                }

                // Outgoing messages
                is MessageEvent.SendLongCommand -> event.message.let { message ->
                    if (fPendingLongCommand != null) {
                        Log.w(LOG_TAG, "Another command is already waiting. Dropping: $message")
                    } else {
                        Log.i(LOG_TAG, "Sending command: $message")
                        fPendingLongCommand = Pair(message.getInt("command"), event.result)
                        sendMessage(message)
                    }
                }
                is MessageEvent.SendMessage -> event.message.let { message ->
                    fMessageQueue.offer(message)
                }
                is MessageEvent.SendMissionMessage -> event.message.let { message ->
                    if (fPendingMissionCommand != null) {
                        Log.w(LOG_TAG, "Another mission command is already waiting. Dropping: $message")
                    } else {
                        Log.i(LOG_TAG, "Sending mission command: $message")
                        fPendingMissionCommand = Pair(event.expected, event.result)
                        sendMessage(message)
                    }
                }
            }
        }
    }

    protected open inner class NativeMissionExecution(target: MAVLinkSystem) : Execution(), MAVLinkExecution {

        private var fState = ExecutionState.CREATED

        override fun tick() = runBlocking(PlatformContext) {
            when (fState) {
                ExecutionState.CREATED -> {
                    fState = ExecutionState.UPLOADING
                    launch(PlatformContext) { upload() }
                    Status.PREPARING
                }
                ExecutionState.UPLOADING -> Status.PREPARING
                ExecutionState.RUNNING -> Status.RUNNING
                ExecutionState.FAILED -> Status.FAILURE
            }
        }

        override operator fun plusAssign(command: Command<*>) {
            assert(command is MAVLinkCommand)
            super.plusAssign(command)
        }

        private suspend fun upload() {
            Log.i(LOG_TAG, "Starting upload")
            val count = createTargetedMAVLinkMessage(MessageID.MISSION_COUNT, senderSystem, fTarget, schema)
            count["count"] = fCommands.size

            if (sendMissionCommand(count, MessageID.MISSION_REQUEST) == null) {
                fState = ExecutionState.FAILED
                return
            }

            var response: MAVLinkMessage? = null
            fCommands.forEachIndexed { idx, cmd ->
                response = sendItem(idx, cmd)
                if (response == null) {
                    fState = ExecutionState.FAILED
                    return
                }
            }

            if (response?.msgName != MessageID.MISSION_ACK.name || response?.getInt("type") != 0) {
                fState = ExecutionState.FAILED
                return
            }

            response = sendLongCommand(createArmMessage(fSender, fTarget, schema))
            if (response == null || response?.getInt("result") != 0) {
                fState = ExecutionState.FAILED
                return
            }

            val launch = createLongCommandMessage(fSender, fTarget, schema, LongCommand.MISSION_START).apply {
                set("param1", 0)
                set("param2", fCommands.size - 1)
            }
            response = sendLongCommand(launch)
            if (response == null || response?.getInt("result") != 0) {
                fState = ExecutionState.FAILED
                return
            }

            fState = ExecutionState.RUNNING
        }

        private suspend fun sendItem(index: Int, command: Command<*>): MAVLinkMessage? {
            val nativeCommand = command.nativeCommand as MAVLinkMissionCommand
            val item = createTargetedMAVLinkMessage(MessageID.MISSION_ITEM, senderSystem, fTarget, schema)

            item["seq"] = index
            item["frame"] = nativeCommand.frame.ordinal
            item["command"] = nativeCommand.id.value
            item["current"] = if (index == 0) 1 else 0
            item["autocontinue"] = 1
            item["param1"] = nativeCommand.param1
            item["param2"] = nativeCommand.param2
            item["param3"] = nativeCommand.param3
            item["param4"] = nativeCommand.param4
            item["x"] = nativeCommand.x
            item["y"] = nativeCommand.y
            item["z"] = nativeCommand.z

            if (index < fCommands.size - 1) {
                return sendMissionCommand(item, MessageID.MISSION_REQUEST)
            }

            return sendMissionCommand(item, MessageID.MISSION_ACK)
        }

    }

    protected enum class ExecutionState {
        CREATED,
        UPLOADING,
        RUNNING,
        FAILED,
    }

    init {
        fReceiverJob = startReceiver()
        fSenderJob = startSender()
        fHeartbeatJob = startHeartbeat()
        fSurveillanceJob = startSurveyor()
        GlobalScope.launch(PlatformContext) {
            sendLongCommand(createRequestAutopilotCapabilitiesMessage(fSender, fTarget, schema))
        }
    }

    // Platform implementation

    override val name get() = runBlocking(PlatformContext) { fProduct }
    override val isAlive get() = runBlocking(PlatformContext) { fIsAlive }
    override val currentPosition get() = runBlocking(PlatformContext) { fPosition }
    override var payload: Payload = NullPayload()
    override val execution: Execution get() = fCurrentExecution

    // AerialVehicle implementation

    override fun moveTo(position: GPSPosition) = MAVLinkCommand(MAVLinkMissionCommand(
            LongCommand.NAV_WAYPOINT,
            frame = NavigationFrame.GLOBAL_RELATIVE_ALTITUDE,
            param4 = Float.NaN,
            x = position.latitude.toFloat(),
            y = position.longitude.toFloat(),
            z = position.altitude.toFloat()
    ))

    override fun changeAltitude(altitude: AerialVehicle.Altitude) = MAVLinkCommand(MAVLinkMissionCommand(
            LongCommand.NAV_LOITER_TO_ALT,
            frame = NavigationFrame.GLOBAL_RELATIVE_ALTITUDE,
            z = altitude.meters.toFloat()
    ))

    override fun returnToLaunch() = MAVLinkCommand(MAVLinkMissionCommand(
            LongCommand.NAV_RETURN_TO_LAUNCH,
            frame = NavigationFrame.MISSION
    ))

    override fun takeOff(altitude: AerialVehicle.Altitude) = MAVLinkCommand(MAVLinkMissionCommand(
            LongCommand.NAV_TAKEOFF,
            frame = NavigationFrame.GLOBAL_RELATIVE_ALTITUDE,
            x = fPosition?.latitude?.toFloat() ?: Float.NaN,
            y = fPosition?.longitude?.toFloat() ?: Float.NaN,
            z = altitude.meters.toFloat()
    ))

    override fun land() = MAVLinkCommand(MAVLinkMissionCommand(
            LongCommand.NAV_LAND,
            frame = NavigationFrame.GLOBAL_RELATIVE_ALTITUDE,
            x = Float.NaN,
            y = Float.NaN,
            z = Float.NaN
    ))

    override fun limitTravelSpeed(speed: Double) = MAVLinkCommand(MAVLinkMissionCommand(
            LongCommand.DO_CHANGE_SPEED,
            frame = NavigationFrame.MISSION,
            param1 = 1.0f,
            param2 = speed.toFloat(),
            param3 = -1.0f,
            param4 = 0.0f
    ))

    // MAVLinkPlatform implementation

    override fun arm() = MAVLinkCommand(MAVLinkMissionCommand(
            LongCommand.COMPONENT_ARM_DISARM,
            frame = NavigationFrame.MISSION,
            param1 = 1.toFloat()
    ))

    override fun disarm() = MAVLinkCommand(MAVLinkMissionCommand(
            LongCommand.COMPONENT_ARM_DISARM,
            frame = NavigationFrame.MISSION,
            param1 = 1.toFloat()
    ))

    /**
     * Get the sender system
     *
     * @since 1.0.0
     */
    override val senderSystem get() = fSender

    /**
     * Get the target system
     *
     * @since 1.0.0
     */
    override val targetSystem get() = fTarget

    // Protected implementation

    /**
     * Send a non-acknowledged MAVLink message
     *
     * @since 1.0.0
     */
    @Suppress("MemberVisibilityCanBePrivate")
    protected fun sendMessage(message: MAVLinkMessage) {
        fMainActor.offer(MessageEvent.SendMessage(message))
    }

    /**
     * Send an acknowledged MAVLink message (command protocol)
     *
     * @since 1.0.0
     */
    protected suspend fun sendLongCommand(command: MAVLinkMessage, retries: Int = 8): MAVLinkMessage? {
        val result = CompletableDeferred<MAVLinkMessage>()
        fMainActor.send(MessageEvent.SendLongCommand(command, result))

        for (retry in 0 until retries) {
            withTimeoutOrNull(500) {
                result.await()
            }?.let { return@sendLongCommand it } ?: command.apply {
                set("confirmation", (getInt("confirmation") + 1) % 256)
                sendMessage(this)
            }
        }

        result.cancel()
        fPendingLongCommand = null
        return null
    }

    /**
     * Send an acknowledged MAVLink mission command (mission protocol)
     */
    protected suspend fun sendMissionCommand(command: MAVLinkMessage, expected: MessageID, retries: Int = 8): MAVLinkMessage? {
        val result = CompletableDeferred<MAVLinkMessage>()
        fMainActor.send(MessageEvent.SendMissionMessage(command, expected, result))

        var response: MAVLinkMessage?
        for (retry in 0 until retries) {
            response = withTimeoutOrNull(500) { result.await() }
            if (response != null) {
                return response
            }
            sendMessage(command)
        }

        result.cancel()
        fPendingMissionCommand = null
        return null
    }

    // Private implementation

    private fun startReceiver() = GlobalScope.launch(Dispatchers.IO) {
        while(isActive) {
            try {
                fMessageStream.read()?.let { dispatch(it) }
            } catch (e: IOException) {
                Log.w(LOG_TAG, "I/O Exception while reading message from remote: ${e.localizedMessage}")
            }
        }
    }

    private fun startSender() = GlobalScope.launch(Dispatchers.IO) {
        while(isActive) {
            try {
                fMessageQueue.poll()?.let(fMessageStream::write)
                delay(10)
            } catch (e: IOException) {
                Log.w(LOG_TAG, "I/O Exception while sending message to remote: ${e.localizedMessage}")
            }
        }
    }

    /**
     * Schedule the transmission of 'Heartbeat' messages to the vehicle at a fixed 1s interval
     */
    private fun startHeartbeat() = GlobalScope.launch(PlatformContext) {
        while (isActive) {
            sendMessage(createHeartbeatMessage(fSender, schema))
            delay(TimeUnit.SECONDS.toMillis(1))
        }
    }

    /**
     * Schedule the surveyor actions
     */
    private fun startSurveyor() = GlobalScope.launch(PlatformContext) {
        while (isActive) {
            fIsAlive = fLastHeartbeat + Duration.ofSeconds(10) > Instant.now()
            delay(500)
        }
    }

    /**
     * Dispatch a received MAVLink message via the main actor
     */
    private fun dispatch(message: MAVLinkMessage) {
        //Log.v(LOG_TAG, "Received message: $message")
        when (MessageID.from(message.msgName)) {
            MessageID.HEARTBEAT -> {
                fMainActor.offer(MessageEvent.Heartbeat(message))
            }
            MessageID.AUTOPILOT_VERSION -> {
                fMainActor.offer(MessageEvent.Version(message))
            }
            MessageID.GLOBAL_POSITION_INT -> {
                fMainActor.offer(MessageEvent.Position(message))
            }
            MessageID.COMMAND_ACK -> {
                fMainActor.offer(MessageEvent.LongCommandAcknowledgement(message))
            }
            MessageID.MISSION_REQUEST -> {
                fMainActor.offer(MessageEvent.MissionRequest(message))
            }
            MessageID.MISSION_ACK -> {
                fMainActor.offer(MessageEvent.MissionAcknowledgement(message))
            }
            else -> {
                Log.v(LOG_TAG, "Unhandled message: $message")
            }
        }
    }

}