package ch.hsr.ifs.gcs.model

import org.osmdroid.util.GeoPoint

class ChooseTargetTask : Task<GeoPoint> {

    override val name get() = "Target"

    override val description get() = "Choose a single target on the map."

    override var result: GeoPoint? = null

    override var isActive = false

    override fun completeTask() {
        // TODO: Replace dummy data
        val chosenPoint = GeoPoint(47.223231, 8.816547)
        result = chosenPoint
    }

}