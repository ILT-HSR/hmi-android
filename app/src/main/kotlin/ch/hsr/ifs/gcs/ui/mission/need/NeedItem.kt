package ch.hsr.ifs.gcs.ui.mission.need

import ch.hsr.ifs.gcs.mission.need.Need
import ch.hsr.ifs.gcs.ui.mission.need.parameter.ParameterItem

interface NeedItem {

    val isActive: Boolean

    val name: String

    val need: Need

    val parameters: List<ParameterItem<*>>

    fun activate()

    fun deactivate()
}