package ch.hsr.ifs.gcs.ui.mission.need.parameter.configurator

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ch.hsr.ifs.gcs.R
import ch.hsr.ifs.gcs.ui.mission.need.parameter.ParameterConfigurator
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_choose_altitude.*

class AltitudeConfigurator : ParameterConfigurator<Int>() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_choose_altitude, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        altitudeSelectionBackgroundView.setOnClickListener {} // needed to shadow events on underlying map view
        altitudeValueTextView.text = "${parameter.parameter.result}m"
        increaseButton.setOnClickListener {
            parameter.parameter.result = parameter.parameter.result + 1
            altitudeValueTextView.text = "${parameter.parameter.result}m"
        }
        decreaseButton.setOnClickListener {
            if (parameter.parameter.result != 0) {
                parameter.parameter.result = parameter.parameter.result - 1
                altitudeValueTextView.text = "${parameter.parameter.result}m"
            }
        }
    }

    override fun present() {
        setDefaultResult()
        context.map.setBuiltInZoomControls(false)
        context.showMainFragment(this)
    }

    override fun destroy() {
        context.map.setBuiltInZoomControls(true)
        context.hideMainFragment()
    }

    private fun setDefaultResult() {
        parameter.parameter.result = 5
    }

}