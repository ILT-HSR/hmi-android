package ch.hsr.ifs.gcs.driver.platform.mavlink

import ch.hsr.ifs.gcs.driver.platform.AerialVehicle
import ch.hsr.ifs.gcs.driver.platform.SerialPlatform
import me.drton.jmavlib.mavlink.MAVLinkSchema

/**
 * This interface specifies the generic API of MAVLink vehicle platforms.
 *
 * @since 1.0.0
 * @author IFS Institute for Software
 */
interface MAVLinkPlatform : AerialVehicle, SerialPlatform {

    /**
     * The MAVLink message schema associated with this platform
     *
     * @since 1.0.0
     */
    val schema: MAVLinkSchema

    /**
     * Arm the vehicle for takeoff
     *
     * @since 1.0.0
     */
    fun arm()

    /**
     * Disarm the vehicle, preventing takeoff
     *
     * @since 1.0.0
     */
    fun disarm()

}