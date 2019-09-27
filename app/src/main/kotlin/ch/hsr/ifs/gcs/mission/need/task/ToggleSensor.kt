package ch.hsr.ifs.gcs.mission.need.task

import ch.hsr.ifs.gcs.driver.ToggleablePayload
import ch.hsr.ifs.gcs.driver.Vehicle
import ch.hsr.ifs.gcs.resource.Resource

class ToggleSensor : Task {
    override fun executeOn(resource: Resource) = with(resource.plaform as Vehicle) {
        when(val sensor = payloads.firstOrNull()) {
            is ToggleablePayload -> sensor.trigger()
            else -> listOf()
        }
    }
}