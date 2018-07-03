package ch.hsr.ifs.gcs.mission.need.task

import ch.hsr.ifs.gcs.driver.Vehicle
import ch.hsr.ifs.gcs.resource.Resource

class ReturnToHome : Task {
    override fun executeOn(resource: Resource) = with(resource.plaform as Vehicle) {
        listOf(returnToHome())
    }

}