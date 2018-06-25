package ch.hsr.ifs.gcs.mission.need.parameter

import ch.hsr.ifs.gcs.MainActivity
import ch.hsr.ifs.gcs.R
import ch.hsr.ifs.gcs.ui.fragments.FragmentType
import ch.hsr.ifs.gcs.ui.fragments.needparameters.CargoFragment
import kotlinx.android.synthetic.main.activity_main.*
import org.osmdroid.views.MapView

/**
 * This [Parameter] implementation is used to configure the desired cargo of the vehicle.
 *
 * @since 1.0.0
 * @author IFS Institute for Software
 */
class Cargo : Parameter<String> {

    override val id = "ch.hsr.ifs.gcs.mission.need.parameter.cargo"

    override lateinit var result: String

    override fun resultToString(): String {
        return result
    }

}