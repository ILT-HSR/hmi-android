package ch.hsr.ifs.gcs.model

class RadiationMap : Need {

    override val name get() = "Radiation Map"

    override val taskList: List<Task<Any>>
        get() = arrayListOf(ChooseRegionTask() as Task<Any>, ChooseAltitudeTask() as Task<Any>, ChooseModeTask() as Task<Any>)

    override var isActive = false

}