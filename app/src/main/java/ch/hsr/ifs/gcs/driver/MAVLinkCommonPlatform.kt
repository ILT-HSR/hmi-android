package ch.hsr.ifs.gcs.driver

import me.drton.jmavlib.mavlink.MAVLinkSchemaRegistry

/**
 * This driver interface specifies the commands and queries support by 'Common' profile MAVLink
 * vehicles.
 *
 * @since 1.0.0
 * @author IFS Institute for Software
 */
interface MAVLinkCommonPlatform : MAVLinkPlatform {

    override val schema get() = MAVLinkSchemaRegistry["common"]!!

}