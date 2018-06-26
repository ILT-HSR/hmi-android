package ch.hsr.ifs.gcs.mission.need.parameter

import org.osmdroid.util.GeoPoint

/**
 * This [Parameter] implementation is used to define a region in which a need has to take place.
 *
 * @since 1.0.0
 * @author IFS Institute for Software
 */
class Region : Parameter<List<GeoPoint>> {

    override val id = "ch.hsr.ifs.gcs.mission.need.parameter.region"

    override lateinit var result: List<GeoPoint>

    override fun resultToString() = "${result.size} waypoints"

}