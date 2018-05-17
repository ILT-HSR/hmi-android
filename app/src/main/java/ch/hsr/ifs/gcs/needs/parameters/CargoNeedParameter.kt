package ch.hsr.ifs.gcs.needs.parameters

import ch.hsr.ifs.gcs.MainActivity
import ch.hsr.ifs.gcs.R
import ch.hsr.ifs.gcs.ui.fragments.needparameters.CargoFragment

/**
 * This [NeedParameter] implementation is used to configure the desired cargo of the vehicle.
 *
 * @since 1.0.0
 * @author IFS Institute for Software
 */
class CargoNeedParameter : NeedParameter<String> {

    private val fragment = CargoFragment()

    override val name = "Cargo"

    override val description = "Select the cargo involved in your mission."

    override var result: String? = ""

    override fun resultToString(): String {
        return result!!
    }

    override var isActive = false

    override var isCompleted = false

    override fun setup(context: MainActivity) {
        fragment.task = this
        context.fragmentHandler?.performFragmentTransaction(R.id.mapholder, fragment)
    }

    override fun cleanup(context: MainActivity) {
        context.fragmentHandler?.removeFragment(fragment)
    }

}