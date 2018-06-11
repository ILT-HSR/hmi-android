package ch.hsr.ifs.gcs.need

import ch.hsr.ifs.gcs.need.parameter.Altitude
import ch.hsr.ifs.gcs.need.parameter.Mode
import ch.hsr.ifs.gcs.need.parameter.Parameter
import ch.hsr.ifs.gcs.need.parameter.Region
import ch.hsr.ifs.gcs.need.task.Task
import ch.hsr.ifs.gcs.resources.CAPABILITY_CAN_FLY
import ch.hsr.ifs.gcs.resources.Capability
import ch.hsr.ifs.gcs.resources.Resource

/**
 * This [Need] implementation represents the need to generate a heat map for radiation in a
 * provided region using a certain mode.
 *
 * @since 1.0.0
 * @author IFS Institute for Software
 */
class RadiationMap(override val resource: Resource) : Need {

    private val regionParameter = Region()
    private val altitudeParameter = Altitude()
    private val modeParameter = Mode()

    override val name = "Radiation Map"

    override val parameterList: List<Parameter<*>> = listOf(
            regionParameter,
            altitudeParameter,
            modeParameter
    )

    override var isActive = false

    override val tasks: List<Task>?
        get() {
            TODO("not implemented")
        }

    override val requirements: List<Capability<*>>
        get() = listOf(
                Capability(CAPABILITY_CAN_FLY, true)
        )

}