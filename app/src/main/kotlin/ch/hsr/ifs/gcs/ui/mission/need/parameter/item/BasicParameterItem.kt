package ch.hsr.ifs.gcs.ui.mission.need.parameter.item

import ch.hsr.ifs.gcs.mission.need.parameter.Parameter
import ch.hsr.ifs.gcs.ui.mission.need.parameter.ParameterItem

abstract class BasicParameterItem<Result>(override val parameter: Parameter<Result>) : ParameterItem<Result> {

    private var fIsActive = false
    private var fIsComplete = false

    override val isActive: Boolean
        get() = fIsActive

    override val isComplete: Boolean
        get() = fIsComplete

    override fun activate() {
        fIsActive = true
    }

    override fun deactivate() {
        fIsActive = false
    }

    override fun markComplete() {
        fIsComplete = true
    }
}