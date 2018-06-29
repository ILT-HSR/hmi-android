package ch.hsr.ifs.gcs.driver.mavlink.platform

import android.util.Log
import ch.hsr.ifs.gcs.driver.AerialVehicle
import ch.hsr.ifs.gcs.driver.Command
import ch.hsr.ifs.gcs.driver.Payload
import ch.hsr.ifs.gcs.driver.Platform
import ch.hsr.ifs.gcs.driver.access.PayloadProvider
import ch.hsr.ifs.gcs.driver.mavlink.MAVLinkCommand
import ch.hsr.ifs.gcs.driver.mavlink.MAVLinkPlatform
import ch.hsr.ifs.gcs.driver.mavlink.payload.NullPayload
import ch.hsr.ifs.gcs.driver.mavlink.support.*
import ch.hsr.ifs.gcs.mission.Execution
import ch.hsr.ifs.gcs.support.geo.GPSPosition
import ch.hsr.ifs.gcs.support.geo.WGS89Position
import me.drton.jmavlib.mavlink.*
import java.io.IOException
import java.nio.channels.ByteChannel
import java.time.Duration
import java.time.Instant
import java.time.temporal.TemporalField
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates

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
         * The timeout used to determine if a command was received by the vehicle
         */
        private val TIMEOUT_COMMAND_ACK = Duration.ofMillis(1000)

        /**
         * The component id of the onboard MAVLink mission planner
         */
        private const val COMPONENT_MISSION_PLANNER = 190
    }


    private val fSender = MAVLinkSystem(8, 250)
    private val fTarget = MAVLinkSystem(1, 1)

    private val fMessageStream = MAVLinkStream(schema, channel)
    private val fMessageQueue = ConcurrentLinkedQueue<MAVLinkMessage>()

    private val fExecutors = object {
        val io = Executors.newSingleThreadScheduledExecutor()
        val heartbeat = Executors.newSingleThreadScheduledExecutor()
        val await = Executors.newSingleThreadScheduledExecutor()
        val surveyor = Executors.newSingleThreadScheduledExecutor()
    }

    private val fPeriodicMessages = object {
        val heartbeat = createHeartbeatMessage(fSender, schema)
        val capabilities = createRequestAutopilotCapabilitiesMessage(fSender, fTarget, schema)
    }

    private val fPlatformListeners = mutableListOf<Platform.Listener>()
    private val fMessageListeners = mutableMapOf<MessageID, MutableList<(MAVLinkMessage) -> Unit>>()
    private val fOneShotMessageListeners = mutableMapOf<MessageID, MutableList<AbortableHandler>>()

    private val fVehicleState = object {
        /**
         * The liveliness of the vehicle connection
         */
        var isAlive: Boolean by Delegates.observable(false) { _, old, new ->
            if (old != new) {
                fPlatformListeners.forEach { it.onLivelinessChanged(this@BasicPlatform) }
            }
        }

        /**
         * The GCS local time of the last received vehicle heartbeat in milliseconds
         */
        @Volatile
        var lastHeartbeat = Instant.ofEpochSecond(0)

        /**
         * The vehicle controller vendor
         */
        var vendor: String? = null

        /**
         * The vehicle controller product ID
         */
        var product: String? = null

        /**
         * The vehicle local time since the boot of the controller in milliseconds
         */
        var timeSinceBoot: Int = 0

        /**
         * The vehicle GPS position
         */
        var position: GPSPosition? = null

        /**
         * The vehicle ground speed
         */
        var groundSpeed = object {
            /**
             * The speed in north direction in centimeter per second
             */
            var north = 0

            /**
             * The speed in east direction in centimeter per second
             */
            var east = 0

            /**
             * The speed in up direction in centimeter per second
             */
            var up = 0
        }
    }

    /**
     * A state object to handle the 'Long Command' retransmissions
     */
    private val fCommandState = object {

        /**
         * The last 'Long Command' that was sent to the vehicle
         */
        var command: MAVLinkMessage? = null

        /**
         * The time of the last transmission in milliseconds
         */
        var transmittedAt = Instant.ofEpochSecond(0)

    }

    private data class AbortableHandler(private val fSuccess: (MAVLinkMessage) -> Unit, private val fFailure: () -> Unit) {
        enum class State {
            WAITING,
            SUCCEEDED,
            FAILED
        }

        private var state = State.WAITING

        fun invoke(message: MAVLinkMessage) = synchronized(this) {
            if (state == State.WAITING) {
                state = State.SUCCEEDED
                fSuccess(message)
            }
        }

        fun abort() = synchronized(this) {
            if (state == State.WAITING) {
                state = State.FAILED
                fFailure()
            }
        }
    }

    protected open inner class NativeMissionExecution(target: MAVLinkSystem) : Execution(), MAVLinkExecution {
        private val fMissionPlanner = MAVLinkSystem(target.id, COMPONENT_MISSION_PLANNER)
        private var fState = ExecutionState.CREATED

        override operator fun plusAssign(command: Command<*>) {
            assert(command is MAVLinkCommand)
            super.plusAssign(command)
        }

        private fun initiateUpload(): Unit = with(createTargetedMAVLinkMessage(MessageID.MISSION_COUNT, senderSystem, fMissionPlanner, schema)) {
            set("count", fCommands.size)
            awaitResponse(this, MessageID.MISSION_REQUEST, Duration.ofMillis(1000), {
                Log.i(LOG_TAG, "initiateUpload await succeeded.")
                transmitItem(it.getInt("seq"))
            }) {
                Log.i(LOG_TAG, "initiateUpload await failed.")
                initiateUpload()
            }
        }

        private fun transmitItem(index: Int) {
            val nativeCommand = fCommands[index].nativeCommand as MAVLinkMissionCommand
            with(createTargetedMAVLinkMessage(MessageID.MISSION_ITEM, senderSystem, fMissionPlanner, schema)) {
                set("seq", index)
                set("frame", nativeCommand.frame.ordinal)
                set("command", nativeCommand.id.value)
                set("current", if (index == 0) 1 else 0)
                set("autocontinue", 1)
                set("param1", nativeCommand.param1)
                set("param2", nativeCommand.param2)
                set("param3", nativeCommand.param3)
                set("param4", nativeCommand.param4)
                set("x", nativeCommand.x)
                set("y", nativeCommand.y)
                set("z", 5.0)

                val expectedResponse = if (index < fCommands.size - 1) {
                    MessageID.MISSION_REQUEST
                } else {
                    MessageID.MISSION_ACK
                }

                awaitResponse(this, expectedResponse, Duration.ofMillis(1000), {
                    Log.i(LOG_TAG, "transmitItem($index) await succeeded")

                    when (it.msgName) {
                        MessageID.MISSION_REQUEST.name -> transmitItem(it.getInt("seq"))
                        MessageID.MISSION_ACK.name -> {
                            enqueueCommands(
                                    createArmMessage(fSender, fTarget, schema),
                                    createDoTakeoffMessage(fSender, fTarget, schema)
                            )

                            fState = ExecutionState.RUNNING

                            with(createLongCommandMessage(fSender, fTarget, schema, LongCommand.MISSION_START)) {
                                set("param1", 0)
                                set("param2", fCommands.size - 1)
                                enqueueCommand(this)
                            }
                        }
                    }
                }) {
                    Log.i(LOG_TAG, "transmitItem($index) await failed")
                    transmitItem(index)
                }
            }
        }

        private fun upload() =
                if (fCommands.size > 0) {
                    fState = ExecutionState.UPLOADING
                    initiateUpload()
                    Status.PREPARING
                } else {
                    Status.FAILURE
                }

        override fun tick() = when (fState) {
            ExecutionState.CREATED -> upload()
            ExecutionState.UPLOADING -> Status.PREPARING
            ExecutionState.RUNNING -> Status.RUNNING
        }
    }

    init {
        registerBasicHandlers()
        beginSerialIO()
        scheduleHeartbeat()
        requestVehicleCapabilities()
        scheduleSurveyor()
    }

    protected enum class ExecutionState {
        CREATED,
        UPLOADING,
        RUNNING,
    }

    // Platform implementation

    override val name
        get() = synchronized(fVehicleState) {
            fVehicleState.vendor?.let {
                "${fVehicleState.vendor} ${fVehicleState.product}"
            } ?: "<unknown>"
        }

    override val isAlive get() = synchronized(fVehicleState) { fVehicleState.isAlive }

    override val currentPosition: GPSPosition?
        get() = synchronized(fVehicleState) { fVehicleState.position }

    override val payload: Payload
        get() = NullPayload() //fPayloadDriverId?.let { PayloadProvider.instantiate(it) } ?: NullPayload()

    override val execution: Execution
        get() = NativeMissionExecution(targetSystem)

    override fun addListener(listener: Platform.Listener) {
        fPlatformListeners += listener
    }

    override fun removeListener(listener: Platform.Listener) {
        fPlatformListeners -= listener
    }

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
    protected fun enqueueCommand(message: MAVLinkMessage) {
        fMessageQueue.offer(message)
    }

    /**
     * Enqueue a series of commands into the internal command queue
     *
     * @since 1.0.0
     */
    protected fun enqueueCommands(vararg messages: MAVLinkMessage) {
        fMessageQueue.addAll(messages)
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
     * The maximum time between 'Heartbeats' for a vehicle to be considered alive (in ms)
     *
     * @since 1.0.0
     */
    protected val maximumExpectedHeartbeatInterval: Duration = Duration.ofSeconds(10)

    /**
     * Add a listener for a specific [message type][MessageID]
     *
     * @since 1.0.0
     */
    protected fun addListener(messageName: MessageID, handler: (MAVLinkMessage) -> Unit) = synchronized(fMessageListeners) {
        fMessageListeners.computeIfAbsent(messageName) { mutableListOf() } += handler
    }

    /**
     * Await the reception of a message of the given [type][MessageID], executing the provided
     * handler when the message arrives.
     *
     * The provided handler will be executed exactly once
     *
     * @param message The message to transmit
     * @param response The message ID to listen for
     * @param handler The handler to be called on arrival of the specified message
     *
     * @since 1.0.0
     */
    protected fun awaitResponse(message: MAVLinkMessage,
                                response: MessageID,
                                handler: (MAVLinkMessage) -> Unit) = awaitResponse(message, response, Duration.ZERO, handler)

    /**
     * Await the reception of a response of the given [type][MessageID], executing the provided
     * handler when the response arrives.
     *
     * The provided handler will be executed exactly once
     *
     * @param message The message to transmit
     * @param response The response ID to listen for
     * @param timeout How long to wait for the response
     * @param handler The handler to be called on arrival of the specified response
     *
     * @since 1.0.0
     */
    protected fun awaitResponse(message: MAVLinkMessage,
                                response: MessageID,
                                timeout: Duration,
                                success: (MAVLinkMessage) -> Unit,
                                failure: () -> Unit = {}) = synchronized(fOneShotMessageListeners) {
        val handler = AbortableHandler(success, failure)
        fOneShotMessageListeners.computeIfAbsent(response) { mutableListOf() } += handler

        if (!timeout.isZero) {
            fExecutors.await.schedule({
                synchronized(fOneShotMessageListeners) {
                    fOneShotMessageListeners[response]?.remove(handler)
                }
                handler.abort()
            }, timeout.toNanos(), TimeUnit.NANOSECONDS)
        }
        enqueueCommand(message)
    }

    /**
     * Register the listeners for the [message types][MessageID] constituting the
     * basic information source of the vehicle.
     *
     * MAVLink vehicles send out certain 'data streams' consisting of different message types
     * which contain basic vehicle status information, as well as information regarding the
     * vehicle's position, heading, etc.
     */
    private fun registerBasicHandlers() {
        addListener(MessageID.HEARTBEAT, this::handleHeartbeat)
        addListener(MessageID.AUTOPILOT_VERSION, this::handleVersion)
        addListener(MessageID.GLOBAL_POSITION_INT, this::handlePosition)
        addListener(MessageID.COMMAND_ACK, this::handleCommandAcknowledgement)
    }

    /**
     * Initialize the serial I/O connection with the vehicle.
     *
     * We communicate with the vehicle using a serial (RS232) interface. The serial connection is
     * wrapped in a MAVLink message stream, which allows us to work on a more 'abstract' level.
     */
    private fun beginSerialIO() {
        fExecutors.io.scheduleAtFixedRate({
            try {
                fMessageStream.read()?.let(this::dispatch)
                fMessageQueue.peek()?.let {
                    when (it.msgName) {
                        MessageID.COMMAND_LONG.name -> sendLongCommand(it)
                        else -> sendCommand(it)
                    }
                }
            } catch (e: IOException) {
            }
        }, 0, 138, TimeUnit.MICROSECONDS)
    }

    /**
     * Schedule the transmission of 'Heartbeat' messages to the vehicle at a fixed 1s interval
     */
    private fun scheduleHeartbeat() {
        fExecutors.heartbeat.scheduleAtFixedRate({
            fMessageQueue.offer(fPeriodicMessages.heartbeat)
        }, 0, 1, TimeUnit.SECONDS)
    }

    /**
     * Schedule the surveyor actions
     */
    private fun scheduleSurveyor() {
        fExecutors.surveyor.scheduleAtFixedRate({
            if (fVehicleState.isAlive && (fVehicleState.lastHeartbeat + maximumExpectedHeartbeatInterval) > Instant.now()) {
                fVehicleState.isAlive = false
            }
        }, 0, 100, TimeUnit.MILLISECONDS)
    }


    /**
     * Request the basic vehicle capabilities
     *
     * We need to know certain aspects of the vehicle, for example the vendor and firmware version
     * in order to be able to make certain decisions.
     */
    private fun requestVehicleCapabilities() {
        fMessageQueue.offer(fPeriodicMessages.capabilities)
    }

    /**
     * Dispatch a received MAVLink message to the associated listener(s)
     */
    private fun dispatch(message: MAVLinkMessage) {
        when (MessageID.from(message.msgName)) {
            null -> Unit
            else -> {
                invokeListeners(message)
                invokeOneShotListeners(message)
            }
        }
    }

    private fun invokeListeners(message: MAVLinkMessage): Unit = synchronized(fMessageListeners) {
        val id = MessageID.from(message.msgName)!!
        fMessageListeners[id]?.forEach {
            it.invoke(message)
        }
    }

    private fun invokeOneShotListeners(message: MAVLinkMessage) {
        val id = MessageID.from(message.msgName)!!
        val handlers = synchronized(fOneShotMessageListeners) {
            val original = fOneShotMessageListeners.getOrDefault(id, mutableListOf())
            val result = original.toList()
            original.clear()
            result
        }

        handlers.forEach {
            it.invoke(message)
        }
    }

    /**
     * Process a `Heartbeat` message received on the link
     */
    @Suppress("UNUSED_PARAMETER")
    private fun handleHeartbeat(message: MAVLinkMessage): Unit = synchronized(fVehicleState) {
        val now = Instant.now()
        fVehicleState.lastHeartbeat = now
        fVehicleState.isAlive = true
    }

    /**
     * Process a `Autopilot Version` message received on the link
     */
    private fun handleVersion(message: MAVLinkMessage): Unit = synchronized(fVehicleState) {
        fVehicleState.vendor = MAVLinkVendors[message["vendor_id"] as? Int ?: 0]
        fVehicleState.product = MAVLinkProducts[message["product_id"] as? Int ?: 0]
    }

    /**
     * Process a `Global Position INT` message received on the link
     */
    private fun handlePosition(message: MAVLinkMessage): Unit = synchronized(fVehicleState) {
        fVehicleState.timeSinceBoot = message.getInt("time_boot_ms")
        fVehicleState.position = GPSPosition(WGS89Position(message.getInt("lat"), message.getInt("lon"), message.getInt("alt")))
        fVehicleState.groundSpeed.north = message.getInt("vx")
        fVehicleState.groundSpeed.east = message.getInt("vy")
        fVehicleState.groundSpeed.up = message.getInt("vz")
    }

    /**
     * Process a 'Command Long ACK' message on the link.
     */
    private fun handleCommandAcknowledgement(message: MAVLinkMessage) {
        fCommandState.command?.let {
            if (message["command"] == it["command"]) {
                fCommandState.command = null
                fMessageQueue.remove()
            }
        }
    }

    /**
     * Send a 'Command Long' message.
     *
     * We need special handling here, since we need to make sure to 'wait' for the ACK
     */
    private fun sendLongCommand(message: MAVLinkMessage) {
        when (fCommandState.command) {
            message -> {
                if (fCommandState.transmittedAt + (TIMEOUT_COMMAND_ACK) < Instant.now()) {
                    message["confirmation"] = (message["confirmation"] as Int + 1) % 256
                    fMessageStream.write(fCommandState.command)
                    fCommandState.transmittedAt = Instant.now()
                }
            }
            else -> {
                fCommandState.command = message
                fMessageStream.write(fCommandState.command)
                fCommandState.transmittedAt = Instant.now()
            }
        }
    }

    /**
     * Send a regular command
     */
    private fun sendCommand(message: MAVLinkMessage) {
        fMessageStream.write(message)
        fMessageQueue.remove()
    }

}