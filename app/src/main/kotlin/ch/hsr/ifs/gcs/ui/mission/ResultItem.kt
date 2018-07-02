package ch.hsr.ifs.gcs.ui.mission

import ch.hsr.ifs.gcs.mission.Result

class ResultItem(val result: Result) {

    private var fIsActive = false

    val isActive get() = fIsActive

    fun activate() {
        fIsActive = true
    }

    fun deactivate() {
        fIsActive = false
    }

}