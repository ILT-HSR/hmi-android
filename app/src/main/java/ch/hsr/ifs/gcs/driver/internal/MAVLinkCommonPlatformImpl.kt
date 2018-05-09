package ch.hsr.ifs.gcs.driver.internal

import android.util.Log
import ch.hsr.ifs.gcs.comm.protocol.*
import ch.hsr.ifs.gcs.driver.AerialVehicle
import ch.hsr.ifs.gcs.driver.MAVLinkCommonPlatform
import ch.hsr.ifs.gcs.driver.Platform
import me.drton.jmavlib.mavlink.MAVLinkMessage
import me.drton.jmavlib.mavlink.MAVLinkProducts
import me.drton.jmavlib.mavlink.MAVLinkStream
import me.drton.jmavlib.mavlink.MAVLinkVendors
import java.nio.channels.ByteChannel
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

enum class MAVLinkMessageName {
    HEARTBEAT,
    AUTOPILOT_VERSION;

    companion object {

        /**
         * Try to create a [MAVLinkMessageName] with the given name
         *
         * @param name The name of a MAVLink message
         * @return The corresponding [MAVLinkMessageName] if it exists, `null` otherwise
         * @since 1.0.0
         * @author IFS Institute for Software
         */
        fun from(name: String) = try {
            MAVLinkMessageName.valueOf(name)
        } catch (e: Exception) {
            null
        }

    }
}

private val TAG = MAVLinkCommonPlatformImpl::class.simpleName

/**
 * Concrete implementation of the [platform driver interface][Platform] for MAVLink vehicles
 *
 * @since 1.0.0
 * @author IFS Institute for Software
 */
internal class MAVLinkCommonPlatformImpl constructor(channel: ByteChannel) : MAVLinkCommonPlatform {

    private val fIOStream = MAVLinkStream(schema, channel)
    private val fCommandQueue = ConcurrentLinkedQueue<MAVLinkMessage>()
    private val fSender = MAVLinkSystem(8, 250)
    private val fTarget = MAVLinkSystem(1, 0)

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

    private val fMessageListeners = HashMap<MAVLinkMessageName, MutableList<(MAVLinkMessage) -> Unit>>()

    init {
        registerBasicHandlers()
        beginSerialIO()
        scheduleHeartbeat()
        requestVehicleCapabilities()
    }

    override val driverId get() = "ch.hsr.ifs.gcs.driver.generic.MAVLinkCommonPlatform"

    /**
     * Check if the connection to the vehicle is alive.
     *
     * @note A [MAVLinkCommonPlatform] vehicle is considered to be alive if the last heartbeat was
     * received within the last ten seconds.
     *
     * @since 1.0.0
     * @author IFS Institute for Software
     */
    override val isAlive get() = (System.currentTimeMillis() - fVehicleState.lastHeartbeat) < 10000

    override val name
        get() = fVehicleState.vendor?.let {
            "${fVehicleState.vendor} ${fVehicleState.product}"
        } ?: "<unknown>"

    override fun arm() {
        fCommandQueue.offer(createArmMessage(fSender, fTarget, schema))
    }

    override fun disarm() {
        fCommandQueue.offer(createDisarmMessage(fSender, fTarget, schema))
    }

    override fun takeOff(altitude: AerialVehicle.Altitude) {
        fCommandQueue.offer(createTakeOffLocalMessage(fSender, fTarget, schema, 0.0F, 1.0F, 0.0F, 0.0F, 0.0F, 0.0F));
    }

    override fun changeAltitude(altitude: AerialVehicle.Altitude) {
        fCommandQueue.offer(createLoiterToAltitudeMessage(fSender, fTarget, schema, altitude.meters.toFloat()))
    }

    private fun handle(message: MAVLinkMessage) {
        when (MAVLinkMessageName.from(message.msgName)) {
            null -> Log.d(TAG, "Unsupported message '$message'")
            else -> {
                MAVLinkMessageName.from(message.msgName)?.let {
                    fMessageListeners[it]?.forEach {
                        it(message)
                    }
                }
            }
        }
    }

    private fun handleHeartbeat(message: MAVLinkMessage) {
        fVehicleState.lastHeartbeat = System.currentTimeMillis()
        Log.d(TAG, "Last vehicle heartbeat at ${fVehicleState.lastHeartbeat}")
    }

    private fun handleVersion(message: MAVLinkMessage) {
        fVehicleState.vendor = MAVLinkVendors[message["vendor_id"] as? Int ?: 0]
        fVehicleState.product = MAVLinkProducts[message["product_id"] as? Int ?: 0]
    }

    private fun enqueueOnHighFrequencyScheduler(task: () -> Unit) =
            fExecutors.highFrequency.scheduleAtFixedRate(task, 0, 100, TimeUnit.MILLISECONDS)


    private fun enqueueOnLowFrequencyScheduler(task: () -> Unit) =
            fExecutors.lowFrequency.scheduleAtFixedRate(task, 0, 5000, TimeUnit.MILLISECONDS)


    private fun addListener(messageName: MAVLinkMessageName, handler: (MAVLinkMessage) -> Unit) {
        val listeners = fMessageListeners[messageName]
        if (listeners != null) {
            listeners.add(handler)
        } else {
            fMessageListeners[messageName] = listOf(handler).toMutableList()
        }
    }

    private fun registerBasicHandlers() {
        addListener(MAVLinkMessageName.HEARTBEAT, this::handleHeartbeat)
        addListener(MAVLinkMessageName.AUTOPILOT_VERSION, this::handleVersion)
    }

    private fun beginSerialIO() {
        fExecutors.io.scheduleAtFixedRate({
            while (true) {
                fIOStream.read()?.let(this@MAVLinkCommonPlatformImpl::handle)

                fCommandQueue.poll()?.let {
                    fIOStream.write(it)
                }
            }
        }, 0, 138, TimeUnit.MICROSECONDS)
    }

    private fun scheduleHeartbeat() {
        fExecutors.heartbeat.scheduleAtFixedRate({
            fCommandQueue.offer(fMessages.heartbeat)
        }, 0, 1, TimeUnit.SECONDS)
    }

    private fun requestVehicleCapabilities() {
        enqueueOnLowFrequencyScheduler {
            fVehicleState.vendor ?: fCommandQueue.offer(fMessages.capabilities)
        }.let { f ->
            addListener(MAVLinkMessageName.AUTOPILOT_VERSION, object : (MAVLinkMessage) -> Unit {
                override fun invoke(msg: MAVLinkMessage) {
                    if (!f.isCancelled) {
                        f.cancel(false)
                        fMessageListeners[MAVLinkMessageName.AUTOPILOT_VERSION]!!.remove(this)
                    }
                }
            })
        }
    }
}