package ch.hsr.ifs.gcs.ui.fragments.needparameters

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import ch.hsr.ifs.gcs.R
import ch.hsr.ifs.gcs.mission.need.parameter.Mode
import kotlinx.android.synthetic.main.fragment_choose_mode.*

class ModeFragment : Fragment() {

    var needParameter: Mode? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_choose_mode, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        modeSelectionView.setOnClickListener {} // needed to shadow events on underlying map view
        modeButton.text = "Autonomous"
        setDefaultResult()
        modeButton.setOnClickListener {
            needParameter?.result = "Autonomous"
        }
    }

    private fun setDefaultResult() {
        needParameter?.result = "Autonomous"
    }

}
