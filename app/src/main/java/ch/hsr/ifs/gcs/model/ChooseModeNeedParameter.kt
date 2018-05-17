package ch.hsr.ifs.gcs.model

import ch.hsr.ifs.gcs.MainActivity

/**
 * This [NeedParameter] implementation is used to configure the mode of the vehicle while
 * carrying out a need.
 *
 * @since 1.0.0
 * @author IFS Institute for Software
 */
class ChooseModeNeedParameter: NeedParameter<String> {

    override val name = "Mode"

    override val description = "Choose the mode for your vehicle."

    override var result: String? = ""

    override fun resultToString(): String {
        return result!!
    }

    override var isActive = false

    override var isCompleted = false

    override fun setup(context: MainActivity) {
        TODO("not implemented")
    }

    override fun cleanup(context: MainActivity) {
        TODO("not implemented")
    }

}