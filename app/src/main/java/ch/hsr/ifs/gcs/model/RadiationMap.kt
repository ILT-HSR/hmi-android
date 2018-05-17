package ch.hsr.ifs.gcs.model

class RadiationMap : Need {

    private val regionParameter = ChooseRegionNeedParameter()
    private val altitudeParameter = ChooseAltitudeNeedParameter()
    private val modeParameter = ChooseModeNeedParameter()

    override val name get() = "Radiation Map"

    override val needParameterList: List<NeedParameter<Any>> = arrayListOf(
            regionParameter as NeedParameter<Any>,
            altitudeParameter as NeedParameter<Any>,
            modeParameter as NeedParameter<Any>
    )

    override var isActive = false

    override fun getTasks(): List<Task>? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}