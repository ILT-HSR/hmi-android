package ch.hsr.ifs.gcs.model

import ch.hsr.ifs.gcs.MainActivity
import org.osmdroid.util.GeoPoint

class ChooseRegionNeedParameter: NeedParameter<List<GeoPoint>> {

    override val name get() = "Region"

    override val description get() = "Choose the region your mission should be carried out."

    override var result: List<GeoPoint>? = ArrayList()

    override fun resultToString(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override var isActive = false

    override var isCompleted = false

    override fun setup(context: MainActivity) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun cleanup(context: MainActivity) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}