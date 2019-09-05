package ch.hsr.ifs.gcs.driver

import ch.hsr.ilt.uxv.hmi.core.driver.Command
import ch.hsr.ilt.uxv.hmi.core.driver.Vehicle

/**
 * The interface of drivers for aerial vehicles
 *
 * This interface defines the common functionality shared by all aerial vehicles.
 *
 * @since 1.0.0
 * @author IFS Institute for Software
 */
interface AerialVehicle : Vehicle {

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
    fun takeOff(altitude: Altitude): Command<*>

    /**
     * Instruct the vehicle to land
     *
     * @since 1.0.0
     */
    fun land(): Command<*>

    /**
     * Instruct the vehicle to change its altitude to the given value
     *
     * @since 1.0.0
     */
    fun changeAltitude(altitude: Altitude): Command<*>

    /**
     * Instruct the vehicle to return to its launch position
     *
     * @since 1.0.0
     */
    fun returnToLaunch(): Command<*>

    override fun returnToHome() = returnToLaunch()

}