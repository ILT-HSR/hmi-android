package ch.hsr.ifs.gcs.ui.mission.need.parameter.item

import ch.hsr.ifs.gcs.ui.MainActivity
import ch.hsr.ifs.gcs.R
import ch.hsr.ifs.gcs.mission.need.parameter.Altitude
import ch.hsr.ifs.gcs.ui.fragments.FragmentType
import ch.hsr.ifs.gcs.ui.mission.need.parameter.configurator.AltitudeConfigurator
import kotlinx.android.synthetic.main.activity_main.*
import org.osmdroid.views.MapView


class AltitudeItem(parameter: Altitude) : BasicParameterItem<Int>(parameter) {

    private val fragment = AltitudeConfigurator()

    override val name = "Altitude"

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