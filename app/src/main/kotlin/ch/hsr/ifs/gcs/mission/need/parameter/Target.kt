package ch.hsr.ifs.gcs.mission.need.parameter

import org.osmdroid.util.GeoPoint

/**
 * This [Parameter] implementation is used to configure the target of the vehicle.
 *
 * @since 1.0.0
 * @author IFS Institute for Software
 */
class Target : Parameter<GeoPoint> {

    override val id = "ch.hsr.ifs.gcs.mission.need.parameter.target"

    override lateinit var result: GeoPoint

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