package ch.hsr.ifs.gcs.driver

import android.content.Context
import ch.hsr.ifs.gcs.comm.SerialDataChannel
import com.hoho.android.usbserial.driver.UsbSerialPort
import me.drton.jmavlib.MAVLINK_SCHEMA_COMMON
import me.drton.jmavlib.mavlink.MAVLinkMessage
import me.drton.jmavlib.mavlink.MAVLinkStream
import me.drton.jmavlib.newArmMessage
import me.drton.jmavlib.newDisarmMessage
import me.drton.jmavlib.newMAVLinkHeartbeat
import java.io.IOException
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * Concrete implementation of the [platform driver interface][Platform] for MAVLink vehicles
 *
 * @since 1.0.0
 * @author IFS Institute for Software
 */
class MAVLinkPlatform private constructor(val channel: SerialDataChannel) : Platform {

    private val fIOExecutor = Executors.newSingleThreadExecutor()
    private val fIOStream = MAVLinkStream(MAVLINK_SCHEMA_COMMON, channel)

    private val fHeartbeat = newMAVLinkHeartbeat()
    private val fHeartbeatExecutor = Executors.newSingleThreadScheduledExecutor()

    private val fCommandQueue = ConcurrentLinkedQueue<MAVLinkMessage>()

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
                else -> MAVLinkPlatform(channel)
            }
        }

    }

    init {
        fIOExecutor.submit {
            while(true) {

                try {
                    fIOStream.read()?.let(this@MAVLinkPlatform::handle)
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
    }

    override val isAlive = true

    override val name = "<unknown>"

    fun arm() {
        fCommandQueue.offer(newArmMessage())
    }

    fun disarm() {
        fCommandQueue.offer(newDisarmMessage())
    }

}