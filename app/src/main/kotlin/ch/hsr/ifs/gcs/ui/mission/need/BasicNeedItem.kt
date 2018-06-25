package ch.hsr.ifs.gcs.ui.mission.need

import ch.hsr.ifs.gcs.mission.need.Need

abstract class BasicNeedItem(override val need: Need) : NeedItem {

    private var fIsActive = false

    override val isActive: Boolean
        get() = fIsActive

    override fun activate() {
        fIsActive = true
    }

    override fun deactivate() {
        fIsActive = false
    }
}