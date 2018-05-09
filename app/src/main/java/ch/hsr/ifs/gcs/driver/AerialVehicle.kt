package ch.hsr.ifs.gcs.driver

import ch.hsr.ifs.gcs.comm.protocol.GPSPosition

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

}