package ch.hsr.ifs.gcs.mission.need.parameter

/**
 * This [Parameter] implementation is used to configure the desired altitude of the vehicle.
 *
 * @since 1.0.0
 * @author IFS Institute for Software
 */
class SpeedLimit: Parameter<Double> {

    override val id = "ch.hsr.ifs.gcs.mission.need.parameter.speedLimit"

    override var result: Double = 1.0

    override fun resultToString(): String {
        return "$result m/s"
    }

}