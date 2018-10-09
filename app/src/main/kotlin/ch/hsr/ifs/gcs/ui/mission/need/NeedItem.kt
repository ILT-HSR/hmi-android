package ch.hsr.ifs.gcs.ui.mission.need

import ch.hsr.ifs.gcs.mission.Need
import ch.hsr.ifs.gcs.ui.MainActivity

class NeedItem(val need: Need, val name: String) {

    private var fIsActive = false

    val isActive get() = fIsActive

    fun activate() {
        fIsActive = true
    }

    fun deactivate() {
        fIsActive = false
    }
}