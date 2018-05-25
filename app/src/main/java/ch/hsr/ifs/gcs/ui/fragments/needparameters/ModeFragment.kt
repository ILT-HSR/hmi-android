package ch.hsr.ifs.gcs.ui.fragments.needparameters

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import ch.hsr.ifs.gcs.R
import ch.hsr.ifs.gcs.needs.parameters.ModeNeedParameter
import kotlinx.android.synthetic.main.fragment_choose_mode.*

class ModeFragment : Fragment() {

    var needParameter: ModeNeedParameter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_choose_mode, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        modeSelectionView.setOnClickListener {} // needed to shadow events on underlying map view
        setDefaultResult()
        modeButton.setOnClickListener {
            needParameter?.result = "Autonomous"
            itemCheckedView.background = context.getDrawable(R.drawable.checkbox_active_complete)
        }
    }

    private fun setDefaultResult() {
        needParameter?.result = "Autonomous"
        itemCheckedView.background = context.getDrawable(R.drawable.checkbox_active_complete)
    }

}
