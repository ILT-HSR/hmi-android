package ch.hsr.ifs.gcs.driver.mavlink.platform

import ch.hsr.ifs.gcs.driver.Payload
import ch.hsr.ifs.gcs.driver.Platform
import ch.hsr.ifs.gcs.driver.channel.Channel

/**
 * Concrete  implementation of the [platform driver interface][Platform] for common UGVs.
 *
 * The main purpose of this implementation is to talk to ground vehicles that have a mavlink
 * implementation based on the common mavlink stack.
 *
 * @since 1.2.0
 * @author ILT Institute for Lab Automation and Mechatronics
 */
internal class CommonUGV private constructor(channel: Channel, payloads: List<Payload>) : CommonPlatform(channel, payloads) {

    companion object {
        /**
         * The driver ID of the builtin [ch.hsr.ifs.gcs.driver.mavlink.MAVLinkPlatform] implementation for common UGVs
         *
         * @since 1.2.0
         * @author ILT Institute for Lab Automation and Mechatronics
         */
        const val DRIVER_MAVLINK_COMMON_UGV = "ch.hsr.ifs.gcs.driver.mavlink.platform.CommonUGV"

        fun instantiate(channel: Channel, payloads: List<Payload>): CommonUGV? = CommonUGV(channel, payloads)
    }

    override val driverId get() = DRIVER_MAVLINK_COMMON_UGV

    override var fExecution = MissionExecution(this)

    init {
        start()
    }
}