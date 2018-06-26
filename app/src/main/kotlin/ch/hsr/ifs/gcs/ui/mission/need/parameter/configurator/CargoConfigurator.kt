package ch.hsr.ifs.gcs.ui.mission.need.parameter.configurator

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import ch.hsr.ifs.gcs.R
import ch.hsr.ifs.gcs.ui.mission.need.parameter.item.CargoItem
import kotlinx.android.synthetic.main.fragment_choose_cargo.*

class CargoConfigurator() : Fragment() {

    lateinit var needParameter: CargoItem

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_choose_cargo, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        cargoSelectionView.setOnClickListener {} // needed to shadow events on underlying map view
        cargoButton.text = "Medkit"
        setDefaultResult()
        cargoButton.setOnClickListener {
            needParameter.parameter.result = "Medkit"
        }
    }

    private fun setDefaultResult() {
        needParameter.parameter.result = "Medkit"
    }

}
