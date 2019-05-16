package ch.hsr.ifs.gcs.mission.need.parameter

import ch.hsr.ifs.gcs.support.geo.GPSPosition

/**
 * This [Parameter] implementation is used to define a region in which a need has to take place.
 *
 * @since 1.0.0
 * @author IFS Institute for Software
 */
class Region : Parameter<List<GPSPosition>> {

    override val id = "ch.hsr.ifs.gcs.mission.need.parameter.region"

    override lateinit var result: List<GPSPosition>

    override fun resultToString() = "${result.size} waypoints"

    override fun copy(): Parameter<List<GPSPosition>> {
        val copy = Region()
        copy.result = result
        return copy
    }

}