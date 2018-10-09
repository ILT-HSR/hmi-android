package ch.hsr.ifs.gcs.mission.need.parameter

import ch.hsr.ifs.gcs.support.geo.GPSPosition

/**
 * This [Parameter] implementation is used to configure the target of the vehicle.
 *
 * @since 1.0.0
 * @author IFS Institute for Software
 */
class Target : Parameter<GPSPosition> {

    override val id = "ch.hsr.ifs.gcs.mission.need.parameter.target"

    override lateinit var result: GPSPosition

    override fun resultToString(): String {
        result.let {
            var latitude = "${it.latitude}"
            latitude = latitude.dropLast(latitude.length - 7)
            var longitude = "${it.longitude}"
            longitude = longitude.dropLast(longitude.length - 7)
            return "$latitude, $longitude"
        }
    }

}