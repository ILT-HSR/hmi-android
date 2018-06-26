package ch.hsr.ifs.gcs.ui.mission.need

import ch.hsr.ifs.gcs.mission.need.Need
import ch.hsr.ifs.gcs.ui.mission.need.parameter.ParameterItemFactory

class NeedItem(val need: Need, val name: String) {

    private var fIsActive = false

    val isActive get() = fIsActive

    val parameters = need.parameterList.map(ParameterItemFactory::instantiate)

    fun activate() {
        fIsActive = true
    }

    fun deactivate() {
        fIsActive = false
    }
}