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
    private val fIOStream = MAVLinkStream(schema, channel)

    private val fHeartbeat = createHeartbeatMessage(8, 250, schema)
    private val fHeartbeatExecutor = Executors.newSingleThreadScheduledExecutor()

    private val fCapabilities = createRequestAutopilotCapabilitiesMessage(0, 8, 250, schema)

    private val fCommandQueue = ConcurrentLinkedQueue<MAVLinkMessage>()

    private var fVehicleLastHeartbeat: Long = 0
    private lateinit var fVehicleVendor: String
    private lateinit var fVehicleProduct: String


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
            fCommandQueue.offer(fHeartbeat)

            if (!(this::fVehicleProduct.isInitialized && this::fVehicleVendor.isInitialized)) {
                fCommandQueue.offer(fCapabilities)
            }
        }, 0, 1, TimeUnit.SECONDS)
    }

    override val isAlive get() = (System.currentTimeMillis() - fVehicleLastHeartbeat) < 10000

    override val name get() = if (this::fVehicleVendor.isInitialized) "$fVehicleVendor $fVehicleProduct" else "<unknown>"

    override fun arm() {
        fCommandQueue.offer(createArmMessage(1, 8, 250, schema))
    }

    override fun disarm() {
        fCommandQueue.offer(createDisarmMessage(1, 8, 250, schema))
    }

    private fun handle(message: MAVLinkMessage) {
        when (message.msgName) {
            MAVLinkMessageName.HEARTBEAT.name -> fVehicleLastHeartbeat = System.currentTimeMillis()
            MAVLinkMessageName.AUTOPILOT_VERSION.name -> handleVersion(message)
            else -> Log.d(TAG, "Unsupported message '$message'")
        }
    }

    private fun handleVersion(message: MAVLinkMessage) {
        val vendorId = message["vendor_id"] as? Int
        val productId = message["product_id"] as? Int

        fVehicleVendor = MAVLinkVendors[vendorId ?: 0]
        fVehicleProduct = MAVLinkProducts[productId ?: 0]
    }

}