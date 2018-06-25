package ch.hsr.ifs.gcs.mission.need.parameter

import ch.hsr.ifs.gcs.MainActivity
import ch.hsr.ifs.gcs.R
import ch.hsr.ifs.gcs.ui.fragments.FragmentType
import ch.hsr.ifs.gcs.ui.fragments.needparameters.AltitudeFragment
import kotlinx.android.synthetic.main.activity_main.*
import org.osmdroid.views.MapView

/**
 * This [Parameter] implementation is used to configure the desired altitude of the vehicle.
 *
 * @since 1.0.0
 * @author IFS Institute for Software
 */
class Altitude: Parameter<Int> {

    override val id = "ch.hsr.ifs.gcs.mission.need.parameter.altitude"

    override var result: Int = 0

    override fun resultToString(): String {
        return "$result meters"
    }

}