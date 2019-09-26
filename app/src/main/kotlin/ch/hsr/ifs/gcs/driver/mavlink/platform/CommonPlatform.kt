package ch.hsr.ifs.gcs.driver.mavlink.platform

import ch.hsr.ifs.gcs.driver.Payload
import me.drton.jmavlib.mavlink.MAVLinkSchemaRegistry
import java.nio.channels.ByteChannel

/**
 * This class provides a basic [platform][ch.hsr.ifs.gcs.driver.Platform] implementation for the
 * MAVLink **common** schema. Specific platform drivers for vehicles implementing the **common**
 * schema should be derived from this class.
 *
 * @since 1.0.0
 * @author IFS Institute for Software
 */
abstract class CommonPlatform(channel: ByteChannel, payloads: List<Payload>) : BasicPlatform(channel, payloads, MAVLinkSchemaRegistry["common"]!!) {

    companion object {
        /**
         * The driver ID of the builtin [CommonPlatform] implementation
         *
         * @since 1.0.0
         * @author IFS Institute for Software
         */
        const val DRIVER_MAVLINK_COMMON = "ch.hsr.ifs.gcs.driver.mavlink.platform.CommonPlatform"
    }

    override val driverId get() = DRIVER_MAVLINK_COMMON

}