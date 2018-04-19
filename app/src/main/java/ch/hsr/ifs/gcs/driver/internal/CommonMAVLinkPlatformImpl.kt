package ch.hsr.ifs.gcs.driver.internal

import android.content.Context
import ch.hsr.ifs.gcs.comm.SerialDataChannel
import ch.hsr.ifs.gcs.driver.CommonMAVLinkPlatform
import ch.hsr.ifs.gcs.driver.Platform
import com.hoho.android.usbserial.driver.UsbSerialPort
import me.drton.jmavlib.*
import me.drton.jmavlib.mavlink.MAVLinkMessage
import me.drton.jmavlib.mavlink.MAVLinkSchemaRegistry
import me.drton.jmavlib.mavlink.MAVLinkStream
import java.io.IOException
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

enum class MAVLinkMessageName {
    HEARTBEAT
}

/**
 * Concrete implementation of the [platform driver interface][Platform] for MAVLink vehicles
 *
 * @since 1.0.0
 * @author IFS Institute for Software
 */
class CommonMAVLinkPlatformImpl private constructor(val channel: SerialDataChannel) : CommonMAVLinkPlatform {

    private val fIOExecutor = Executors.newSingleThreadExecutor()
    private val fIOStream = MAVLinkStream(MAVLinkSchemaRegistry.instance["common"], channel)

    private val fHeartbeat = createMAVLinkHeartbeat(system = 8, component = 250)
    private val fHeartbeatExecutor = Executors.newSingleThreadScheduledExecutor()

    private val fCommandQueue = ConcurrentLinkedQueue<MAVLinkMessage>()

    private var fVehicleLastHeartbeat: Long = 0

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
                else -> CommonMAVLinkPlatformImpl(channel)
            }
        }

    }

    init {
        fIOExecutor.submit {
            while(true) {

                try {
                    fIOStream.read()?.let(this@CommonMAVLinkPlatformImpl::handle)
                } catch (e: IOException) {
                }

                fCommandQueue.poll()?.let{
                    fIOStream.write(it)
                }
            }
        }

        fHeartbeatExecutor.scheduleAtFixedRate({
            fCommandQueue.offer(fHeartbeat)
        }, 0, 1, TimeUnit.SECONDS)
    }

    private fun handle(message: MAVLinkMessage) {
        when(message.msgName) {
            MAVLinkMessageName.HEARTBEAT.name -> fVehicleLastHeartbeat = System.currentTimeMillis()
        }
    }

    override val isAlive = (System.currentTimeMillis() - fVehicleLastHeartbeat) < 10000

    override val name = "<unknown>"

    override fun arm() {
        fCommandQueue.offer(createArmMessage(system = 8, component = 250))
    }

    override fun disarm() {
        fCommandQueue.offer(createDisarmMessage(system = 8, component = 250))
    }

}