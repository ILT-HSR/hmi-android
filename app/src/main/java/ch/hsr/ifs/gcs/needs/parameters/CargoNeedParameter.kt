package ch.hsr.ifs.gcs.needs.parameters

import ch.hsr.ifs.gcs.MainActivity
import ch.hsr.ifs.gcs.R
import ch.hsr.ifs.gcs.ui.fragments.needparameters.CargoFragment
import org.osmdroid.views.MapView

/**
 * This [NeedParameter] implementation is used to configure the desired cargo of the vehicle.
 *
 * @since 1.0.0
 * @author IFS Institute for Software
 */
class CargoNeedParameter : NeedParameter<String> {

    private val fragment = CargoFragment()

    override val name = "Cargo"

    override val description = "Select the cargo involved in your mission."

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