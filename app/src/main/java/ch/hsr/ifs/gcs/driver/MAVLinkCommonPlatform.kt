package ch.hsr.ifs.gcs.driver

import android.content.Context
import ch.hsr.ifs.gcs.comm.SerialDataChannel
import ch.hsr.ifs.gcs.driver.internal.MAVLinkCommonPlatformImpl
import com.hoho.android.usbserial.driver.UsbSerialPort
import me.drton.jmavlib.mavlink.MAVLinkSchemaRegistry
import java.nio.channels.ByteChannel

/**
 * This driver interface specifies the commands and queries support by 'Common' profile MAVLink
 * vehicles.
 *
 * @since 1.0.0
 * @author IFS Institute for Software
 */
interface MAVLinkCommonPlatform : MAVLinkPlatform {

    override val schema get() = MAVLinkSchemaRegistry["common"]!!

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
        fun <ImplType : MAVLinkCommonPlatform> create(impl: (ByteChannel) -> ImplType, context: Context, port: UsbSerialPort): MAVLinkCommonPlatform? {
            val channel = SerialDataChannel.create(context, port, 57600, 8, 1, SerialDataChannel.Parity.NONE)
            return when (channel) {
                null -> null
                else -> impl(channel)
            }
        }

    }

}