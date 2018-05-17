package ch.hsr.ifs.gcs.model

import ch.hsr.ifs.gcs.MainActivity

class ChooseAltitudeNeedParameter: NeedParameter<Int> {

    override val name get() = "Altitude"

    override val description get() = "Choose the altitude for your vehicle."

    override var result: Int? = 0

    override fun resultToString(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override var isActive = false

    override var isCompleted = false

    override fun setup(context: MainActivity) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun cleanup(context: MainActivity) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}