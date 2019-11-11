package ch.hsr.ifs.gcs.driver

import ch.hsr.ifs.gcs.driver.channel.Channel
import ch.hsr.ifs.gcs.driver.generic.platform.NullPlatform
import ch.hsr.ifs.gcs.driver.mavlink.platform.PixhawkPX4

object PlatformDrivers {

    val drivers = mapOf<String, (Channel, List<Payload>) -> Platform?>(
            NullPlatform.DRIVER_ID to NullPlatform.Companion::instantiate,
            PixhawkPX4.DRIVER_MAVLINK_PIXHAWK_PX4 to PixhawkPX4.Companion::instantiate
    )

}