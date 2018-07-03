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
import ch.hsr.ifs.gcs.support.concurrent.every
import ch.hsr.ifs.gcs.support.geo.GPSPosition
import ch.hsr.ifs.gcs.support.geo.WGS89Position
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.actor
import me.drton.jmavlib.mavlink.*
import java.io.IOException
import java.nio.channels.ByteChannel
import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executors
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
        private const val COMPONENT_MISSION_PLANNER = 190
    }

    private val fSender = MAVLinkSystem(8, 250)
    private val fTarget = MAVLinkSystem(1, 1)

    private val fMessageStream = MAVLinkStream(schema, channel)
    private val fMessageQueue = ConcurrentLinkedQueue<MAVLinkMessage>()

    private val fHeartbeatJob: Job
    private val fSurveillanceJob: Job

    private val fIORunner = Executors.newSingleThreadScheduledExecutor()

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

    // Regular message handling

    sealed class MessageEvent {
        data class Heartbeat(val time: Instant) : MessageEvent()
        data class Position(val message: MAVLinkMessage) : MessageEvent()
        data class Version(val message: MAVLinkMessage) : MessageEvent()
    }

    private val fMainActor = actor<MessageEvent>(PlatformContext, Channel.UNLIMITED) {
        for (event in this) {
            when (event) {
                is MessageEvent.Heartbeat -> event.time.let { time ->
                    fLastHeartbeat = time
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
            }
        }
    }

    // Command message handling

    private var fPendingAck: CompletableDeferred<MAVLinkMessage>? = null

    sealed class CommandEvent {
        data class SendCommand(val command: MAVLinkMessage) : CommandEvent()
    }

    private val fCommandActor = actor<CommandEvent>(PlatformContext, Channel.UNLIMITED) {
        for (event in this) {
            when (event) {
                is CommandEvent.SendCommand -> event.command.let { command ->
                    fPendingAck = CompletableDeferred()
                    while (withTimeoutOrNull(250, TimeUnit.MILLISECONDS) { fPendingAck!!.await() } == null) {
                        Log.d(LOG_TAG, "Retrying")
                        sendMessage(command.apply { set("confirmation", (getInt("confirmation") + 1) % 256) })
                    }
                    fPendingAck = null
                }
            }
        }
    }

    protected open inner class NativeMissionExecution(target: MAVLinkSystem) : Execution(), MAVLinkExecution {

        private var fState = ExecutionState.CREATED
        private val fMissionPlanner = MAVLinkSystem(target.id, COMPONENT_MISSION_PLANNER)
        private lateinit var fPendingResponse: CompletableDeferred<MAVLinkMessage>

        fun handleResponse(message: MAVLinkMessage) {
            if(this::fPendingResponse.isInitialized && fPendingResponse.isActive) {
                fPendingResponse.complete(message)
            }
        }

        override fun tick() = runBlocking(PlatformContext) {
            when (fState) {
                ExecutionState.CREATED -> {
                    fState = ExecutionState.UPLOADING
                    launch { upload() }
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
            val count = createTargetedMAVLinkMessage(MessageID.MISSION_COUNT, senderSystem, fMissionPlanner, schema)
            count["count"] = fCommands.size

            fPendingResponse = CompletableDeferred()
            sendMessage(count)
            while (withTimeoutOrNull(1000, TimeUnit.MILLISECONDS) { fPendingResponse.await() } == null) {
                sendMessage(count)
            }
            var response = fPendingResponse.getCompleted()
            Log.d(LOG_TAG, "Response: $response")

            if (response.msgName == MessageID.MISSION_REQUEST.name) {
                fCommands.forEachIndexed { idx, cmd ->
                    while (response.msgName == MessageID.MISSION_REQUEST.name && response.getInt("seq") == idx) {
                        response = sendItem(idx, cmd)
                        Log.d(LOG_TAG, "Response: $response")
                    }
                }
            }

            if (response.msgName == MessageID.MISSION_ACK.name) {
                fState = if (response.getInt("type") == 0) {
                    sendCommand(createArmMessage(fSender, fTarget, schema))
                    delay(1000)
                    sendCommand(createLongCommandMessage(fSender, fTarget, schema, LongCommand.MISSION_START).apply {
                        set("param1", 0)
                        set("param2", fCommands.size - 1)
                    })
                    ExecutionState.RUNNING
                } else {
                    ExecutionState.FAILED
                }
            } else {
                Log.i(LOG_TAG, "Mission start failed")
                fState = ExecutionState.FAILED
            }
        }

        private suspend fun sendItem(index: Int, command: Command<*>): MAVLinkMessage {
            val nativeCommand = command.nativeCommand as MAVLinkMissionCommand
            val item = createTargetedMAVLinkMessage(MessageID.MISSION_ITEM, senderSystem, fMissionPlanner, schema)

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

            fPendingResponse = CompletableDeferred()
            sendMessage(item)
            Log.i(LOG_TAG, "Sending $item")
            while (withTimeoutOrNull(1000, TimeUnit.MILLISECONDS) { fPendingResponse.await() } == null) {
                sendMessage(item)
            }

            return fPendingResponse.getCompleted()
        }

    }

    init {
        beginSerialIO()
        fHeartbeatJob = startHeartbeat()
        fSurveillanceJob = startSurveyor()
        sendCommand(createRequestAutopilotCapabilitiesMessage(fSender, fTarget, schema))
    }

    protected enum class ExecutionState {
        CREATED,
        UPLOADING,
        RUNNING,
        FAILED,
    }

    // Platform implementation

    override val name get() = runBlocking(PlatformContext) { fProduct }
    override val isAlive get() = runBlocking(PlatformContext) { fIsAlive }
    override val currentPosition get() = runBlocking(PlatformContext) { fPosition }
    override var payload: Payload = NullPayload()
    override val execution: Execution by lazy { NativeMissionExecution(targetSystem) }

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
            x = Float.NaN,
            y = Float.NaN,
            z = altitude.meters.toFloat()
    ))

    override fun land() = MAVLinkCommand(MAVLinkMissionCommand(
            LongCommand.NAV_LAND,
            frame = NavigationFrame.GLOBAL_RELATIVE_ALTITUDE,
            x = Float.NaN,
            y = Float.NaN,
            z = Float.NaN
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
     * Enqueue a single command into the internal command queue
     *
     * @since 1.0.0
     */
    protected fun sendMessage(message: MAVLinkMessage) {
        fMessageQueue.offer(message)
    }

    /**
     * Enqueue a series of commands into the internal command queue
     *
     * @since 1.0.0
     */
    protected fun sendMessages(vararg messages: MAVLinkMessage) {
        fMessageQueue.addAll(messages)
    }

    protected fun sendCommand(command: MAVLinkMessage) {
        fCommandActor.offer(CommandEvent.SendCommand(command))
    }

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

    /**
     * Initialize the serial I/O connection with the vehicle.
     *
     * We communicate with the vehicle using a serial (RS232) interface. The serial connection is
     * wrapped in a MAVLink message stream, which allows us to work on a more 'abstract' level.
     */
    private fun beginSerialIO() {
        fIORunner.every(Duration.ofMillis(10)) {
            try {
                fMessageStream.read()?.let(this::dispatch)
                fMessageQueue.poll()?.let(fMessageStream::write)
            } catch (e: IOException) {
            }
        }
    }

    /**
     * Schedule the transmission of 'Heartbeat' messages to the vehicle at a fixed 1s interval
     */
    private fun startHeartbeat() = launch(PlatformContext) {
        while (isActive) {
            sendMessage(createHeartbeatMessage(fSender, schema))
            delay(1, TimeUnit.SECONDS)
        }
    }

    /**
     * Schedule the surveyor actions
     */
    private fun startSurveyor() = launch(PlatformContext) {
        while (isActive) {
            if (fIsAlive && fLastHeartbeat + Duration.ofSeconds(10) < Instant.now()) {
                fIsAlive = false
            }
            delay(100, TimeUnit.MILLISECONDS)
        }
    }

    /**
     * Dispatch a received MAVLink message to the associated listener(s)
     */
    private fun dispatch(message: MAVLinkMessage) {
        MessageID.from(message.msgName)?.let {
            when (it) {
                MessageID.HEARTBEAT -> fMainActor.offer(MessageEvent.Heartbeat(Instant.now()))
                MessageID.AUTOPILOT_VERSION -> fMainActor.offer(MessageEvent.Version(message))
                MessageID.GLOBAL_POSITION_INT -> fMainActor.offer(MessageEvent.Position(message))
                MessageID.COMMAND_ACK -> fPendingAck?.complete(message)
                MessageID.MISSION_REQUEST, MessageID.MISSION_ACK -> (execution as NativeMissionExecution).handleResponse(message)
                else -> Unit
            }
        } ?: Log.v(LOG_TAG, "unhandled: $message")
    }

}