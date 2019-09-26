package ch.hsr.ifs.gcs.driver.mavlink.platform

import android.util.Log
import ch.hsr.ifs.gcs.driver.AerialVehicle
import ch.hsr.ifs.gcs.driver.Payload
import ch.hsr.ifs.gcs.driver.PayloadContext
import ch.hsr.ifs.gcs.driver.PlatformContext
import ch.hsr.ifs.gcs.driver.mavlink.*
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
abstract class BasicPlatform(channel: ByteChannel, payloads: List<Payload>, final override val schema: MAVLinkSchema) : MAVLinkPlatform {

    companion object {
        /**
         * The log prefix for log entries emitted by this implementation
         */
        private val LOG_TAG = BasicPlatform::class.simpleName

        /**
         * The component id of the platforms autopilot
         */
        private const val COMPONENT_AUTOPILOT = 1

        /**
         * The component id of the payload
         */
        private const val COMPONENT_USER1 = 25

        /**
         * The component id of the local mission planner
         *
         */
        private const val COMPONENT_MISSION_PLANNER = 190
    }

    private val fLocalSystem = MAVLinkSystem(255, COMPONENT_MISSION_PLANNER)
    private val fPlatformSystem = MAVLinkSystem(1, COMPONENT_AUTOPILOT)
    private val fPayloadSystem = MAVLinkSystem(1, COMPONENT_USER1)

    private val fMessageStream = MAVLinkStream(schema, channel)
    private val fMessageQueue = ConcurrentLinkedQueue<MAVLinkMessage>()

    private lateinit var fHeartbeatJob: Job
    private lateinit var fSurveillanceJob: Job
    private lateinit var fReceiverJob: Job
    private lateinit var fSenderJob: Job

    protected abstract var fExecution: MissionExecution

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
    private var fPendingAcknowledgements: MutableMap<MessageID, MutableList<Pair<(MAVLinkMessage) -> Boolean, CompletableDeferred<MAVLinkMessage>>>> = mutableMapOf()

    sealed class MessageEvent {
        data class Heartbeat(val message: MAVLinkMessage) : MessageEvent()
        data class Position(val message: MAVLinkMessage) : MessageEvent()
        data class Version(val message: MAVLinkMessage) : MessageEvent()
        data class LongCommandAcknowledgement(val message: MAVLinkMessage) : MessageEvent()
        data class MissionItemReached(val sequenceNumber: Int) : MessageEvent()
        data class MissionCurrentChanged(val sequenceNumber: Int) : MessageEvent()
        data class ExtendedSystemState(val vtolState: Int, val landedState: Int) : MessageEvent()
        data class Ping(val message: MAVLinkMessage) : MessageEvent()
        data class TunneledMessage(val message: MAVLinkMessage) : MessageEvent()

        data class SendMessage(val message: MAVLinkMessage) : MessageEvent()
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
                    fTimeSinceBoot = message.getInt("time_boot_ms")
                    fPosition = GPSPosition(WGS89Position(message.getInt("lat"), message.getInt("lon"), message.getInt("alt")))
                    fGroundSpeed.north = message.getInt("vx")
                    fGroundSpeed.east = message.getInt("vy")
                    fGroundSpeed.up = message.getInt("vz")
                }
                is MessageEvent.Version -> event.message.let { message ->
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

                is MessageEvent.MissionItemReached -> with(event.sequenceNumber) {
                    fExecution.handleMissionItemReached(this)
                }
                is MessageEvent.MissionCurrentChanged -> with(event.sequenceNumber) {
                    fExecution.handleCurrentMissionItem(this)
                }
                is MessageEvent.ExtendedSystemState -> {
                    fExecution.handleLandedState(event.landedState)
                }
                is MessageEvent.Ping -> event.message.let { ping ->
                    createMAVLinkMessage(MessageID.PING, fLocalSystem, schema).let { pong ->
                        pong.set("time_usec", ping.getLong("time_usec"))
                        pong.set("seq", ping.getInt("seq"))
                        pong.set("target_system", ping.systemID)
                        pong.set("target_component", ping.componentID)
                        send(pong)
                    }
                }
                is MessageEvent.TunneledMessage -> event.message.let { tunneled ->
                    Log.i(LOG_TAG, "Tunneled Message")
                    val innerMessage = payloadTunnel.decode(tunneled)
                    launch(PayloadContext) {
                        (payload as MAVLinkPayload).handle(innerMessage, this@BasicPlatform)
                    }
                }

                // Outgoing messages
                is MessageEvent.SendMessage -> event.message.let { message ->
                    Log.i(LOG_TAG, "Sending message: $message")
                    fMessageQueue.offer(message)
                }
            }
        }
    }

    protected fun start() {
        fReceiverJob = startReceiver()
        fSenderJob = startSender()
        fHeartbeatJob = startHeartbeat()
        fSurveillanceJob = startSurveyor()
        GlobalScope.launch(PlatformContext) {
            sendCommand(createRequestAutopilotCapabilitiesMessage(fLocalSystem, fPlatformSystem, schema))
        }
    }

// Platform implementation

    override val name get() = runBlocking(PlatformContext) { fProduct }
    override val isAlive get() = runBlocking(PlatformContext) { fIsAlive }
    override val currentPosition get() = runBlocking(PlatformContext) { fPosition }
    override var payload: Payload = payloads[0]
    override val execution: Execution get() = fExecution

// AerialVehicle implementation

    override fun moveTo(position: GPSPosition) = MAVLinkCommand(PlanCommand(
            LongCommand.NAV_WAYPOINT,
            frame = NavigationFrame.GLOBAL_RELATIVE_ALTITUDE,
            param4 = Float.NaN,
            x = position.latitude.toFloat(),
            y = position.longitude.toFloat(),
            z = position.altitude.toFloat()
    ))

    override fun changeAltitude(altitude: AerialVehicle.Altitude) = MAVLinkCommand(PlanCommand(
            LongCommand.NAV_LOITER_TO_ALT,
            frame = NavigationFrame.GLOBAL_RELATIVE_ALTITUDE,
            z = altitude.meters.toFloat()
    ))

    override fun returnToLaunch() = MAVLinkCommand(PlanCommand(
            LongCommand.NAV_RETURN_TO_LAUNCH,
            frame = NavigationFrame.MISSION
    ))

    override fun takeOff(altitude: AerialVehicle.Altitude) = MAVLinkCommand(PlanCommand(
            LongCommand.NAV_TAKEOFF,
            frame = NavigationFrame.GLOBAL_RELATIVE_ALTITUDE,
            x = fPosition?.latitude?.toFloat() ?: Float.NaN,
            y = fPosition?.longitude?.toFloat() ?: Float.NaN,
            z = altitude.meters.toFloat()
    ))

    override fun land() = MAVLinkCommand(PlanCommand(
            LongCommand.NAV_LAND,
            frame = NavigationFrame.GLOBAL_RELATIVE_ALTITUDE,
            x = Float.NaN,
            y = Float.NaN,
            z = Float.NaN
    ))

    override fun limitTravelSpeed(speed: Double) = MAVLinkCommand(PlanCommand(
            LongCommand.DO_CHANGE_SPEED,
            frame = NavigationFrame.MISSION,
            param1 = 1.0f,
            param2 = speed.toFloat(),
            param3 = -1.0f,
            param4 = 0.0f
    ))

// MAVLinkPlatform implementation

    override val payloadTunnel by lazy {
        assert(payload is MAVLinkPayload)
        MAVLinkTunnel(this, fPayloadSystem, fLocalSystem, (payload as MAVLinkPayload).schema)
    }

    override fun arm() = MAVLinkCommand(PlanCommand(
            LongCommand.COMPONENT_ARM_DISARM,
            frame = NavigationFrame.MISSION,
            param1 = 1.toFloat()
    ))

    override fun disarm() = MAVLinkCommand(PlanCommand(
            LongCommand.COMPONENT_ARM_DISARM,
            frame = NavigationFrame.MISSION,
            param1 = 1.toFloat()
    ))

    override fun send(message: MAVLinkMessage) {
        fMainActor.offer(MessageEvent.SendMessage(message))
    }

    override suspend fun sendWithAck(message: MAVLinkMessage, ack: MessageID, retries: Int, matching: (MAVLinkMessage) -> Boolean): Boolean {
        val result = CompletableDeferred<MAVLinkMessage>()
        val handlers = fPendingAcknowledgements.getOrPut(ack) {
            mutableListOf()
        }
        val ackHandler = Pair(matching, result)
        handlers.add(ackHandler)

        for (retry in 0 until retries) {
            send(message)
            withTimeoutOrNull(1000) {
                result.await()
            }?.let {
                handlers.remove(ackHandler)
                return true
            }
        }

        handlers.remove(ackHandler)
        return false
    }

    override suspend fun sendCommand(message: MAVLinkMessage): Boolean {
        assert(message.msgName == MessageID.COMMAND_LONG.name)
        if (fPendingLongCommand != null) {
            Log.w(LOG_TAG, "A Long-Command is already in flight. Dropping '$message'")
            return false
        }
        val waiter = CompletableDeferred<MAVLinkMessage>()
        fPendingLongCommand = Pair(message.getInt("command"), waiter)
        for (retry in 1..5) {
            send(message)
            withTimeoutOrNull(1000) {
                waiter.await()
            }?.let {
                fPendingLongCommand = null
                return it.getInt("result") == 0
            } ?: send(message.apply {
                set("confirmation", retry)
            })
        }
        fPendingLongCommand?.second?.cancel()
        fPendingLongCommand = null
        return false
    }

    override val senderSystem get() = fLocalSystem

    override val targetSystem get() = fPlatformSystem

// Private implementation

    private fun startReceiver() = GlobalScope.launch(Dispatchers.IO) {
        while (isActive) {
            try {
                fMessageStream.read()?.let { dispatch(it) }
            } catch (e: IOException) {
                Log.w(LOG_TAG, "I/O Exception while reading message from remote: ${e.localizedMessage}")
            }
        }
    }

    private fun startSender() = GlobalScope.launch(Dispatchers.IO) {
        while (isActive) {
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
            send(createHeartbeatMessage(fLocalSystem, schema))
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
        when (val id = MessageID.from(message.msgName)) {
            null -> Log.v(LOG_TAG, "Unknown message '${message.msgName}'")
            MessageID.HEARTBEAT -> fMainActor.offer(MessageEvent.Heartbeat(message))
            MessageID.AUTOPILOT_VERSION -> fMainActor.offer(MessageEvent.Version(message))
            MessageID.GLOBAL_POSITION_INT -> fMainActor.offer(MessageEvent.Position(message))
            MessageID.COMMAND_ACK -> fMainActor.offer(MessageEvent.LongCommandAcknowledgement(message))
            MessageID.MISSION_ITEM_REACHED -> fMainActor.offer(MessageEvent.MissionItemReached(message.getInt("seq")))
            MessageID.MISSION_CURRENT -> fMainActor.offer(MessageEvent.MissionCurrentChanged(message.getInt("seq")))
            MessageID.EXTENDED_SYS_STATE -> fMainActor.offer(MessageEvent.ExtendedSystemState(message.getInt("vtol_state"), message.getInt("landed_state")))
            MessageID.PING -> fMainActor.offer(MessageEvent.Ping(message))
            MessageID.TUNNEL -> fMainActor.offer(MessageEvent.TunneledMessage(message))
            else -> fPendingAcknowledgements[id]?.let { handlers ->
                handlers.removeAll {
                    it.second.isCancelled
                }
                for (handler in handlers) {
                    if (handler.first(message)) {
                        handler.second.complete(message)
                    }
                }
            } ?: Log.v(LOG_TAG, "Unhandled message: $message")
        }
    }

}