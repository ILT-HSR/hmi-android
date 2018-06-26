package ch.hsr.ifs.gcs.ui.mission.need.parameter.configurator

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ch.hsr.ifs.gcs.R
import ch.hsr.ifs.gcs.ui.mission.need.parameter.item.AltitudeItem
import kotlinx.android.synthetic.main.fragment_choose_altitude.*

class AltitudeConfigurator : Fragment() {

    lateinit var needParameter: AltitudeItem

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_choose_altitude, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        altitudeSelectionBackgroundView.setOnClickListener {} // needed to shadow events on underlying map view
        setDefaultResult()
        increaseButton.setOnClickListener {
            needParameter.parameter.result = needParameter.parameter.result + 1
            altitudeValueTextView.text = "${needParameter.parameter.result}m"
        }
        decreaseButton.setOnClickListener {
            if(needParameter.parameter.result != 0) {
                needParameter.parameter.result = needParameter.parameter.result - 1
                altitudeValueTextView.text = "${needParameter.parameter.result}m"
            }
        }
    }

    private fun setDefaultResult() {
        altitudeValueTextView.text = "5m"
        needParameter.parameter.result = 5
    }

}
