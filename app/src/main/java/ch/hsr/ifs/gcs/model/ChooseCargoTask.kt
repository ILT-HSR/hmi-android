package ch.hsr.ifs.gcs.model

class ChooseCargoTask : Task<String> {

    override val name get() = "Cargo"

    override val description get() = "Select the cargo involved in your mission."

    override var result: String? = ""

    override var isActive = false

    override var isCompleted = false

    override fun completeTask() {
        // TODO: Replace dummy data
        result = "Medkit"
    }

}