package ch.hsr.ifs.gcs.needs

import ch.hsr.ifs.gcs.MainActivity
import org.osmdroid.util.GeoPoint

/**
 * This [NeedParameter] implementation is used to define a region in which a need has to take place.
 *
 * @since 1.0.0
 * @author IFS Institute for Software
 */
class ChooseRegionNeedParameter: NeedParameter<List<GeoPoint>> {

    override val name = "Region"

    override val description = "Choose the region your mission should be carried out."

    override var result: List<GeoPoint>? = ArrayList()

    override fun resultToString(): String {
        TODO("not implemented")
    }

    override var isActive = false

    override var isCompleted = false

    override fun setup(context: MainActivity) {
        TODO("not implemented")
    }

    override fun cleanup(context: MainActivity) {
        TODO("not implemented")
    }

}