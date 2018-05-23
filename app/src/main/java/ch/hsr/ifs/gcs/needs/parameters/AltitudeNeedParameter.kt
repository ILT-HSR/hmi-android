package ch.hsr.ifs.gcs.needs.parameters

import ch.hsr.ifs.gcs.MainActivity

/**
 * This [NeedParameter] implementation is used to configure the desired altitude of the vehicle.
 *
 * @since 1.0.0
 * @author IFS Institute for Software
 */
class AltitudeNeedParameter: NeedParameter<Int> {

    override val name = "Altitude"

    override val description = "Choose the altitude for your vehicle."

    override var result: Int? = 0

    override fun resultToString(): String {
        return "$result meters"
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