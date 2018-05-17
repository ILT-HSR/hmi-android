package ch.hsr.ifs.gcs.ui.fragments.needparameters

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import ch.hsr.ifs.gcs.R
import ch.hsr.ifs.gcs.model.ChooseCargoNeedParameter
import kotlinx.android.synthetic.main.fragment_choose_cargo.*

class ChooseCargoFragment : Fragment() {

    var task: ChooseCargoNeedParameter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_choose_cargo, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        cargoButton.setOnClickListener {
            task!!.result = "Medkit"
            itemCheckedView.background = context.getDrawable(R.drawable.checkbox_active_complete)
        }
    }

}
