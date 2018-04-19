package ch.hsr.ifs.gcs.driver

/**
 * This driver interface specifies the commands and queries support by 'Common' profile MAVLink
 * vehicles.
 */
interface CommonMAVLinkPlatform : Platform {

    /**
     * Arm the vehicle for takeoff
     */
    fun arm()

    /**
     * Disarm the vehicle, preventing takeoff
     */
    fun disarm()

}