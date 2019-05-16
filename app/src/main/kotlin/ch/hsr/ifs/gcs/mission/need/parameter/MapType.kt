package ch.hsr.ifs.gcs.mission.need.parameter

/**
 * This [Parameter] implementation is used to configure the type of map to be created by the vehicle.
 *
 * @since 1.0.0
 * @author IFS Institute for Software
 */
class MapType : Parameter<String> {

    override val id = "ch.hsr.ifs.gcs.mission.need.parameter.maptype"

    override lateinit var result: String

    override fun resultToString(): String {
        return result
    }

    override fun copy(): Parameter<String> {
        val copy = MapType()
        copy.result = result
        return copy
    }

}