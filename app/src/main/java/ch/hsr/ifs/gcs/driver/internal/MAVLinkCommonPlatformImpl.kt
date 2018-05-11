package ch.hsr.ifs.gcs.driver.internal

import android.util.Log
import ch.hsr.ifs.gcs.comm.protocol.*
import ch.hsr.ifs.gcs.driver.*
import me.drton.jmavlib.mavlink.MAVLinkMessage
import me.drton.jmavlib.mavlink.MAVLinkProducts
import me.drton.jmavlib.mavlink.MAVLinkStream
import me.drton.jmavlib.mavlink.MAVLinkVendors
import java.nio.channels.ByteChannel
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * Concrete implementation of the [platform driver interface][Platform] for MAVLink vehicles
 *
 * @since 1.0.0
 * @author IFS Institute for Software
 */
internal open class MAVLinkCommonPlatformImpl constructor(channel: ByteChannel) : MAVLinkCommonPlatform {

    companion object {
        private val TAG = MAVLinkCommonPlatformImpl::class.simpleName
    }

    private val fSender = MAVLinkSystem(8, 250)
    private val fTarget = MAVLinkSystem(1, 1)

    private val fIOStream = MAVLinkStream(schema, channel)
    private val fCommandQueue = ConcurrentLinkedQueue<MAVLinkMessage>()

    private val fExecutors = object {
        val io = Executors.newSingleThreadScheduledExecutor()
        val heartbeat = Executors.newSingleThreadScheduledExecutor()
        val lowFrequency = Executors.newSingleThreadScheduledExecutor()
        val highFrequency = Executors.newSingleThreadScheduledExecutor()
    }

    private val fMessages = object {
        val heartbeat = createHeartbeatMessage(fSender, schema)
        val capabilities = createRequestAutopilotCapabilitiesMessage(fSender, fTarget, schema)
    }

    private val fVehicleState = object {
        var lastHeartbeat: Long = 0
        var vendor: String? = null
        var product: String? = null
    }

    private val fMessageListeners = HashMap<MAVLinkPlatform.MessageID, MutableList<(MAVLinkMessage) -> Unit>>()

    init {
        registerBasicHandlers()
        beginSerialIO()
        scheduleHeartbeat()
        requestVehicleCapabilities()
    }

    override val driverId get() = DRIVER_MAVLINK_COMMON

    override val isAlive get() = (System.currentTimeMillis() - fVehicleState.lastHeartbeat) < maximumExpectedHeartbeatInterval

    override val name
        get() = fVehicleState.vendor?.let {
            "${fVehicleState.vendor} ${fVehicleState.product}"
        } ?: "<unknown>"

    override fun arm() = enqueueCommand(createArmMessage(fSender, fTarget, schema))

    override fun disarm() = enqueueCommand(createDisarmMessage(fSender, fTarget, schema))

    override fun takeOff(altitude: AerialVehicle.Altitude) = Unit

    override fun land() = Unit

    override fun moveTo(position: GPSPosition) = enqueueCommand(createDoRepositionMessage(fSender, fTarget, schema, WGS89Position(position)))

    override fun changeAltitude(altitude: AerialVehicle.Altitude) = Unit

    /**
     * Enqueue a command into the internal command queue
     *
     * @since 1.0.0
     */
    protected fun enqueueCommand(message: MAVLinkMessage) {
        fCommandQueue.offer(message)
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
    protected val maximumExpectedHeartbeatInterval = 10000

    /**
     * Add a listener for a specific [message type][MAVLinkPlatform.MessageID]
     *
     * @since 1.0.0
     */
    protected fun addListener(messageName: MAVLinkPlatform.MessageID, handler: (MAVLinkMessage) -> Unit) {
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
        addListener(MAVLinkPlatform.MessageID.HEARTBEAT, this::handleHeartbeat)
        addListener(MAVLinkPlatform.MessageID.AUTOPILOT_VERSION, this::handleVersion)
    }

    /**
     * Initialize the serial I/O connection with the vehicle.
     *
     * We communicate with the vehicle using a serial (RS232) interface. The serial connection is
     * wrapped in a MAVLink message stream, which allows us to work on a more 'abstract' level.
     */
    private fun beginSerialIO() {
        fExecutors.io.scheduleAtFixedRate({
            while (true) {
                fIOStream.read()?.let(this::dispatch)

                fCommandQueue.poll()?.let {
                    fIOStream.write(it)
                }
            }
        }, 0, 138, TimeUnit.MICROSECONDS)
    }

    /**
     * Schedule the transmission of 'Heartbeat' messages to the vehicle at a fixed 1s interval
     */
    private fun scheduleHeartbeat() {
        fExecutors.heartbeat.scheduleAtFixedRate({
            fCommandQueue.offer(fMessages.heartbeat)
        }, 0, 1, TimeUnit.SECONDS)
    }

    /**
     * Request the basic vehicle capabilities
     *
     * We need to know certain aspects of the vehicle, for example the vendor and firmware version
     * in order to be able to make certain decisions.
     */
    private fun requestVehicleCapabilities() {
        enqueueOnLowFrequencyScheduler {
            fVehicleState.vendor ?: fCommandQueue.offer(fMessages.capabilities)
        }.let { f ->
            addListener(MAVLinkPlatform.MessageID.AUTOPILOT_VERSION, object : (MAVLinkMessage) -> Unit {
                override fun invoke(msg: MAVLinkMessage) {
                    if (!f.isCancelled) {
                        f.cancel(false)
                        fMessageListeners[MAVLinkPlatform.MessageID.AUTOPILOT_VERSION]!!.remove(this)
                    }
                }
            })
        }
    }

    /**
     * Dispatch a received MAVLink message to the associated listener(s)
     */
    private fun dispatch(message: MAVLinkMessage) {
        when (MAVLinkPlatform.MessageID.from(message.msgName)) {
            null -> Log.d(TAG, "Unsupported message '$message'")
            else -> {
                MAVLinkPlatform.MessageID.from(message.msgName)?.let {
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
    private fun handleHeartbeat(message: MAVLinkMessage) {
        fVehicleState.lastHeartbeat = System.currentTimeMillis()
        Log.i(TAG, "Heartbeat from ${message.systemID}:${message.componentID} at ${fVehicleState.lastHeartbeat}")
        Log.i(TAG, "Base mode : ${message.getInt("base_mode")}, status : ${message.getInt("system_status")}")
    }

    /**
     * Process a `Autopilot Version` message received on the link
     */
    private fun handleVersion(message: MAVLinkMessage) {
        fVehicleState.vendor = MAVLinkVendors[message["vendor_id"] as? Int ?: 0]
        fVehicleState.product = MAVLinkProducts[message["product_id"] as? Int ?: 0]
    }

    /**
     * Enqueue a task on the high frequency scheduler
     *
     * The high frequency scheduler repeats its tasks at a rate of 10 times per second. This is
     * useful for messages that need to be 'spammed' to the vehicle, for example when 'killing' the
     * vehicle by sending it disarm messages.
     */
    private fun enqueueOnHighFrequencyScheduler(task: () -> Unit) =
            fExecutors.highFrequency.scheduleAtFixedRate(task, 0, 100, TimeUnit.MILLISECONDS)


    /**
     * Enqueue a task on the high frequency scheduler
     *
     * The high frequency scheduler repeats its tasks at a rate of 1 time per 5 seconds. This is
     * useful for messages that request status information that needs to be updates regularly but
     * not often.
     */
    private fun enqueueOnLowFrequencyScheduler(task: () -> Unit) =
            fExecutors.lowFrequency.scheduleAtFixedRate(task, 0, 5000, TimeUnit.MILLISECONDS)

}