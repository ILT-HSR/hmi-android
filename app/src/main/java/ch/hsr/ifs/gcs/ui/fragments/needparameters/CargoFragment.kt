package ch.hsr.ifs.gcs.ui.fragments.needparameters

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import ch.hsr.ifs.gcs.R
import ch.hsr.ifs.gcs.needs.parameters.CargoNeedParameter
import kotlinx.android.synthetic.main.fragment_choose_cargo.*

class CargoFragment : Fragment() {

    var task: CargoNeedParameter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_choose_cargo, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        cargoSelectionView.setOnClickListener {} // needed to shadow events on underlying map view
        setDefaultResult()
        cargoButton.setOnClickListener {
            task?.result = "Medkit"
            itemCheckedView.background = context.getDrawable(R.drawable.checkbox_active_complete)
        }
    }

    private fun setDefaultResult() {
        task?.result = "Medkit"
        itemCheckedView.background = context.getDrawable(R.drawable.checkbox_active_complete)
    }

}
