package ch.hsr.ifs.gcs.mission.need.task

import ch.hsr.ifs.gcs.resource.Resource
import ch.hsr.ilt.uxv.hmi.core.driver.Vehicle

class ReturnToHome : Task {
    override fun executeOn(resource: Resource) = with(resource.plaform as Vehicle) {
        listOf(returnToHome())
    }

}