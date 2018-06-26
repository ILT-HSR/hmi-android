package ch.hsr.ifs.gcs.ui.mission.need.parameter

import ch.hsr.ifs.gcs.mission.need.parameter.Parameter
import java.util.*

class ParameterItem<Result>(val parameter: Parameter<Result>, val name: String, private val fConfigurator: ParameterConfigurator<*>) : Observable() {

    private var fIsActive = false
    private var fIsComplete = false

    /**
     * Whether the the item is currently selected/activated
     *
     * @since 1.0.0
     */
    var isActive
        get() = fIsActive
        set(value) {
            fIsActive = value
            notifyObservers()
        }

    /**
     * Whether the parameter configuration has been completed
     */
    var isComplete
        get() = fIsComplete
        set(value) {
            fIsComplete = value
            notifyObservers()
        }

    @Suppress("UNCHECKED_CAST")
    fun showConfigurator() {
        (fConfigurator as ParameterConfigurator<Result>).parameter = this
        fConfigurator.present()
    }

    fun hideConfigurator() {
        fConfigurator.destroy()
    }
}