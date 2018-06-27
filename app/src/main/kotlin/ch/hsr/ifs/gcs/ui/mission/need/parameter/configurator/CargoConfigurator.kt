package ch.hsr.ifs.gcs.ui.mission.need.parameter.configurator

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ch.hsr.ifs.gcs.R
import ch.hsr.ifs.gcs.ui.fragments.MenuFragmentID
import ch.hsr.ifs.gcs.ui.mission.need.parameter.ParameterConfigurator
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_choose_cargo.*

class CargoConfigurator : ParameterConfigurator<String>() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_choose_cargo, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        cargoSelectionView.setOnClickListener {} // needed to shadow events on underlying map view
        cargoButton.text = parameter.parameter.result
        cargoButton.setOnClickListener {
            parameter.parameter.result = "Medkit"
        }
    }

    override fun present() {
        setDefaultResult()
        context.map.setBuiltInZoomControls(false)
        context.showMainFragment(this)
        context.leftButton.setOnClickListener {
            destroy()
            context.showMenuFragment(MenuFragmentID.NEEDS_FRAGMENT)
        }
    }

    override fun destroy() {
        context.map.setBuiltInZoomControls(true)
        context.hideMainFragment()
    }

    private fun setDefaultResult() {
        parameter.parameter.result = "Medkit"
    }

}
