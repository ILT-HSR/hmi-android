package ch.hsr.ifs.gcs.driver.platform.mavlink

import android.util.Log
import ch.hsr.ifs.gcs.driver.payload.mavlink.BasicPayload
import ch.hsr.ifs.gcs.driver.payload.mavlink.MAVLinkPayload
import ch.hsr.ifs.gcs.driver.platform.AerialVehicle
import ch.hsr.ifs.gcs.driver.platform.mavlink.support.*
import ch.hsr.ifs.gcs.mission.Execution
import ch.hsr.ifs.gcs.mission.need.task.Task
import ch.hsr.ifs.gcs.support.geo.GPSPosition
import ch.hsr.ifs.gcs.support.geo.WGS89Position
import me.drton.jmavlib.mavlink.*
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
    }

    private val fPeriodicMessages = object {
        val heartbeat = createHeartbeatMessage(fSender, schema)
        val capabilities = createRequestAutopilotCapabilitiesMessage(fSender, fTarget, schema)
    }

    private val fMessageListeners = mutableMapOf<MessageID, MutableList<(MAVLinkMessage) -> Unit>>()
    private val fOneShotMessageListeners = mutableMapOf<MessageID, MutableList<(MAVLinkMessage?) -> Unit>>()

    private val fVehicleState = object {
        /**
         * The GCS local time of the last received vehicle heartbeat in milliseconds
         */
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

    init {
        registerBasicHandlers()
        beginSerialIO()
        scheduleHeartbeat()
        requestVehicleCapabilities()
    }

    protected enum class ExecutionState {
        CREATED,
        UPLOADING
    }

    protected open inner class NativeMissionExecution(target: MAVLinkSystem, tasks: List<Task>) : Execution(tasks) {

        private val fMissionPlanner = MAVLinkSystem(target.id, COMPONENT_MISSION_PLANNER)
        private var fState = ExecutionState.CREATED

        private fun initiateUpload(): Unit = with(createTargetedMAVLinkMessage(MessageID.MISSION_COUNT, senderSystem, fMissionPlanner, schema)) {
            set("count", tasks.size)
            awaitResponse(this, MessageID.MISSION_REQUEST, 1, TimeUnit.SECONDS) {
                it?.apply { transmitItem(getInt("seq")) } ?: initiateUpload()
            }
        }

        private fun transmitItem(index: Int) {
            val command = tasks[index].asMAVLinkCommandDescriptor(this@BasicPlatform, this@BasicPlatform.payload as BasicPayload)
        }

        private fun upload() {
            fState = ExecutionState.UPLOADING
            initiateUpload()
        }

        override fun tick() = when (fState) {
            ExecutionState.CREATED -> {
                upload()
                Status.PREPARING
            }
            ExecutionState.UPLOADING -> Status.PREPARING
        }

    }

    // Platform implementation

    override val name
        get() = synchronized(fVehicleState) {
            fVehicleState.vendor?.let {
                "${fVehicleState.vendor} ${fVehicleState.product}"
            } ?: "<unknown>"
        }

    override val isAlive
        get() = synchronized(fVehicleState) {
            Instant.now() < fVehicleState.lastHeartbeat + maximumExpectedHeartbeatInterval
        }

    override val currentPosition: GPSPosition?
        get() = synchronized(fVehicleState) { fVehicleState.position }

    override fun getExecutionFor(tasks: List<Task>) =
            NativeMissionExecution(targetSystem, tasks) as Execution

    override val payload: MAVLinkPayload
    get() = TODO("Implement")

    // AerialVehicle implementation

    override fun takeOff(altitude: AerialVehicle.Altitude) = Unit

    override fun land() = Unit

    override fun moveTo(position: GPSPosition) = enqueueCommand(createDoRepositionMessage(fSender, fTarget, schema, WGS89Position(position)))

    override fun changeAltitude(altitude: AerialVehicle.Altitude) = Unit

    override fun returnToLaunch() {
        enqueueCommand(createReturnToLaunchMessage(fSender, fTarget, schema))
    }

    // MAVLinkPlatform implementation

    override fun arm() = enqueueCommand(createArmMessage(fSender, fTarget, schema))

    override fun disarm() = enqueueCommand(createDisarmMessage(fSender, fTarget, schema))

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
                                handler: (MAVLinkMessage?) -> Unit) = synchronized(fOneShotMessageListeners) {
        fOneShotMessageListeners.computeIfAbsent(response) { mutableListOf() } += handler
        enqueueCommand(message)
    }

    /**
     * Await the reception of a response of the given [type][MessageID], executing the provided
     * handler when the response arrives.
     *
     * The provided handler will be executed exactly once
     *
     * @param message The message to transmit
     * @param response The response ID to listen for
     * @param timeout How long to wait for the response
     * @param unit The unit of the timeout
     * @param handler The handler to be called on arrival of the specified response
     *
     * @since 1.0.0
     */
    protected fun awaitResponse(message: MAVLinkMessage,
                                response: MessageID,
                                timeout: Long,
                                unit: TimeUnit,
                                handler: (MAVLinkMessage?) -> Unit) = synchronized(fOneShotMessageListeners) {
        fOneShotMessageListeners.getOrDefault(response, mutableListOf()) += handler
        fExecutors.await.schedule({
            synchronized(fOneShotMessageListeners) {
                fOneShotMessageListeners[response]?.apply {
                    forEach { it.invoke(null) }
                    clear()
                }
            }
        }, timeout, unit)
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
            fMessageStream.read()?.let(this::dispatch)
            fMessageQueue.peek()?.let {
                when (it.msgName) {
                    MessageID.COMMAND_LONG.name -> sendLongCommand(it)
                    else -> sendCommand(it)
                }
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
            null -> Log.d(LOG_TAG, "Unsupported message '$message'")
            else -> {
                MessageID.from(message.msgName)?.let {
                    fMessageListeners[it]?.forEach {
                        it(message)
                    }
                    fOneShotMessageListeners[it]?.forEach {
                        it(message)
                    }
                    fOneShotMessageListeners.clear()
                }
            }
        }
    }

    /**
     * Process a `Heartbeat` message received on the link
     */
    @Suppress("UNUSED_PARAMETER")
    private fun handleHeartbeat(message: MAVLinkMessage): Unit = synchronized(fVehicleState) {
        fVehicleState.lastHeartbeat = Instant.now()
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
        Log.i(LOG_TAG, "Received ACK: $message")
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
                    Log.i(LOG_TAG, "Retransmitting $message")
                    fMessageStream.write(fCommandState.command)
                    fCommandState.transmittedAt = Instant.now()
                }
            }
            else -> {
                fCommandState.command = message
                Log.i(LOG_TAG, "Transmitting $message")
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