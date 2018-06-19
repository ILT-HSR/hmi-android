package ch.hsr.ifs.gcs.driver.access

import ch.hsr.ifs.gcs.driver.Platform
import ch.hsr.ifs.gcs.driver.SerialPlatform
import ch.hsr.ifs.gcs.driver.channel.ChannelFactory
import ch.hsr.ifs.gcs.driver.mavlink.platform.CommonPlatform
import ch.hsr.ifs.gcs.driver.mavlink.platform.CommonPlatform.Companion.DRIVER_MAVLINK_COMMON
import ch.hsr.ifs.gcs.driver.mavlink.platform.PixhawkPX4
import ch.hsr.ifs.gcs.driver.mavlink.platform.PixhawkPX4.Companion.DRIVER_MAVLINK_PIXHAWK_PX4
import java.nio.channels.ByteChannel

object PlatformProvider {

    private val fSerialDrivers = mutableMapOf<String, (ByteChannel, String?) -> SerialPlatform>(
            DRIVER_MAVLINK_PIXHAWK_PX4 to ::PixhawkPX4,
            DRIVER_MAVLINK_COMMON to ::CommonPlatform
    )

    fun registerSerialDriver(id: String, factory: (ByteChannel, String?) -> SerialPlatform) {
        fSerialDrivers[id] = factory
    }

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
    fun instantiate(driverId: String, factory: ChannelFactory, parameters: ChannelFactory.Parameters, payloadDriverId: String?) =
            fSerialDrivers[driverId]?.let { f ->
                factory.createChannel(parameters)?.let { c ->
                    f(c, payloadDriverId)
                }
            } as Platform?
}
