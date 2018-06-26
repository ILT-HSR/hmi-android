package ch.hsr.ifs.gcs.ui.mission.need.parameter

import ch.hsr.ifs.gcs.ui.MainActivity
import ch.hsr.ifs.gcs.R
import ch.hsr.ifs.gcs.mission.need.parameter.Cargo
import ch.hsr.ifs.gcs.ui.fragments.FragmentType
import ch.hsr.ifs.gcs.ui.fragments.needparameters.CargoFragment
import kotlinx.android.synthetic.main.activity_main.*
import org.osmdroid.views.MapView

class CargoItem(parameter: Cargo) : BasicParameterItem<String>(parameter) {

    private val fragment = CargoFragment()

    override val name = "Cargo"

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