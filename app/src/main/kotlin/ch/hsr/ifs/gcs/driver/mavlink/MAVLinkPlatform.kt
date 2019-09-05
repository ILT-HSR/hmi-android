package ch.hsr.ifs.gcs.driver.mavlink

import ch.hsr.ifs.gcs.driver.AerialVehicle
import ch.hsr.ifs.gcs.driver.SerialPlatform
import ch.hsr.ifs.gcs.driver.mavlink.support.MAVLinkSystem
import ch.hsr.ilt.uxv.hmi.core.driver.Command
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
    fun arm(): Command<*>

    /**
     * Disarm the vehicle, preventing takeoff
     *
     * @since 1.0.0
     */
    fun disarm(): Command<*>

    /**
     * The MAVLink system identifying the GCS
     *
     * @since 1.0.0
     */
    val senderSystem: MAVLinkSystem

    /**
     * The MAVLink system identifying the Vehicle
     *
     * @since 1.0.0
     */
    val targetSystem: MAVLinkSystem
}