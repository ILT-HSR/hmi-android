package ch.hsr.ifs.gcs.model

class ChooseModeTask: Task<String> {

    override val name get() = "Mode"

    override val description get() = "Choose the mode for your vehicle."

    override var result: String? = ""

    override var isActive = false

    override var isCompleted = false

    override fun completeTask() {
        // TODO: Replace dummy data
        result = "Autonomous"
    }

}