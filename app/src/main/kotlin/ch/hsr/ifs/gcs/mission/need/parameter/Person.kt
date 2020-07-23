package ch.hsr.ifs.gcs.mission.need.parameter

import ch.hsr.ifs.gcs.support.geo.GPSPosition

/**
 * This [Parameter] implementation is used to configure the desired cargo of the vehicle.
 *
 * @since 1.2.0
 * @author ILT Institute for Lab Automation and Mechatronics
 */
class Person : Parameter<GPSPosition> {

    override val id = "ch.hsr.ifs.gcs.mission.need.parameter.person"

    override lateinit var result: GPSPosition

    override fun resultToString(): String {
        return "lat: ${result.latitude}; lon: ${result.longitude}; alt: ${result.altitude}"
    }

    override fun copy(): Person {
        val copy = Person()
        copy.result = result
        return copy
    }

}