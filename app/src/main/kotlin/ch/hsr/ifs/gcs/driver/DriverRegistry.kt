package ch.hsr.ifs.gcs.driver

import ch.hsr.ifs.gcs.driver.mavlink.platform.PixhawkPX4
import java.nio.channels.ByteChannel

object DriverRegistry {

    val drivers = mapOf<String, (ByteChannel, List<Payload>) -> Platform?>(
            PixhawkPX4.DRIVER_MAVLINK_PIXHAWK_PX4 to PixhawkPX4.Companion::instantiate
    )

}