package ch.hsr.ifs.gcs.ui.fragments.needparameters

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import ch.hsr.ifs.gcs.R
import ch.hsr.ifs.gcs.need.parameter.Altitude
import kotlinx.android.synthetic.main.fragment_choose_altitude.*

class AltitudeFragment : Fragment() {

    var needParameter: Altitude? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_choose_altitude, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        altitudeSelectionBackgroundView.setOnClickListener {} // needed to shadow events on underlying map view
        setDefaultResult()
        increaseButton.setOnClickListener {
            needParameter?.result = needParameter?.result?.plus(1)
            altitudeValueTextView.text = "${needParameter?.result}m"
        }
        decreaseButton.setOnClickListener {
            if(needParameter!!.result!! != 0) {
                needParameter?.result = needParameter?.result?.minus(1)
                altitudeValueTextView.text = "${needParameter?.result}m"
            }
        }
    }

    private fun setDefaultResult() {
        altitudeValueTextView.text = "5m"
        needParameter?.result = 5
    }

}
