package ch.hsr.ifs.gcs.driver.platform

import ch.hsr.ifs.gcs.driver.Platform
import ch.hsr.ifs.gcs.support.geo.GPSPosition

/**
 * The interface of drivers for aerial vehicles
 *
 * This interface defines the common functionality shared by all aerial vehicles.
 *
 * @since 1.0.0
 * @author IFS Institute for Software
 */
interface AerialVehicle : Platform {

    /**
     * This class represents altitudes for aerial vehicles.
     *
     * @since 1.0.0
     * @author IFS Institute for Software
     */
    data class Altitude(val meters: Double) {

        fun plus(other: Altitude) = Altitude(meters + other.meters)

        fun minus(other: Altitude) = Altitude(meters - other.meters)

    }

    /**
     * Instruct the vehicle to take-off into the specified altitude
     *
     * @since 1.0.0
     */
    fun takeOff(altitude: Altitude)

    /**
     * Instruct the vehicle to land
     *
     * @since 1.0.0
     */
    fun land()

    /**
     * Instruct the vehicle to move to the given GPS position
     *
     * @since 1.0.0
     */
    fun moveTo(position: GPSPosition)

    /**
     * Instruct the vehicle to change its altitude to the given value
     *
     * @since 1.0.0
     */
    fun changeAltitude(altitude: Altitude)

    /**
     * Instruct the vehicle to return to its launch position
     *
     * @since 1.0.0
     */
    fun returnToLaunch()

    /**
     * Access the current position of the vehicle
     *
     * The returned position shall be `null` if no position has yet been determined
     *
     * @since 1.0.0
     */
    val currentPosition: GPSPosition?
}