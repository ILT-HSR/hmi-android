package ch.hsr.ifs.gcs.need.parameter

import ch.hsr.ifs.gcs.MainActivity
import ch.hsr.ifs.gcs.R
import ch.hsr.ifs.gcs.ui.fragments.FragmentType
import ch.hsr.ifs.gcs.ui.fragments.needparameters.ModeFragment
import kotlinx.android.synthetic.main.activity_main.*
import org.osmdroid.views.MapView

/**
 * This [Parameter] implementation is used to configure the mode of the vehicle while
 * carrying out a need.
 *
 * @since 1.0.0
 * @author IFS Institute for Software
 */
class Mode: Parameter<String> {

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
        context.leftButton.setOnClickListener {
            cleanup(context)
            context.fragmentHandler?.performFragmentTransaction(R.id.menuholder, FragmentType.NEEDS_FRAGMENT)
        }
    }

    override fun cleanup(context: MainActivity) {
        val mapView = context.findViewById<MapView>(R.id.map)
        mapView.setBuiltInZoomControls(true)
        context.fragmentHandler?.removeFragment(fragment)
    }

}