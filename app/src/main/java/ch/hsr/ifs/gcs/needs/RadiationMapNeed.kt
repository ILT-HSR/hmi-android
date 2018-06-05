package ch.hsr.ifs.gcs.needs

import ch.hsr.ifs.gcs.needs.parameters.AltitudeNeedParameter
import ch.hsr.ifs.gcs.needs.parameters.ModeNeedParameter
import ch.hsr.ifs.gcs.needs.parameters.NeedParameter
import ch.hsr.ifs.gcs.needs.parameters.RegionNeedParameter
import ch.hsr.ifs.gcs.resources.CAPABILITY_CAN_FLY
import ch.hsr.ifs.gcs.resources.Capability
import ch.hsr.ifs.gcs.resources.Resource
import ch.hsr.ifs.gcs.tasks.Task

/**
 * This [Need] implementation represents the need to generate a heat map for radiation in a
 * provided region using a certain mode.
 *
 * @since 1.0.0
 * @author IFS Institute for Software
 */
class RadiationMapNeed(override val resource: Resource) : Need {

    private val regionParameter = RegionNeedParameter()
    private val altitudeParameter = AltitudeNeedParameter()
    private val modeParameter = ModeNeedParameter()

    override val name = "Radiation Map"

    override val needParameterList: List<NeedParameter<*>> = arrayListOf(
            regionParameter,
            altitudeParameter,
            modeParameter
    )

    override var isActive = false

    override fun getTasks(): List<Task>? {
        TODO("not implemented")
    }

    override val requirements: List<Capability<*>>
        get() = listOf(
                Capability(CAPABILITY_CAN_FLY, true)
        )

}