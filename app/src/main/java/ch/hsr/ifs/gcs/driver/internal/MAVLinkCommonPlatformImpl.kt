package ch.hsr.ifs.gcs.driver.internal

import android.util.Log
import ch.hsr.ifs.gcs.comm.protocol.*
import ch.hsr.ifs.gcs.driver.*
import me.drton.jmavlib.mavlink.MAVLinkMessage
import me.drton.jmavlib.mavlink.MAVLinkProducts
import me.drton.jmavlib.mavlink.MAVLinkStream
import me.drton.jmavlib.mavlink.MAVLinkVendors
import java.nio.channels.ByteChannel
import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

typealias MessageHandler = (MAVLinkMessage) -> Unit

/**
 * Concrete implementation of the [platform driver interface][Platform] for MAVLink vehicles
 *
 * @since 1.0.0
 * @author IFS Institute for Software
 */
internal open class MAVLinkCommonPlatformImpl(channel: ByteChannel) : MAVLinkCommonPlatform {

    companion object {
        private val LOG_TAG = MAVLinkCommonPlatformImpl::class.simpleName
        private val TIMEOUT_COMMAND_ACK = Duration.ofMillis(1000)
    }

    private val fSender = MAVLinkSystem(8, 250)
    private val fTarget = MAVLinkSystem(1, 1)

    private val fMessageStream = MAVLinkStream(schema, channel)
    private val fMessageQueue = ConcurrentLinkedQueue<MAVLinkMessage>()

    private val fExecutors = object {
        val io = Executors.newSingleThreadScheduledExecutor()
        val heartbeat = Executors.newSingleThreadScheduledExecutor()
        val lowFrequency = Executors.newSingleThreadScheduledExecutor()
        val highFrequency = Executors.newSingleThreadScheduledExecutor()
    }

    private val fPeriodicMessages = object {
        val heartbeat = createHeartbeatMessage(fSender, schema)
        val capabilities = createRequestAutopilotCapabilitiesMessage(fSender, fTarget, schema)
    }

    private val fMessageListeners = mutableMapOf<MessageID, MutableList<MessageHandler>>()

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

    override val driverId get() = DRIVER_MAVLINK_COMMON

    override val isAlive
        get() = synchronized(fVehicleState) {
            Instant.now() < fVehicleState.lastHeartbeat + maximumExpectedHeartbeatInterval
        }

    override val name
        get() = synchronized(fVehicleState) {
            fVehicleState.vendor?.let {
                "${fVehicleState.vendor} ${fVehicleState.product}"
            } ?: "<unknown>"
        }

    override val currentPosition: GPSPosition?
        get() = synchronized(fVehicleState) { fVehicleState.position }

    override fun arm() = enqueueCommand(createArmMessage(fSender, fTarget, schema))

    override fun disarm() = enqueueCommand(createDisarmMessage(fSender, fTarget, schema))

    override fun takeOff(altitude: AerialVehicle.Altitude) = Unit

    override fun land() = Unit

    override fun moveTo(position: GPSPosition) = enqueueCommand(createDoRepositionMessage(fSender, fTarget, schema, WGS89Position(position)))

    override fun changeAltitude(altitude: AerialVehicle.Altitude) = Unit

    override fun returnToLaunch() {
        enqueueCommand(createReturnToLaunchMessage(fSender, fTarget, schema))
    }

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
    protected val senderSystem get() = fSender

    /**
     * Get the target system
     *
     * @since 1.0.0
     */
    protected val targetSystem get() = fTarget

    /**
     * The maximum time between 'Heartbeats' for a vehicle to be considered alive (in ms)
     *
     * @since 1.0.0
     */
    protected val maximumExpectedHeartbeatInterval: Duration = Duration.ofSeconds(10)

    /**
     * Add a listener for a specific [message type][MAVLinkPlatform.MessageID]
     *
     * @since 1.0.0
     */
    protected fun addListener(messageName: MessageID, handler: MessageHandler) {
        val listeners = fMessageListeners[messageName]
        if (listeners != null) {
            listeners.add(handler)
        } else {
            fMessageListeners[messageName] = listOf(handler).toMutableList()
        }
    }

    /**
     * Register the listeners for the [message types][MAVLinkPlatform.MessageID] constituting the
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
                }
            }
        }
    }

    /**
     * Process a `Heartbeat` message received on the link
     */
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

    /**
     * Enqueue a needParameter on the high frequency scheduler
     *
     * The high frequency scheduler repeats its tasks at a rate of 10 times per second. This is
     * useful for messages that need to be 'spammed' to the vehicle, for example when 'killing' the
     * vehicle by sending it disarm messages.
     */
    private fun enqueueOnHighFrequencyScheduler(task: () -> Unit) =
            fExecutors.highFrequency.scheduleAtFixedRate(task, 0, 100, TimeUnit.MILLISECONDS)


    /**
     * Enqueue a needParameter on the high frequency scheduler
     *
     * The high frequency scheduler repeats its tasks at a rate of 1 time per 5 seconds. This is
     * useful for messages that request status information that needs to be updates regularly but
     * not often.
     */
    private fun enqueueOnLowFrequencyScheduler(task: () -> Unit) =
            fExecutors.lowFrequency.scheduleAtFixedRate(task, 0, 5000, TimeUnit.MILLISECONDS)
}