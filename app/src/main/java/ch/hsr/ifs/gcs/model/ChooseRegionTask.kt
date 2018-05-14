package ch.hsr.ifs.gcs.model

import org.osmdroid.util.GeoPoint

class ChooseRegionTask: Task<List<GeoPoint>> {

    override val name get() = "Region"

    override val description get() = "Choose the region your mission should be carried out."

    override var result: List<GeoPoint>? = ArrayList()

    override var isActive = false

    override var isCompleted = false

    override fun completeTask() {
        // TODO: Replace dummy data
        val upperLeft = GeoPoint(47.223231, 8.816547)
        val lowerLeft = GeoPoint(47.222231, 8.816547)
        val upperRight = GeoPoint(47.223231, 8.819000)
        val lowerRight = GeoPoint(47.222231, 8.819000)
        val pointList = arrayListOf(upperLeft, upperRight, lowerLeft, lowerRight)
        result = pointList
    }

}