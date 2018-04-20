package ch.hsr.ifs.gcs.driver.internal

import android.util.Log
import ch.hsr.ifs.gcs.driver.MAVLinkCommonPlatform
import ch.hsr.ifs.gcs.driver.Platform
import me.drton.jmavlib.createArmMessage
import me.drton.jmavlib.createDisarmMessage
import me.drton.jmavlib.createHeartbeatMessage
import me.drton.jmavlib.createRequestAutopilotCapabilitiesMessage
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

    private val fExecutors = object {
        val io = Executors.newSingleThreadScheduledExecutor()
        val heartbeat = Executors.newSingleThreadScheduledExecutor()
        val lowFrequency = Executors.newSingleThreadScheduledExecutor()
        val highFrequency = Executors.newSingleThreadScheduledExecutor()
    }

    private val fMessages = object {
        val heartbeat = createHeartbeatMessage(8, 250, schema)
        val capabilities = createRequestAutopilotCapabilitiesMessage(0, 8, 250, schema)
    }

    private val fVehicleState = object {
        var lastHeartbeat: Long = 0
        var vendor: String? = null
        var product: String? = null
    }

    init {
        fExecutors.io.scheduleAtFixedRate({
            while (true) {
                fIOStream.read()?.let(this@MAVLinkCommonPlatformImpl::handle)

                fCommandQueue.poll()?.let {
                    fIOStream.write(it)
                }
            }
        }, 0, 138, TimeUnit.MICROSECONDS)

        fExecutors.heartbeat.scheduleAtFixedRate({
            fCommandQueue.offer(fMessages.heartbeat)
        }, 0, 1, TimeUnit.SECONDS)
    }

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
        fCommandQueue.offer(createArmMessage(1, 8, 250, schema))
    }

    override fun disarm() {
        fCommandQueue.offer(createDisarmMessage(1, 8, 250, schema))
    }

    private fun handle(message: MAVLinkMessage) {
        when (MAVLinkMessageName.from(message.msgName)) {
            MAVLinkMessageName.HEARTBEAT -> fVehicleState.lastHeartbeat = System.currentTimeMillis()
            MAVLinkMessageName.AUTOPILOT_VERSION -> handleVersion(message)
            null -> Log.d(TAG, "Unsupported message '$message'")
        }
    }

    private fun handleVersion(message: MAVLinkMessage) {
        fVehicleState.vendor = MAVLinkVendors[message["vendor_id"] as? Int ?: 0]
        fVehicleState.product = MAVLinkProducts[message["product_id"] as? Int ?: 0]
    }

    private fun enqueueOnHighFrequencyScheduler(task: () -> Unit) {
        fExecutors.highFrequency.scheduleAtFixedRate(task, 0, 100, TimeUnit.MILLISECONDS)
    }

    private fun enqueueOnLowFrequencyScheduler(task: () -> Unit) {
        fExecutors.lowFrequency.scheduleAtFixedRate(task, 0, 5000, TimeUnit.MILLISECONDS)
    }
}