package ch.hsr.ifs.gcs.mission.need.parameter

/**
 * This [Parameter] implementation is used to configure the desired cargo of the vehicle.
 *
 * @since 1.0.0
 * @author IFS Institute for Software
 */
class Cargo : Parameter<String> {

    override val id = "ch.hsr.ifs.gcs.mission.need.parameter.cargo"

    override lateinit var result: String

    override fun resultToString(): String {
        return result
    }

}