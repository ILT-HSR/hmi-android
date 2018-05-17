package ch.hsr.ifs.gcs.model

class RadiationMap : Need {

    override val name get() = "Radiation Map"

    override val needParameterList: List<NeedParameter<Any>> = arrayListOf(ChooseRegionNeedParameter() as NeedParameter<Any>, ChooseAltitudeNeedParameter() as NeedParameter<Any>, ChooseModeNeedParameter() as NeedParameter<Any>)

    override var isActive = false

    override fun translateToFunctionList(): List<Task> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}