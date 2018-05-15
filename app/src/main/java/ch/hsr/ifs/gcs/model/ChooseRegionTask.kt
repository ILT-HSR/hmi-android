package ch.hsr.ifs.gcs.model

import org.osmdroid.util.GeoPoint

class ChooseRegionTask: Task<List<GeoPoint>> {

    override val name get() = "Region"

    override val description get() = "Choose the region your mission should be carried out."

    override var result: List<GeoPoint>? = ArrayList()

    override var isActive = false

    override var isCompleted = false

}