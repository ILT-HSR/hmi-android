package ch.hsr.ifs.gcs.model

/**
 * This [Need] implementation represents the need to generate a heat map for radiation in a
 * provided region using a certain mode.
 *
 * @since 1.0.0
 * @author IFS Institute for Software
 */
class RadiationMap : Need {

    private val regionParameter = ChooseRegionNeedParameter()
    private val altitudeParameter = ChooseAltitudeNeedParameter()
    private val modeParameter = ChooseModeNeedParameter()

    override val name = "Radiation Map"

    override val needParameterList: List<NeedParameter<*>> = arrayListOf(
            regionParameter,
            altitudeParameter,
            modeParameter)

    override var isActive = false

    override fun getTasks(): List<Task>? {
        TODO("not implemented")
    }

}