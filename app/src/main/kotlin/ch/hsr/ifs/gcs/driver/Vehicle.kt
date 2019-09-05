package ch.hsr.ifs.gcs.driver

import ch.hsr.ifs.gcs.support.geo.GPSPosition
import ch.hsr.ilt.uxv.hmi.core.driver.Command

/**
 * The interface of drivers for vehicles
 *
 * This interface defines the common functionality shared by all vehicles.
 *
 * @since 1.0.0
 * @author IFS Institute for Software
 */
interface Vehicle : Platform {

    /**
     * Instruct the vehicle to move to the given GPS position
     *
     * @since 1.0.0
     */
    fun moveTo(position: GPSPosition): Command<*>

    /**
     * Instruct the vehicle to return to its launch position
     *
     * @since 1.0.0
     */
    fun returnToHome(): Command<*>

    /**
     * Set the maximum travel speed for the vehicle
     *
     * @since 1.1.0
     */
    fun limitTravelSpeed(speed: Double): Command<*>

}