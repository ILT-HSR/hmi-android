package ch.hsr.ifs.gcs.driver.internal

import android.content.Context
import android.util.Log
import ch.hsr.ifs.gcs.comm.SerialDataChannel
import ch.hsr.ifs.gcs.driver.MAVLinkCommonPlatform
import ch.hsr.ifs.gcs.driver.Platform
import com.hoho.android.usbserial.driver.UsbSerialPort
import me.drton.jmavlib.createArmMessage
import me.drton.jmavlib.createDisarmMessage
import me.drton.jmavlib.createHeartbeatMessage
import me.drton.jmavlib.createRequestAutopilotCapabilitiesMessage
import me.drton.jmavlib.mavlink.MAVLinkMessage
import me.drton.jmavlib.mavlink.MAVLinkProducts
import me.drton.jmavlib.mavlink.MAVLinkStream
import me.drton.jmavlib.mavlink.MAVLinkVendors
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

enum class MAVLinkMessageName {
    HEARTBEAT,
    AUTOPILOT_VERSION
}

private val TAG = MAVLinkCommonPlatformImpl::class.simpleName

/**
 * Concrete implementation of the [platform driver interface][Platform] for MAVLink vehicles
 *
 * @since 1.0.0
 * @author IFS Institute for Software
 */
class MAVLinkCommonPlatformImpl private constructor(val channel: SerialDataChannel) : MAVLinkCommonPlatform {

    private val fIOExecutor = Executors.newSingleThreadExecutor()
    private val fHeartbeatExecutor = Executors.newSingleThreadScheduledExecutor()
    private val fLowFrequencyTaskExecutor = Executors.newSingleThreadScheduledExecutor()
    private val fHighFrequencyTaskExecutor = Executors.newSingleThreadScheduledExecutor()

    private val fIOStream = MAVLinkStream(schema, channel)
    private val fCommandQueue = ConcurrentLinkedQueue<MAVLinkMessage>()

    private val fMessages = object {
        val heartbeat = createHeartbeatMessage(8, 250, schema)
        val capabilities = createRequestAutopilotCapabilitiesMessage(0, 8, 250, schema)
    }

    private val fVehicleState = object {
        var lastHeartbeat: Long = 0
        var vendor: String? = null
        var product: String? = null
    }

    companion object {

        /**
         * Create a new driver instance for the given [port] in the given [context]
         *
         * This function ensures, that the communication port is initialized correctly, as required
         * by the driver implementation.
         *
         * @param context The application context used for device input/ouput
         * @param port The USB port to use for device communication
         * @return A new instance of the MAVLink platform driver if a vehicle was detected on the
         * provided port, `null` otherwise.
         */
        fun create(context: Context, port: UsbSerialPort): Platform? {
            val channel = SerialDataChannel.create(context, port, 57600, 8, 1, SerialDataChannel.Parity.NONE)
            return when (channel) {
                null -> null
                else -> MAVLinkCommonPlatformImpl(channel)
            }
        }

    }

    init {
        fIOExecutor.submit {
            while (true) {
                fIOStream.read()?.let(this@MAVLinkCommonPlatformImpl::handle)

                fCommandQueue.poll()?.let {
                    fIOStream.write(it)
                }
            }
        }

        fHeartbeatExecutor.scheduleAtFixedRate({
            fCommandQueue.offer(fMessages.heartbeat)
            fVehicleState.vendor?:fCommandQueue.offer(fMessages.capabilities)
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

    override val name get() = fVehicleState.vendor?.let {
        "${fVehicleState.vendor} ${fVehicleState.product}"
    } ?: "<unknown>"

    override fun arm() {
        fCommandQueue.offer(createArmMessage(1, 8, 250, schema))
    }

    override fun disarm() {
        fCommandQueue.offer(createDisarmMessage(1, 8, 250, schema))
    }

    private fun handle(message: MAVLinkMessage) {
        when (message.msgName) {
            MAVLinkMessageName.HEARTBEAT.name -> fVehicleState.lastHeartbeat = System.currentTimeMillis()
            MAVLinkMessageName.AUTOPILOT_VERSION.name -> handleVersion(message)
            else -> Log.d(TAG, "Unsupported message '$message'")
        }
    }

    private fun handleVersion(message: MAVLinkMessage) {
        fVehicleState.vendor = MAVLinkVendors[message["vendor_id"] as? Int ?: 0]
        fVehicleState.product = MAVLinkProducts[message["product_id"] as? Int ?: 0]
    }

}