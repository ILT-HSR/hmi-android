package ch.hsr.ifs.gcs.driver

import android.location.Location
import ch.hsr.ifs.gcs.support.geo.GPSPosition

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
    fun moveTo(position: Location): Command<*>

    /**
     * Instruct the vehicle to return to its launch position
     *
     * @since 1.0.0
     */
    fun returnToHome(): Command<*>

}