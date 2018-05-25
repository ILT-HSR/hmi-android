package ch.hsr.ifs.gcs.needs.parameters

import ch.hsr.ifs.gcs.MainActivity
import ch.hsr.ifs.gcs.R
import ch.hsr.ifs.gcs.ui.fragments.needparameters.ModeFragment
import org.osmdroid.views.MapView

/**
 * This [NeedParameter] implementation is used to configure the mode of the vehicle while
 * carrying out a need.
 *
 * @since 1.0.0
 * @author IFS Institute for Software
 */
class ModeNeedParameter: NeedParameter<String> {

    private val fragment = ModeFragment()

    override val name = "Mode"

    override val description = "Choose the mode for your vehicle."

    override var result: String? = ""

    override fun resultToString(): String {
        return result!!
    }

    override var isActive = false

    override var isCompleted = false

    override fun setup(context: MainActivity) {
        val mapView = context.findViewById<MapView>(R.id.map)
        mapView.setBuiltInZoomControls(false)
        fragment.needParameter = this
        context.fragmentHandler?.performFragmentTransaction(R.id.mapholder, fragment)
    }

    override fun cleanup(context: MainActivity) {
        val mapView = context.findViewById<MapView>(R.id.map)
        mapView.setBuiltInZoomControls(true)
        context.fragmentHandler?.removeFragment(fragment)
    }

}