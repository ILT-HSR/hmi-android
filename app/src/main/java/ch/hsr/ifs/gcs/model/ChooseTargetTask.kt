package ch.hsr.ifs.gcs.model

import org.osmdroid.util.GeoPoint

class ChooseTargetTask : Task<GeoPoint> {

    override val name get() = "Target"

    override val description get() = "Choose a single target on the map."

    override var result: GeoPoint? = null

    override var isActive = false

    override var isCompleted = false

}