package ch.hsr.ifs.gcs.model

import ch.hsr.ifs.gcs.MainActivity

class ChooseModeTask: Task<String> {

    override val name get() = "Mode"

    override val description get() = "Choose the mode for your vehicle."

    override var result: String? = ""

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