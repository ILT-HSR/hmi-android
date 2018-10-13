package ch.hsr.ifs.gcs.ui.mission.need.parameter.configurator

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ch.hsr.ifs.gcs.R
import ch.hsr.ifs.gcs.ui.mission.need.parameter.ParameterConfigurator
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_choose_speed_limit.*

class SpeedLimitConfigurator : ParameterConfigurator<Double>() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_choose_speed_limit, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        speedLimitSelectionBackgroundView.setOnClickListener {} // needed to shadow events on underlying map view
        speedLimitValueTextView.text = "${parameter.parameter.result}m/s"
        increaseButton.setOnClickListener {
            parameter.parameter.result = parameter.parameter.result + 1
            speedLimitValueTextView.text = "${parameter.parameter.result}m/s"
        }
        decreaseButton.setOnClickListener {
            if (parameter.parameter.result != 1.0) {
                parameter.parameter.result = parameter.parameter.result - 1
                speedLimitValueTextView.text = "${parameter.parameter.result}m/s"
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
        parameter.parameter.result = 2.0
    }

}