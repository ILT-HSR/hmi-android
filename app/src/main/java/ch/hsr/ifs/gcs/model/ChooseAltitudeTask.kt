package ch.hsr.ifs.gcs.model

class ChooseAltitudeTask: Task<Int> {

    override val name get() = "Altitude"

    override val description get() = "Choose the altitude for your vehicle."

    override var result: Int? = 0

    override var isActive = false

    override var isCompleted = false

}