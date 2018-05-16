package ch.hsr.ifs.gcs.model

import org.osmdroid.util.GeoPoint

class CallIn : Need {

    override val name get() = "Call-in"

    override val taskList: List<Task<Any>>
        get() = arrayListOf(ChooseTargetTask() as Task<Any>, ChooseCargoTask() as Task<Any>)

    override var isActive = false

}