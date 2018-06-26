package ch.hsr.ifs.gcs.mission.need

import ch.hsr.ifs.gcs.mission.need.parameter.Altitude
import ch.hsr.ifs.gcs.mission.need.parameter.Region
import ch.hsr.ifs.gcs.mission.need.task.Task
import ch.hsr.ifs.gcs.resource.capability.CAPABILITY_CAN_FLY
import ch.hsr.ifs.gcs.resource.Capability
import ch.hsr.ifs.gcs.resource.Resource

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

    override val id = "ch.hsr.ifs.gcs.mission.need.radiationMap"

    override val parameterList = listOf(
            regionParameter,
            altitudeParameter
    )

    override val tasks: List<Task>?
        get() = emptyList()

    override val requirements: List<Capability<*>>
        get() = listOf(
                Capability(CAPABILITY_CAN_FLY, true)
        )

}