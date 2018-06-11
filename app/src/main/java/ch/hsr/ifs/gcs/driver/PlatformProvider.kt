package ch.hsr.ifs.gcs.driver

import android.content.Context
import ch.hsr.ifs.gcs.comm.SerialDataChannel
import ch.hsr.ifs.gcs.comm.SerialDataChannel.Configuration
import ch.hsr.ifs.gcs.driver.mavlink.internal.PixhawkPX4
import com.hoho.android.usbserial.driver.UsbSerialPort
import java.nio.channels.ByteChannel

object PlatformProvider {

    private val fSerialDrivers = mutableMapOf<String, (ByteChannel) -> SerialPlatform>(
            DRIVER_MAVLINK_PIXHAWK_PX4 to ::PixhawkPX4
    )

    /**
     * Create a new driver instance for the given [port] in the given [context]
     *
     * This function ensures, that the communication port is initialized correctly, as required
     * by the driver implementation.
     *
     * @param driverId The ID associated with the desired platform driver
     * @param context The application context used for device input/output
     * @param port The USB port to use for device communication
     * @param configuration The USB-Serial port configuration for the given device
     *
     * @return A new instance of platform driver if a vehicle was detected on the
     * provided port, `null` otherwise.
     */
    fun instantiate(driverId: String, context: Context, port: UsbSerialPort, configuration: Configuration = Configuration()) =
            fSerialDrivers[driverId]?.let {
                SerialDataChannel.create(context, port, configuration)?.let(it)
            }
}