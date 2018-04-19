package ch.hsr.ifs.gcs.driver

import me.drton.jmavlib.mavlink.MAVLinkSchemaRegistry

/**
 * This driver interface specifies the commands and queries support by 'Common' profile MAVLink
 * vehicles.
 */
interface CommonMAVLinkPlatform : Platform {

    val schema get() = MAVLinkSchemaRegistry["common"]

    /**
     * Arm the vehicle for takeoff
     */
    fun arm()

    /**
     * Disarm the vehicle, preventing takeoff
     */
    fun disarm()

}